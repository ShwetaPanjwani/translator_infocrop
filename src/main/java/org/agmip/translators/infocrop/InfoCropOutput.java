package org.agmip.translators.infocrop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.agmip.core.types.TranslatorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoCropOutput implements TranslatorOutput {
	private static final Logger log = LoggerFactory.getLogger(InfoCropOutput.class);
    public void writeFile(String filePath, Map input) {
    	try {
            long start = System.currentTimeMillis();	
            HashMap<String, Object> results = (HashMap<String, Object> )input;
            //CleanDirectory(filePath);
            (new WeatherProcessor()).ProcessWeatherData(filePath,results);
            HashMap<String, Object> soilDataMap = (HashMap<String, Object>)(new SoilProcessor()).ProcessSoilData(filePath,results);
            System.out.println("soilDataMap"+soilDataMap);
            (new ExperimentDataProcessor()).ProcessExperimentData(filePath,results,soilDataMap);
            long end = System.currentTimeMillis();
            log.info("Process Completed Exec Time: {} secs",  ((end-start)/1000) );
    	} catch (Exception ex) {
    		log.error(ex.toString());
    		ex.printStackTrace();
    	}
    }
    
    public void CleanDirectory(String filePath)
    {
    	try
    	{
    		
                File directory = new File(filePath);
    		File[] files = directory.listFiles();
    		for( File file: files )
    		{
    			log.info("Cleaning file {}", file.getAbsolutePath());
    			file.delete();
    		}
    	}
    	catch ( Exception ex )
    	{
    		
    	}
    	
    }
    
}
