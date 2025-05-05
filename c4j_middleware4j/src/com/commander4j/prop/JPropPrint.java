package com.commander4j.prop;

import java.util.concurrent.ConcurrentHashMap;

import com.commander4j.util.Utility;

public class JPropPrint
{

	Utility util = new Utility();
	int level = 0;

	public void print(JProp root, int level)
	{
		level++;
		String spacer = util.padString(level * 4, " ");

		if (root.getChildren().size() > 0)
		{

				System.out.println(spacer + "{" + root.getID() + "}");
		}
		else
		{

				System.out.println(spacer + root);
		}

		for (ConcurrentHashMap.Entry<String, JProp> entry : root.getChildren().entrySet())
		{

			@SuppressWarnings("unused")
			String key = entry.getKey();
			JProp value = entry.getValue();

			JPropPrint pp = new JPropPrint();
			pp.print(value, level);
		}

	}

}
