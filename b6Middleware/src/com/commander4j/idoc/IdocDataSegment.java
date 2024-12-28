package com.commander4j.idoc;

import java.util.HashMap;
import java.util.Map;

public class IdocDataSegment {
	public String SegmentName;
	public Map<String,String> Properties;
	
	public IdocDataSegment(String name)
	{
		SegmentName = name;
		Properties = new HashMap<String,String>();
	}
	
	
	
	public boolean HasKeyAndValue(String segmentName, String key, String value)
	{
		
		if (!SegmentName.equals(segmentName))
			return false;
		
		if (!Properties.containsKey(key))
			return false;
		
		if (!Properties.get(key).toString().equals(value))
			return false;
		
		return true;
	}
	
	
}
