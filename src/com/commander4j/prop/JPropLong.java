package com.commander4j.prop;

import com.commander4j.util.Utility;

public class JPropLong
{
	Utility util = new Utility();

	public Long getValue( Object input)
	{
		Long intvalue = (long) 0;
		
		if (input.getClass() == Long.class)
		{
			intvalue = (Long) input;
		}
		
		if (input.getClass() == String.class)
		{
			String temp = util.replaceNullStringwithBlank((String) input);
			
			if (temp.equals("")) temp = "0";
			
			intvalue = Long.valueOf(temp);
		}	
		
		return intvalue;
	}
	
	
}
