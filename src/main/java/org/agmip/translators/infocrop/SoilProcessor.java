package org.agmip.translators.infocrop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import org.agmip.common.Functions;
import org.agmip.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.Math;
public class SoilProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(SoilProcessor.class);
	private String outputDir;
	HashMap<String, Object> soilDataMap; 
	private final String NEW_LINE = "\r\n";
	
	
	public HashMap<String, Object> ProcessSoilData(String outputDir, HashMap<String, Object> results) throws IOException
	{
		if (!outputDir.endsWith(File.separator)) {
                    outputDir += File.separator;
                 }
                this.outputDir = outputDir;			
		this.soilDataMap = new HashMap<String, Object>();
                
		ArrayList<HashMap<String, Object>> soils = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(results, "soils", new HashMap<String, Object>());
		for(HashMap<String, Object> soil : soils) 
                { 
                    String soil_id = MapUtil.getValueOr(soil, "soil_id", "-9999");
                    double slope = Double.valueOf(MapUtil.getValueOr(soil, "sl_slope", "-9999"));
                    generateSoilLayer(soil,soil_id,slope); 
                }		
		LOG.info( "{}",soilDataMap );		
		return soilDataMap;
	}
	
	
	public void generateSoilLayer(HashMap<String, Object> soil, String soil_id,double slope)
	{
		HashMap<String, Object> recordData,layerData = new HashMap<String, Object>();
		int counter = 0;
                double PHSum=0.0,ECSum=0.0;
                ArrayList<HashMap<String, Object>> Layer1 = new ArrayList<HashMap<String, Object>>();
                ArrayList<HashMap<String, Object>> Layer2 = new ArrayList<HashMap<String, Object>>();
                ArrayList<HashMap<String, Object>> Layer3 = new ArrayList<HashMap<String, Object>>();
                String fileName ="";
		ArrayList<HashMap<String, Object>> soilLayers = (ArrayList<HashMap<String, Object>>) MapUtil.getObjectOr(soil, "soilLayer", new ArrayList<HashMap<String, Object>>());
                String soilData;
               StringBuffer retval= new StringBuffer();
               SoilData soilDataObj =new SoilData(soil_id);
              // LOG.debug("test");
		try {
                                for(HashMap<String, Object> soilLayer : soilLayers) 
                                {
                                        counter++;
                                        PHSum+=Double.valueOf(MapUtil.getValueOr(soilLayer, "slphw", "-9999"));
                                        ECSum+=Double.valueOf(MapUtil.getValueOr(soilLayer, "slec", "-9999"));
                                        if (Integer.valueOf(MapUtil.getValueOr(soilLayer, "sllb", "-9999"))<=30)
                                        {
                                             Layer1.add(soilLayer);
                                        }
                                        if (Integer.valueOf(MapUtil.getValueOr(soilLayer, "sllb", "-9999"))>30 && Integer.valueOf(MapUtil.getValueOr(soilLayer, "sllb", "-9999"))<=60)
                                        {
                                             Layer2.add(soilLayer);
                                        }

                                        if (Integer.valueOf(MapUtil.getValueOr(soilLayer, "sllb", "-9999"))>60 && Integer.valueOf(MapUtil.getValueOr(soilLayer, "sllb", "-9999"))<=150)
                                        {
                                             Layer3.add(soilLayer);
                                        }
                                }
                                PHSum=PHSum/counter;
                                ECSum=ECSum/counter;
                                if(slope!=-9999.0){
                                    layerData.put("SLOPE",slope);
                                }
                                if(PHSum!=-9999.0){
                                    layerData.put("PHSOL",PHSum);
                                }
                                if(ECSum!=-9999.0){
                                     layerData.put("EC1",ECSum);
                                }
                                layerData.put("TKL1",30);
                                if(calculateAverage(Layer1,"slsil")!=-9999.0){
                                    layerData.put("SILT1",String.format("%.2f",calculateAverage(Layer1,"slsil")));
                                }
                                if(calculateAverage(Layer1,"slcly")!=-9999.0){
                                    layerData.put("CLAY1",String.format("%.2f",calculateAverage(Layer1,"slcly")));
                                }
                                if(calculateAverage(Layer1,"slsat")!=-9999.0){
                                    layerData.put("WCSTM1",String.format("%.2f",calculateAverage(Layer1,"slsat")));
                                }
                                if(calculateAverage(Layer1,"sldul")!=-9999.0){
                                    layerData.put("WCFCM1",String.format("%.2f",calculateAverage(Layer1,"sldul")));
                                }
                                if(calculateAverage(Layer1,"slwp")!=-9999.0){
                                    layerData.put("WCFPM1",String.format("%.2f",calculateAverage(Layer1,"slwp")));
                                }
                                if(calculateAverage(Layer1,"SKSAT")!=-9999.0){
                                    layerData.put("KSATM1",String.format("%.2f",calculateAverage(Layer1,"SKSAT")));
                                }
                                if(calculateAverage(Layer1,"slbdm")!=-9999.0){
                                    layerData.put("BDM1",String.format("%.2f",calculateAverage(Layer1,"slbdm")));
                                }
                                if(calculateAverage(Layer1,"sloc")!=-9999.0){
                                    layerData.put("SOC1",String.format("%.2f",calculateAverage(Layer1,"sloc")));
                                }
                                
                                layerData.put("TKL2",60);
                                if(calculateAverage(Layer2,"slsil")!=-9999.0){
                                    layerData.put("SILT2",String.format("%.2f",calculateAverage(Layer2,"slsil")));
                                }
                                if(calculateAverage(Layer2,"slcly")!=-9999.0){
                                    layerData.put("CLAY2",String.format("%.2f",calculateAverage(Layer2,"slcly")));
                                }
                                if(calculateAverage(Layer2,"slsat")!=-9999.0){
                                    layerData.put("WCSTM2",String.format("%.2f",calculateAverage(Layer2,"slsat")));
                                }
                                if(calculateAverage(Layer2,"sldul")!=-9999.0){
                                    layerData.put("WCFCM2",String.format("%.2f",calculateAverage(Layer2,"sldul")));
                                }
                                if(calculateAverage(Layer2,"slwp")!=-9999.0){
                                    layerData.put("WCFPM2",String.format("%.2f",calculateAverage(Layer2,"slwp")));
                                }
                                if(calculateAverage(Layer2,"SKSAT")!=-9999.0){
                                    layerData.put("KSATM2",String.format("%.2f",calculateAverage(Layer2,"SKSAT")));
                                }
                                if(calculateAverage(Layer2,"slbdm")!=-9999.0){
                                    layerData.put("BDM2",String.format("%.2f",calculateAverage(Layer2,"slbdm")));
                                }
                                if(calculateAverage(Layer2,"sloc")!=-9999.0){
                                    layerData.put("SOC2",String.format("%.2f",calculateAverage(Layer2,"sloc")));
                                }
                               
                                layerData.put("TKL3",150);
                                if(calculateAverage(Layer3,"slsil")!=-9999.0){
                                    layerData.put("SILT3",String.format("%.2f",calculateAverage(Layer3,"slsil")));
                                }
                                if(calculateAverage(Layer3,"slcly")!=-9999.0){
                                    layerData.put("CLAY3",String.format("%.2f",calculateAverage(Layer3,"slcly")));
                                }
                                if(calculateAverage(Layer3,"slsat")!=-9999.0){
                                    layerData.put("WCSTM3",String.format("%.2f",calculateAverage(Layer3,"slsat")));
                                }
                                if(calculateAverage(Layer3,"sldul")!=-9999.0){
                                    layerData.put("WCFCM3",String.format("%.2f",calculateAverage(Layer3,"sldul")));
                                }
                                if(calculateAverage(Layer3,"slwp")!=-9999.0){
                                    layerData.put("WCFPM3",String.format("%.2f",calculateAverage(Layer3,"slwp")));
                                }
                                if(calculateAverage(Layer3,"SKSAT")!=-9999.0){
                                    layerData.put("KSATM3",String.format("%.2f",calculateAverage(Layer3,"SKSAT")));
                                }
                                if(calculateAverage(Layer3,"SKSAT")!=-9999.0){
                                    layerData.put("BDM3",String.format("%.2f",calculateAverage(Layer3,"slbdm")));
                                }
                                if(calculateAverage(Layer3,"sloc")!=-9999.0){
                                    layerData.put("SOC3",String.format("%.2f",calculateAverage(Layer3,"sloc")));
                                }
                               // LOG.debug("soilid---"+soil_id+"layerData---"+layerData);
                                
                                //output.write(layerData.toString());  
                                soilDataMap.put(soil_id, layerData);
                                soilDataObj.AddLayerData(soilDataMap.toString());
                                //output.close();
                                soilDataObj.SetGlobalData(soilDataMap.toString());	
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
	}
        
        public double calculateAverage(ArrayList<HashMap<String, Object>> layer,String key) {        
            int n=0;
            double keySum=0.0;
            for(HashMap<String, Object> soilLayer : layer)
            {
                keySum+=Double.valueOf(MapUtil.getValueOr(soilLayer, key, "-9999"));
                n++;
            }
            keySum=keySum/n;
            return keySum;
    }	
}
