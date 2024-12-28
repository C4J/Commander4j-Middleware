package com.commander4j.prop;

import java.util.Arrays;

import com.commander4j.util.Utility;

public class JPropString
{
	Utility util = new Utility();

	public String getValue(Object input)
	{
		String stringvalue = "";
		
		if (input.getClass() == String.class)
		{
			stringvalue = (String) input;
		}
		
		if (input.getClass() == String[].class)
		{
			stringvalue = Arrays.toString((String[]) input);
		}
		
		if (input.getClass() == Integer.class)
		{
			stringvalue = input.toString();
		}	
		
		if (input.getClass() == boolean.class)
		{
			stringvalue = String.valueOf((boolean) input);
		}	
		
		if (input.getClass() == Boolean.class)
		{
			stringvalue = String.valueOf((Boolean) input);
		}	
		
		return stringvalue;
	}
}
