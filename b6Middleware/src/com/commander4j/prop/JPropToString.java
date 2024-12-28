package com.commander4j.prop;

import java.util.Arrays;

public class JPropToString
{

	public String toString(Object value)
	{
		String result = "";
		
		if (value.getClass() == String[].class)
		{
			result = Arrays.toString((String[]) value);
		}
		else
		{
			result = String.valueOf(value);
		}
		
		return result;
	}
}
