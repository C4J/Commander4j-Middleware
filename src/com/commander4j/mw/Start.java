package com.commander4j.mw;

import com.commander4j.sys.Common;
import com.commander4j.util.JWait;
import com.commander4j.util.Utility;

public class Start
{

	public static void main(String[] args)
	{

		int returncode = 0;
		String param = "";

		if (args.length == 0)
		{
			param = "GUI";
		}

		if (args.length == 1)
		{
			param = args[0];
		}

		if (param.equals("") == false)
		{

			Common.runMode = param;

			Common.core.init();
			Common.core.loadMaps();

			switch (param)
			{
			case "Service":

				ShutdownHook shutdownhook = new ShutdownHook();
				shutdownhook.attachShutDownHook();

				if (Common.core.runMaps())
				{
					while (Common.core.isRunning())
					{
						JWait.oneSec();
					}
				}
				System.exit(returncode);
				break;

			case "GUI":

				try
				{
					System.setProperty("apple.laf.useScreenMenuBar", "true");
					
					Utility.setLookandFeel();
					Utility.adjustForLookandFeel();
					
					GUI gui = new GUI();

					gui.setVisible(true);

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				break;

			default:
				// Return error if no valid params specified
				returncode = 1;
				break;
			}
		}

	}

}
