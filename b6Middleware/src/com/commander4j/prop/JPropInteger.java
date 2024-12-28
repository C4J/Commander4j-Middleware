package com.commander4j.prop;

import com.commander4j.util.Utility;

public class JPropInteger
{
	Utility util = new Utility();

	
	public Integer getValue( Object input)
	{
		int intvalue = 0;
		
		if (input.getClass() == Integer.class)
		{
			intvalue = (int) input;
		}
		
		if (input.getClass() == String.class)
		{
			String temp = util.replaceNullStringwithBlank((String) input);
			
			if (temp.equals("")) temp = "0";
			
			intvalue = Integer.valueOf(temp);
		}	
		
		return intvalue;
	}
		
}
