package com.commander4j.mw;

import com.commander4j.sys.Common;

public class ShutdownHook
{
	public void attachShutDownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				Common.core.stopMaps();
			}
		});
		
	}

}
