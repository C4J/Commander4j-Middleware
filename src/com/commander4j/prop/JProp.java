package com.commander4j.prop;

import java.util.Arrays;
import java.util.LinkedHashMap;
import com.commander4j.util.Utility;

public class JProp
{
	private String id = "";
	private Object value;
	private JProp parent;
	private LinkedHashMap<String, JProp> children = new LinkedHashMap<String, JProp>();
	Utility util = new Utility();
	JPropString jPropString = new JPropString();
	JPropStringArray jPropStringArray = new JPropStringArray();
	JPropBoolean jPropBoolean = new JPropBoolean();
	JPropInteger jPropInteger = new JPropInteger();
	JPropLong jPropLong = new JPropLong();

	public JProp(String id, Object value)
	{
		setID(id);
		setValue(value);
	}
	
	public JProp(String id)
	{
		setID(id);
		setValue("");
	}

	public void setID(String id)
	{
		this.id = id;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getID()
	{
		return id;
	}

	public Object getValue()
	{
		return value;
	}
	
	public Integer getValueAsInteger()
	{
		return jPropInteger.getValue(value);
	}
	
	public Long getValueAsLong()
	{
		return jPropLong.getValue(value);
	}
	
	public String getValueAsString()
	{
		return util.replaceNullStringwithBlank(jPropString.getValue(value));
	}
	
	public String[] getValueAsStringArray()
	{
		return jPropStringArray.getValue(value);
	}
	
	public boolean getValueAsBoolean()
	{
		return jPropBoolean.getValue(value);
	}

	public JProp getChildById(String id)
	{
		JProp result = null;

		result = children.get(id);

		return result;
	}
	
	public boolean containsChildId(String id)
	{
		boolean result = false;

		result = children.containsKey(id);

		return result;
	}
	
	public LinkedHashMap<String, JProp>  getChildren()
	{
		return children;
	}

	public void addChild(JProp prop)
	{
		children.put(prop.getID(), prop);
		setParent(prop);
	}
	
	private void setParent(JProp prop)
	{
		this.parent = prop;
	}
	
	public JProp getParent()
	{
		return this.parent;
	}

	public String toString()
	{
		String result = "";

		if (value.getClass() == String[].class)
		{
			String c = "("+simpleClassname(value.getClass().toString())+")";
			c = c.replace(";", " Array");
			result = util.padString(getID(),true,30," ") +" "+ util.padString(c,true,15," ")+ " = " + Arrays.toString((String[]) value)+"]";
		}
		else
		{
			String c = "("+simpleClassname(value.getClass().toString())+")";
			result = util.padString(getID(),true,30," ") + " "+util.padString(c,true,15," ") +" = [" + value+"]";	
		}

		return result;
	}
	
	private String simpleClassname(String input)
	{
		String result = "";
		
		String[] temp = input.split("\\.");
		result = temp[temp.length-1];
		
		return result;
	}
}
