package org.agmip.translators.infocrop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.agmip.common.Functions;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(WeatherProcessor.class);
	private String outputDir;
	private int stationIndex = 1;
	public void ProcessWeatherData(String outputDir, HashMap<String, Object> results) throws IOException
	{
		if (!outputDir.endsWith(File.separator)) {
                    outputDir += File.separator;
                 }
                this.outputDir = outputDir;
                
                
                //System.out.println("outputDir---"+outputDir);
		ArrayList<HashMap<String, Object>> weathers = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(results, "weathers", new HashMap<String, Object>());
                //System.out.println("wedathets"+weathers);
               
		for(HashMap<String, Object> wst : weathers) {                   
                   ArrayList<HashMap<String, Object>> wthRecords = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(wst, "dailyWeather", new ArrayList<HashMap<String, Object>>());
                //System.out.println("wthRecords"+wthRecords);
                String startYear = MapUtil.getValueOr((wthRecords.get(0)), "w_date", "    ").substring(0, 4).trim();
                String endYear = MapUtil.getValueOr((wthRecords.get(wthRecords.size() - 1)), "w_date", "    ").substring(0, 4).trim();
              //  System.out.println(Integer.valueOf(startYear)+"--"+Integer.valueOf(endYear));
                    generateWeatherFile(wst,Integer.valueOf(startYear),Integer.valueOf(endYear)); 
                }
	}

	
	public void generateWeatherFile(HashMap<String, Object> wst,int startYear,int endYear)
	{
		String arngstrom_const_a="0.25",  arngstrom_const_b="0.5";
		String lat =  MapUtil.getValueOr(wst, "wst_lat", "-99");
		String lng =  MapUtil.getValueOr(wst, "wst_long", "-99");
		String elevation =  MapUtil.getValueOr(wst, "wst_elev", "0");
		String wst_id =  MapUtil.getValueOr(wst, "wst_id", "-99");
		//System.out.println("wst"+wst_id);
		StringBuffer headerData= new StringBuffer();
                String fileName ="";
                headerData.append("*----------------------------------------------------------------*").append("\r\n")
                        .append("* Station Name:").append(wst_id).append("\r\n").append("* Author Name:").append("\r\n")
                        .append("* Creation Date:").append("\r\n").append("* Longitude:  ").append(lng).append(" Latitude:  ").append(lat).append(" Altitude:  ").append(elevation).append("\r\n")
                        .append("* Comments:").append("\r\n")
                        .append("*----------------------------------------------------------------*").append("\r\n")
                        .append("* Column	Daily Value		Units").append("\r\n")
                        .append("* 1		Station number").append("\r\n")
                        .append("* 2		Year").append("\r\n")
                        .append("* 3		Day").append("\r\n")
                        .append("* 4		Irradiance		KJ m-2").append("\r\n")
                        .append("* 5		Min Temperature		oC").append("\r\n")
                        .append("* 6		Max Temperature		oC").append("\r\n")
                        .append("* 7		Early Morning VP		kPa").append("\r\n")
                        .append("* 8		Mean Wind Speed		m s-1").append("\r\n")
                        .append("* 9		Precipitation		mm d-1").append("\r\n")
                        .append("*----------------------------------------------------------------*").append("\r\n");
		headerData.append(lng).append(" ").append(lat).append(" ").append(elevation).append(" ").append("0").append(" ").append("0").append("\r\n");
			
		ArrayList<HashMap<String, Object>> dailyWeather = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(wst, "dailyWeather", new ArrayList<HashMap<String, Object>>());
                try {
                   for(int i=startYear;i<=endYear;i++)
                   {
                            //System.out.println(Integer.toString(i));
                            StringBuffer recordData = new StringBuffer(); 
                            fileName = outputDir+wst_id.substring(0,4)+stationIndex+"."+Integer.toString(i).substring(1,4).toString();   
                           // System.out.println("fileName"+fileName);
                            File file = new File(fileName);
                            BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
                            for(HashMap<String, Object> todaysWeather : dailyWeather) 
                            {
                                     Calendar cal = Calendar.getInstance();
                                     cal.setTime(Functions.convertFromAgmipDateString(MapUtil.getValueOr(todaysWeather, "w_date", "99990909")));
                                     if(cal.get(Calendar.YEAR)==i)
                                     {
                                        String solar_radiation = Functions.multiply(MapUtil.getValueOr(todaysWeather, "srad", "0"),"1000");			 
                                        recordData.append(stationIndex).append(" ")
                                       .append(cal.get(Calendar.YEAR)).append(" ").append(cal.get(Calendar.DAY_OF_YEAR)).append(" ")
                                       .append(solar_radiation).append(" ")
                                       .append(MapUtil.getValueOr(todaysWeather, "tmin", "-9999")).append(" ")
                                       .append(MapUtil.getValueOr(todaysWeather, "tmax", "-9999")).append(" ")
                                       .append(MapUtil.getValueOr(todaysWeather, "vprsd", "0")).append(" ")
                                       .append(MapUtil.getValueOr(todaysWeather, "wind", "0")).append(" ")
                                       .append(MapUtil.getValueOr(todaysWeather, "rain", "0")).append("\r\n");
                                     }
                            }	
                           // System.out.println("recordData"+recordData);	
                            output.write(headerData.toString());
                            output.write(recordData.toString());
                            LOG.debug("Data Record {}", recordData.toString()  );
                            output.close();
                        }
                } 
                catch ( IOException e ) {
                    e.printStackTrace();
                }
	}
}
