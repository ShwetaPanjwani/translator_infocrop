package org.agmip.translators.infocrop;
import java.lang.*;
import java.io.BufferedWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import org.agmip.common.Functions;
import org.agmip.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;
import org.apache.commons.io.FileUtils;
import org.agmip.ace.LookupCodes;
import java.util.Scanner;

public class ExperimentDataProcessor 
{
	private static final Logger LOG = LoggerFactory.getLogger(ExperimentDataProcessor.class);
	private String outputDir;
	private String filePath;
        private String crid_year,crop;
	private final String NEW_LINE = "\r\n";
	private HashMap<String, Object> soilDataMap; 
	
	public void ProcessExperimentData(String outputDir, HashMap<String, Object> results,HashMap<String, Object> soilDataMap) throws IOException
	{
            String crid="",variety="",irrig="";
            
		 if (!outputDir.endsWith(File.separator)) {
                    outputDir += File.separator;
                }
                this.outputDir = outputDir;
		this.soilDataMap = soilDataMap;  
                BufferedWriter batchOutput = new BufferedWriter(new FileWriter(outputDir+"infocrop.bat",true));
                
                ArrayList<HashMap<String, Object>> experiments = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(results, "experiments", new HashMap<String, Object>());
                
                LOG.debug("outputDir"+outputDir);
                LOG.debug("experiments"+experiments);
                ClassLoader classLoader = getClass().getClassLoader();
		for(HashMap<String, Object> experiment : experiments) 
                {             
                    irrig=MapUtil.getValueOr(experiment, "IRRIG", "XX");
                    Calendar cal = Calendar.getInstance();                        
                    cal.setTime(Functions.convertFromAgmipDateString(MapUtil.getValueOr(experiment, "sdat", "XX"))); //timer.dat
                    InputStream inputStream = classLoader.getResourceAsStream("timer.dat");
                    InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(streamReader);
                    BufferedWriter output = new BufferedWriter(new FileWriter(outputDir+"timer_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat",true));         
                    LOG.debug("experiment"+MapUtil.getValueOr(experiment, "exname", "XX"));
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        //LOG.debug(line);
                        if(line.startsWith("WTRDIR"))
                        {
                           line="WTRDIR = '"+outputDir+"'";
                        }
                        if(line.startsWith("CNTR"))
                        {
                           line="CNTR = '"+MapUtil.getValueOr(experiment, "wst_id", "XX")+"'";
                        }
                        output.write(line+NEW_LINE);
                    } 
                    output.close();
                    
                    String crop_variety="",sowday="",sowdep="", plant_year="";
                    HashMap<String, Object> mgnData = (HashMap<String, Object>)MapUtil.getObjectOr(experiment, "management", new HashMap<String, Object>());
                    ArrayList<HashMap<String, Object>> events = (ArrayList<HashMap<String, Object>>)MapUtil.getObjectOr(mgnData, "events", new ArrayList<HashMap<String, Object>>());
                    // crid = (String) MapUtil.getObjectOr(events,"crid","");
                    StringBuffer irrigationList = new StringBuffer();
                    StringBuffer fertilizerList = new StringBuffer();
                    StringBuffer organicList = new StringBuffer();
                    irrigationList.append("IRRTSF=0.,0.,").append(NEW_LINE);
                    fertilizerList.append("UREAP1=0.,0.,").append(NEW_LINE);
                    organicList.append("OM1DAT=0.,0.,").append(NEW_LINE);
                    Calendar plant_cal = Calendar.getInstance();
                    for (HashMap<String, Object> event : events) {
                            if ("planting".equals(event.get("event"))) {
                                    crid = (String)event.get("crid");
                                    LOG.debug(event.get("date").toString());
                                    plant_cal.setTime(Functions.convertFromAgmipDateString(event.get("date").toString()));
                                    plant_year=String.valueOf(plant_cal.get(Calendar.YEAR));
                                    sowday=String.valueOf(plant_cal.get(Calendar.DAY_OF_YEAR));
                                    crop_variety=(String)event.get("infocrop_cul_id");
                                    crid = (String)event.get("crid");
                                    sowdep=(String)event.get("pldp");
                           }
                           if ("irrigation".equals(event.get("event"))) {
                               LOG.debug("irrig"+event.get("date"));
                               LOG.debug("irrig"+event.get("irval"));
                               Calendar irrg_cal = Calendar.getInstance();                        
                               irrg_cal.setTime(Functions.convertFromAgmipDateString(event.get("date").toString()));
                               long days_bet=daysBetween(plant_cal,irrg_cal);
                            //   LOG.debug(days_bet);
                                irrigationList.append(days_bet-1).append(".,").append(event.get("irval")).append(".,")
                                        .append(days_bet).append(".,").append(event.get("irval")).append(".,")
                                        .append(days_bet+1).append(".,").append(event.get("irval")).append(".,").append(NEW_LINE);
                               
                           }
                           if ("fertilizer".equals(event.get("event"))) {
                               LOG.debug("fertilzer"+event.get("date"));
                               LOG.debug("fertilzer"+event.get("feamn"));
                               LOG.debug("fertilzer"+event.get("fecd"));
                               Calendar fert_cal = Calendar.getInstance();                        
                               fert_cal.setTime(Functions.convertFromAgmipDateString(event.get("date").toString()));
                               long days_bet1=daysBetween(plant_cal,fert_cal);
                               if(event.get("fecd").equals("FE005")){
                                fertilizerList.append(days_bet1-1).append(".,").append(event.get("feamn")).append(".,")
                                         .append(days_bet1).append(".,").append(event.get("feamn")).append(".,")
                                         .append(days_bet1+1).append(".,").append(event.get("feamn")).append(".,").append(NEW_LINE);
                               }
                           }
                           if ("organic_matter".equals(event.get("event"))) {
                               LOG.debug("ord"+event.get("date"));
                               LOG.debug("orgamt"+event.get("omamt"));
                               Calendar fert_cal = Calendar.getInstance();                        
                               fert_cal.setTime(Functions.convertFromAgmipDateString(event.get("date").toString()));
                               long days_bet1=daysBetween(plant_cal,fert_cal);
                              
                                organicList.append(days_bet1-1).append(".,").append(event.get("omamt")).append(".,")
                                         .append(days_bet1).append(".,").append(event.get("omamt")).append(".,")
                                         .append(days_bet1+1).append(".,").append(event.get("omamt")).append(".,").append(NEW_LINE);
                               
                           }
                    }
                    irrigationList.append("365.,0.").append(NEW_LINE);
                    fertilizerList.append("365.,0.").append(NEW_LINE);
                    organicList.append("365.,0.").append(NEW_LINE);
                    LOG.debug(fertilizerList.toString());
                    if (crid == null) {
                        crid = MapUtil.getValueOr(experiment, "crid", "XX");
                    }  

                    crid = LookupCodes.lookupCode("CRID", crid, "INFOCROP");
                    variety= getVariety(crid,crop_variety);  
                    
                                                                 
                    HashMap<String, Object> soildata=(HashMap<String, Object> )MapUtil.getObjectOr(soilDataMap, MapUtil.getValueOr(experiment, "soil_id", "XX"), "XX");
                    InputStream inputStream1 = classLoader.getResourceAsStream(crid+".dat");
                    InputStreamReader streamReader1 = new InputStreamReader(inputStream1, StandardCharsets.UTF_8);
                    BufferedReader reader1 = new BufferedReader(streamReader1);
                    BufferedWriter output1 = new BufferedWriter(new FileWriter(outputDir+"model_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat",true));         
                    while ((line = reader1.readLine()) != null) 
                    {
                        if(line.startsWith("SLOPE") && soildata.get("SLOPE")!=null)   {
                           line="SLOPE = "+soildata.get("SLOPE");
                        }else if(line.startsWith("PHSOL") && soildata.get("PHSOL")!=null){
                           line="PHSOL = "+soildata.get("PHSOL");
                        }else if(line.startsWith("EC1") && soildata.get("EC1")!=null){
                           line="EC1 = "+soildata.get("EC1");
                        }else  if(line.startsWith("TKL1") && soildata.get("TKL1")!=null)   {
                           line="TKL1 = "+soildata.get("TKL1")+".";
                        }else if(line.startsWith("TKL2") && soildata.get("TKL2")!=null){
                           line="TKL2 = "+soildata.get("TKL2")+".";
                        }else if(line.startsWith("TKL3") && soildata.get("TKL3")!=null){
                           line="TKL3M = "+soildata.get("TKL3")+".";
                        }else if(line.startsWith("SILT1") && soildata.get("SILT1")!=null)  {
                           line="SILT1 = "+soildata.get("SILT1");
                        }else if(line.startsWith("SILT2") && soildata.get("SILT2")!=null){
                           line="SILT2 = "+soildata.get("SILT2");
                        }else if(line.startsWith("SILT3") && soildata.get("SILT3")!=null){
                           line="SILT3 = "+soildata.get("SILT3");
                        }else if(line.startsWith("CLAY1") && soildata.get("CLAY1")!=null)  {
                           line="CLAY1 = "+soildata.get("CLAY1");
                        }else if(line.startsWith("CLAY2") && soildata.get("CLAY2")!=null){
                           line="CLAY2 = "+soildata.get("CLAY2");
                        }else if(line.startsWith("CLAY3") && soildata.get("CLAY3")!=null){
                           line="CLAY3 = "+soildata.get("CLAY3");
                        }else if(line.startsWith("WCSTM1") &&  soildata.get("WCSTM1")!=null) {
                           line="WCSTM1 = "+soildata.get("WCSTM1");
                        }else if(line.startsWith("WCSTM2") && soildata.get("WCSTM2")!=null){
                           line="WCSTM2 = "+soildata.get("WCSTM2");
                        }else if(line.startsWith("WCSTM3") && soildata.get("WCSTM3")!=null){
                           line="WCSTM3 = "+soildata.get("WCSTM3");
                        }else if(line.startsWith("WCFCM1") && soildata.get("WCFCM1")!=null) {
                           line="WCFCM1 = "+soildata.get("WCFCM1");
                        }else if(line.startsWith("WCFCM2") && soildata.get("WCFCM2")!=null){
                           line="WCFCM2 = "+soildata.get("WCFCM2");
                        }else if(line.startsWith("WCFCM3") && soildata.get("WCFCM3")!=null){
                           line="WCFCM3 = "+soildata.get("WCFCM3");
                        }else if(line.startsWith("WCFPM1") && soildata.get("WCFPM1")!=null)  {
                           line="WCFPM1 = "+soildata.get("WCFPM1");
                        }else if(line.startsWith("WCFPM2") && soildata.get("WCFPM2")!=null){
                           line="WCFPM2 = "+soildata.get("WCFPM2");
                        }else if(line.startsWith("WCFPM3") && soildata.get("WCFPM3")!=null){
                           line="WCFPM3 = "+soildata.get("WCFPM3");
                        }else if(line.startsWith("KSATM1") && soildata.get("KSATM1")!=null) {
                           line="KSATM1 = "+soildata.get("KSATM1");
                        }else if(line.startsWith("KSATM2") && soildata.get("KSATM2")!=null){
                           line="KSATM2 = "+soildata.get("KSATM2");
                        }else if(line.startsWith("KSATM3") && soildata.get("KSATM3")!=null){
                           line="KSATM3 = "+soildata.get("KSATM3");
                        }else if(line.startsWith("BDM1") && soildata.get("BDM1")!=null)   {
                           line="BDM1 = "+soildata.get("BDM1");
                        }else if(line.startsWith("BDM2") && soildata.get("BDM2")!=null){
                           line="BDM2 = "+soildata.get("BDM2");
                        }else if(line.startsWith("BDM3") && soildata.get("BDM3")!=null){
                           line="BDM3 = "+soildata.get("BDM3");
                        }else if(line.startsWith("SOC1") && soildata.get("SOC1")!=null) {
                           line="SOC1 = "+soildata.get("SOC1");
                        }else if(line.startsWith("SOC2") && soildata.get("SOC2")!=null){
                           line="SOC2 = "+soildata.get("SOC2");
                        }else if(line.startsWith("SOC3") && soildata.get("SOC3")!=null){
                           line="SOC3 = "+soildata.get("SOC3");
                        }
                        //LOG.debug(line); 
                        output1.write(line+NEW_LINE);
                    } 
                    output1.close();
                   
                    //reruns.dat
                    BufferedWriter rerunOutput = new BufferedWriter(new FileWriter(outputDir+"reruns_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat",true));	
                    rerunOutput.write("IYEAR="+plant_year+NEW_LINE);
                    rerunOutput.write("STTIME="+String.valueOf(cal.get(Calendar.DAY_OF_YEAR))+"."+NEW_LINE);
                    rerunOutput.write("SOWFXD="+sowday+"."+NEW_LINE);
                    rerunOutput.write("SOWDEP="+sowdep+"."+NEW_LINE);
                    rerunOutput.write(variety);
                    rerunOutput.write(irrigationList.toString());
                    if(irrig.equals("Y")){
                        rerunOutput.write("SWCPOT=0."+NEW_LINE);
                    }else{
                        rerunOutput.write("SWCPOT=1."+NEW_LINE);
                    }
                    rerunOutput.write(fertilizerList.toString());
                    rerunOutput.write(organicList.toString());
                    rerunOutput.close(); 
                    
                    //batch
                    batchOutput.write("copy model_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat model.dat"+NEW_LINE);
                    batchOutput.write("copy timer_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat timer.dat"+NEW_LINE);
                    batchOutput.write("copy reruns_"+MapUtil.getValueOr(experiment, "exname", "XX")+".dat reruns.dat"+NEW_LINE);
                    batchOutput.write("copy res.dat "+MapUtil.getValueOr(experiment, "exname", "XX")+".dat"+NEW_LINE);
                    batchOutput.write("copy sum.out "+MapUtil.getValueOr(experiment, "exname", "XX")+".out"+NEW_LINE);
                    batchOutput.write("model"+NEW_LINE+NEW_LINE);
                    //new CSVInput();
                }                   
                batchOutput.close();
                
