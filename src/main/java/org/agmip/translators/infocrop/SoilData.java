package org.agmip.translators.infocrop;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoilData {
	
	String soilGlobalData;
	ArrayList<String> layerData;
	private static final Logger LOG = LoggerFactory.getLogger(SoilData.class);

	public SoilData(String globalData)
	{
		//LOG.debug("test");
                layerData = new ArrayList<String>();                
		this.soilGlobalData = globalData;
	}
	
	public void SetGlobalData(String globalData)
	{
		this.soilGlobalData = globalData;
	}
	
	public String GetGlobalData()
	{
		return this.soilGlobalData;
	}	
	
	public void AddLayerData(String layerData)
	{
		this.layerData.add(layerData);
	}
	
	public String GetLayerData(int index)
	{
		String retval = "";
		
		try
		{
		retval = this.layerData.get(index);
		}
		catch( IndexOutOfBoundsException ex )
		{
			//ex.printStackTrace();
			LOG.error("Index {} Out of bound!",index);
		}
		return retval;
	}
	
	public String toString()
	{
		StringBuffer retval= new StringBuffer();
		
		retval.append(soilGlobalData);
		
		for(String currLayer:layerData)
		{
			//retval.append("-----LayerStart--------");
			retval.append(currLayer);
		}
		
		return retval.toString();
	}
	
	public static SoilData validateObject(SoilData currObj)
	{
		SoilData tempObject = currObj;
		
		if(tempObject == null)
			tempObject = new SoilData("");
		
		return tempObject;
	}
}
