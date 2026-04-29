package com.commander4j.prop;

import com.commander4j.util.Utility;

public class JPropBoolean
{
	Utility util = new Utility();
		
	public boolean getValue(Object input)
	{
		boolean yesNo = false;
		
		if (input.getClass() == boolean.class)
		{
			yesNo = (boolean) input;
		}
		
		if (input.getClass() == Boolean.class)
		{
			yesNo = (Boolean) input;
		}
		
		if (input.getClass() == String.class)
		{
			String temp = util.replaceNullStringwithBlank((String) input);
			
			if (temp.equals("")) temp = "false";
			if (temp.toUpperCase().equals("N")) temp = "false";
			if (temp.toUpperCase().equals("Y")) temp = "true";
			
			yesNo = Boolean.valueOf(temp);
		}	
		
		if (input.getClass() == Integer.class)
		{
			int temp = (Integer) input;
			
			if (temp == 0)
			{
				yesNo = false;
			}
			else
			{
				yesNo = true;
			}
		}
		
		return yesNo;
	}
	
}
