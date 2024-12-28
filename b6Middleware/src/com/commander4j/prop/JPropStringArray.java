package com.commander4j.prop;

import com.commander4j.util.Utility;

public class JPropStringArray
{
	Utility util = new Utility();

	public String[] getValue(Object input)
	{
		String[] stringvalue;
		
		if (input.getClass() == String[].class)
		{
			stringvalue = (String[]) input;
		}
		else
		{
			stringvalue = new String[0];
		}
			
		return stringvalue;
	}
	
}
