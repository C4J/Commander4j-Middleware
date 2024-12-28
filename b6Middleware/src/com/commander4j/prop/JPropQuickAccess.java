package com.commander4j.prop;

import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

public class JPropQuickAccess
{
	Utility utils = new Utility();
	JPropToString ts = new JPropToString();

	public void setValue(JProp root, String url, Object value)
	{

		String[] segments = url.split("//");

		int parts = segments.length;

		JProp node = root;

		String segmentId = "";
		boolean created = false;

		for (int x = 1; x < parts; x++)
		{
			segmentId = segments[x];

			if (node.containsChildId(segmentId) == false)
			{
				node.addChild(new JProp(segmentId, value));
				created = true;

				System.out.println("Create " + url + " = [" + ts.toString(value) + "]");
			}

			node = node.getChildById(segmentId);
		}

		if (node.getID().equals(segmentId))
		{
			if (created == false)
			{
				node.setValue(value);

				System.out.println("Update " + url + " = [" + ts.toString(value) + "]");
			}
		}
	}

	public void setValue(JProp root, String url, Object value, Object defaultValue)
	{

		try
		{
			if ((value != null) && (defaultValue != null))
			{
				if (value.getClass() == defaultValue.getClass())
				{
					// If String
					if (value.getClass() == String.class)
					{
						if (((String) value).equals("") && (((String) defaultValue).equals("") == false))
						{
							value = defaultValue;
						}
					}

					// If String Array
					if (value.getClass() == String[].class)
					{
						if ((((String[]) value).length == 0) && (((String[]) defaultValue).length > 0))
						{
							value = defaultValue;
						}
					}

					// If Integer
					if (value.getClass() == Integer.class)
					{
						if (((Integer) value == 0) && ((Integer) defaultValue != 0))
						{
							value = defaultValue;
						}
					}

					// If Long
					if (value.getClass() == Long.class)
					{
						if (((Long) value == 0) && ((Long) defaultValue != 0))
						{
							value = defaultValue;
						}
					}

				}
			}

		}
		catch (Exception ex)
		{

		}

		setValue(root, url, value);
	}

	public JProp getNode(JProp root, String url)
	{
		JProp result;

		String[] segments = url.split("//");

		int parts = segments.length;

		JProp node = root;

		for (int x = 1; x < parts; x++)
		{
			String segmentId = segments[x];
			if (node.containsChildId(segmentId))
			{
				node = node.getChildById(segmentId);
			}
			else
			{
				node = new JProp("");
				break;
			}
		}

		result = node;

		return result;
	}

	public String getString(JProp root, String url)
	{
		String result = (getNode(root, url)).getValueAsString();

		return result;
	}

	public String[] getStringArray(JProp root, String url)
	{
		String[] result = (getNode(root, url)).getValueAsStringArray();

		return result;
	}

	public Integer getInteger(JProp root, String url)
	{
		Integer result = (getNode(root, url)).getValueAsInteger();

		return result;
	}

	public Long getLong(JProp root, String url)
	{
		Long result = (getNode(root, url)).getValueAsLong();

		return result;
	}

	public boolean getBoolean(JProp root, String url)
	{
		boolean result = (getNode(root, url)).getValueAsBoolean();

		return result;
	}

	public String getRootURL()
	{
		return Common.pathRoot;
	}

	public String getMapURL(String mapId)
	{
		return Common.pathMap.replace("{mapId}", mapId);
	}

	public String getMapInputURL(String mapId, String inputId)
	{
		return Common.pathMapInput.replace("{mapId}", mapId).replace("{inId}", inputId);
	}

	public String getMapOutputURL(String mapId, String outputId)
	{
		return Common.pathMapOutput.replace("{mapId}", mapId).replace("{outId}", outputId);
	}

}
