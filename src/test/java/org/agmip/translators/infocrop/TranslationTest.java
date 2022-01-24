package org.agmip.translators.infocrop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.agmip.translators.infocrop.ExperimentDataProcessor;
import org.agmip.translators.infocrop.InfoCropInput;
import org.agmip.translators.infocrop.InfoCropOutput;
import org.agmip.translators.infocrop.SoilData;
import org.agmip.translators.infocrop.SoilProcessor;
import org.agmip.translators.infocrop.WeatherProcessor;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.agmip.common.*;


import static org.junit.Assert.*;

public class TranslationTest {
	private static final Logger LOG = LoggerFactory.getLogger(TranslationTest.class);
	@Test
	public void firstTest() throws IOException {
	
		URL resource = this.getClass().getResource("/mach_fast.json");
		HashMap<String, Object> results = JSONAdapter.fromJSONFile(resource.getFile());
		(new InfoCropOutput()).writeFile("data"+File.separator, results);
		assertTrue(true);
			
		}
		//LOG.info(weathers.toString());
		
	
	
}
	 
