package com.commander4j.mw;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

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
					GUI gui = new GUI();

					GraphicsDevice gd = Utility.getGraphicsDevice();

					GraphicsConfiguration gc = gd.getDefaultConfiguration();

					Rectangle screenBounds = gc.getBounds();

					gui.setBounds(screenBounds.x + ((screenBounds.width - gui.getWidth()) / 2), screenBounds.y + ((screenBounds.height - gui.getHeight()) / 2), gui.getWidth(), gui.getHeight());

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