                //model
                File fromfile =  new File(classLoader.getResource(crid+".exe").getFile());
                File tofile = new File(outputDir+ "model.exe");
                FileUtils.copyFile(fromfile, tofile);        
	}
	
	
	
	public String foldString(StringBuffer stringlist)
	{
		StringBuffer returnList = new StringBuffer();
		int lastCommaIndex = 0;
		int postfix = 0;
		for(int i=0; i<stringlist.length(); i++)
		{
			char currentChar = stringlist.charAt(i);
			
			if( currentChar == ',' )
				lastCommaIndex = i+1;
			
			returnList.append(currentChar);
			
			if( i!=0 && (i%65) == 0  )
			{
				returnList.insert(lastCommaIndex+postfix, "..."+NEW_LINE);
				postfix+=5;
			}
 		}
		return returnList.toString();
	}
		
	public static long daysBetween(final Calendar startDate, final Calendar endDate)
	{  
			 int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;  
			 long endInstant = endDate.getTimeInMillis();  
			 int presumedDays = (int) ((endInstant - startDate.getTimeInMillis()) / MILLIS_IN_DAY);  
			 Calendar cursor = (Calendar) startDate.clone();  
			 cursor.add(Calendar.DAY_OF_YEAR, presumedDays);  
			 long instant = cursor.getTimeInMillis();  
			 if (instant == endInstant)  
			  return presumedDays;  
			 final int step = instant < endInstant ? 1 : -1;  
			 do {  
			  cursor.add(Calendar.DAY_OF_MONTH, step);  
			  presumedDays += step;  
			 } while (cursor.getTimeInMillis() != endInstant);  
			 return presumedDays;  
	}  	
	
	public void writeFile(String text)
	{
		try {
	          File file = new File(this.filePath);
	          
	          BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
	         
	          output.write(text+NEW_LINE);
	          output.close();
	        } catch ( IOException e ) {
	           e.printStackTrace();
	        }
	}
        
        protected String getVariety(String crid,String crop_variety) {
           String variety=""; 
           try {
                    BufferedReader br = new BufferedReader(new FileReader(outputDir+"CROP_VARIETY.txt"));
                    // Declaring a string variable
                    String st;
                    while ((st = br.readLine()) != null)
                    {
                       String[] st1=st.split(",");// Print the string
                       //LOG.debug(st1[1].toUpperCase()+"_"+arr[2].toUpperCase());
                       if(st1[0].toUpperCase().equals(crid.toUpperCase()) && st1[1].toUpperCase().equals(crop_variety.toUpperCase()))
                       {
                            
                            if(st1[3].contains(".")){
                                variety="TGMBD = " + st1[3]+NEW_LINE;
                            }else{
                                variety="TGMBD = " + st1[3]+"."+NEW_LINE;
                            }
                            if(st1[4].contains(".")){
                                variety+="TTGERM = " + st1[4]+NEW_LINE;
                            }else{
                                variety+="TTGERM = " + st1[4]+"."+NEW_LINE;
                            }
                            if(st1[5].contains(".")){
                                variety+="TPOPT = " + st1[5]+NEW_LINE;
                            }else{
                                variety+="TPOPT = " + st1[5]+"."+NEW_LINE;
                            }
                            if(st1[6].contains(".")){
                                variety+="TVBD = " + st1[6]+NEW_LINE;
                            }else{
                                variety+="TVBD = " + st1[6]+"."+NEW_LINE;
                            }
                            if(st1[7].contains(".")){
                                variety+="TTVG = " + st1[7]+NEW_LINE;
                            }else{
                                variety+="TTVG = " + st1[7]+"."+NEW_LINE;
                            }
                            if(st1[8].contains(".")){
                                variety+="TPMAXD = " + st1[8]+NEW_LINE;
                            }else{
                                variety+="TPMAXD = " + st1[8]+"."+NEW_LINE;
                            }
                            if(st1[9].contains(".")){
                                variety+="TGBD = " + st1[9]+NEW_LINE;
                            }else{
                                variety+="TGBD = " + st1[9]+"."+NEW_LINE;
                            }
                            if(st1[10].contains(".")){
                                variety+="TTGF = " + st1[10]+NEW_LINE;
                            }else{
                                variety+="TTGF = " + st1[10]+"."+NEW_LINE;
                            }
                            if(st1[11].contains(".")){
                                variety+="DAYSEN = " + st1[11]+NEW_LINE;
                            }else{
                                variety+="DAYSEN = " + st1[11]+"."+NEW_LINE;
                            }
                            if(st1[12].contains(".")){
                                variety+="RGRPOT = "  + st1[12]+NEW_LINE;
                            }else{
                                variety+="RGRPOT = "  + st1[12]+"."+NEW_LINE;
                            }
                            if(st1[13].contains(".")){
                                variety+="SLAVAR = " +   st1[13]+NEW_LINE;
                            }else{
                                variety+="SLAVAR = " +   st1[13]+"."+NEW_LINE;
                            }
                            if(st1[14].contains(".")){
                                variety+="GREENF = "  + st1[14]+NEW_LINE;
                            }else{
                                variety+="GREENF = "  + st1[14]+"."+NEW_LINE;
                                }
                            if(st1[15].contains(".")){
                                variety+="KDFMAX = " +   st1[15]+NEW_LINE;
                            }else{
                                 variety+="KDFMAX = " +   st1[15]+"."+NEW_LINE;
                                }
                            if(st1[16].contains(".")){
                                variety+="RUEMAX = " +   st1[16]+NEW_LINE;
                            }else{
                                variety+="RUEMAX = " +   st1[16]+"."+NEW_LINE;
                                }
                            if(st1[17].contains(".")){
                                variety+="ZRTPOT = " +   st1[17]+NEW_LINE;
                            }else{
                                variety+="ZRTPOT = " +   st1[17]+"."+NEW_LINE;
                            }
                            if(st1[18].contains(".")){
                                variety+="VARFLD = " +   st1[18]+NEW_LINE;
                            }else{
                                variety+="VARFLD = " +   st1[18]+"."+NEW_LINE;
                            }
                            if(st1[19].contains(".")){
                                variety+="VARNFX = "  + st1[19]+NEW_LINE;
                            }else{
                                variety+="VARNFX = "  + st1[19]+"."+NEW_LINE;
                            }
                            if(st1[20].contains(".")){
                                variety+="GNOCF = "  + st1[20]+NEW_LINE;
                            }else{
                                variety+="GNOCF = "  + st1[20]+"."+NEW_LINE;
                            }
                            if(st1[21].contains(".")){
                                variety+="POTGWT = " + st1[21]+NEW_LINE;
                            }else{
                                variety+="POTGWT = " + st1[21]+"."+NEW_LINE;
                            }
                            if(st1[22].contains(".")){
                                variety+="NMAXGR = "  + st1[22]+NEW_LINE;
                            }else{
                                variety+="NMAXGR = "  + st1[22]+"."+NEW_LINE;
                            }
                            if(st1[23].contains(".")){
                                variety+="VRSTMN = "  + st1[23]+NEW_LINE;
                            }else{
                                variety+="VRSTMN = "  + st1[23]+"."+NEW_LINE;
                            }
                            if(st1[24].contains(".")){
                                variety+="VRSTMX = "  + st1[24]+NEW_LINE;
                            }else{ 
                                variety+="VRSTMX = "  + st1[24]+"."+NEW_LINE;
                            }
                       }
                    }
                } catch ( IOException e ) {
	           e.printStackTrace();
	        } 
                    return variety;
        }
}
