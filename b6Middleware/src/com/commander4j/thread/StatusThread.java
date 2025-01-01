package com.commander4j.thread;

import org.apache.logging.log4j.Logger;

import com.commander4j.email.EmailHTML;
import com.commander4j.mw.StartMain;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JWait;
import com.commander4j.util.Utility;

public class StatusThread extends Thread
{
	public boolean allDone = false;
	String currentDateTime = "";
	String currentTime = "";
	String currentDate = "";
	String lastRunDate = "";
	int mb = 1024 * 1024;
	Utility util = new Utility();
	Runtime runtime = Runtime.getRuntime();
	JPropQuickAccess qa = new JPropQuickAccess();
	String statusReportTime = "";
	
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((StatusThread.class));

	public StatusThread()
	{
		super();

	}

	public void run()
	{
		logger.debug("StatusThread started.");
		
		statusReportTime = qa.getString(Common.props,qa.getRootURL()+"//statusReportTime").substring(0, 5);
		
		while (true)
		{

			JWait.oneSec();
			currentDateTime = util.getDateTimeString("yyyy-MM-dd HH:mm:ss");
			currentDate = currentDateTime.substring(0, 10);
			currentTime = currentDateTime.substring(11, 19);
			
			if (currentTime.substring(0, 5).equals(statusReportTime))
			{
				if (currentDate.equals(lastRunDate) == false)
				{
					lastRunDate = currentDate;
					
					String description = qa.getString(Common.props, qa.getRootURL()+"//description");
					
					String report = EmailHTML.header + Common.smw.cfg.getInterfaceStatistics() + EmailHTML.footer;

					Common.smw.cfg.resetInterfaceStatistics();
					
					
					report = report + "<br>\n"
							+ "<div id=\"garbage\" >\n"
							+ "<table border=\"3\">\n"
							+ "	<thead>\n"
							+ "  <caption>Before GC</caption>\n"
							+ "	 <tr>\n"
							+ "		<th>Memory</th>\n"
							+ "		<th>MB</th>\n"
							+ "	 </tr>\n"
							+ "	</thead>\n"
							+ " <tbody>\n"
							+ "	 <tr>\n"
							+ "	  <td>Used Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ (runtime.totalMemory() - runtime.freeMemory()) / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "  <tr>\n"
							+ "	  <td>Free Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.freeMemory() / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "	 <tr>\n"
							+ "	  <td>Total Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.totalMemory() / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "  <tr>\n"
							+ "	  <td>Max Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.maxMemory() / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "	</tbody>\n"
							+ "</table> \n"
							+ "<br>\n";
					
							System.gc();
							
					report = report + "<table border=\"3\">\n"
							+ " <thead>\n"
							+ "   <caption>After GC</caption>\n"
							+ "   <tr>\n"
							+ "	   <th>Memory</th>\n"
							+ "    <th>MB</th>\n"
							+ "	  </tr>\n"
							+ "	</thead>\n"
							+ "	<tbody>\n"
							+ "	 <tr>\n"
							+ "	  <td>Used Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ (runtime.totalMemory() - runtime.freeMemory()) / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "  <tr>\n"
							+ "	  <td>Free Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.freeMemory() / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "	 <tr>\n"
							+ "	  <td>Total Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.totalMemory() / mb + "mb</td>\n"
							+ "	 </tr>\n"
							+ "  <tr>\n"
							+ "	  <td>Max Memory</td>\n"
							+ "	  <td style=\"width:20%; text-align: right\">"+ runtime.maxMemory() / mb + "mb</td>\n"
							+ "  </tr>\n"
							+ " </tbody>\n"
							+ "</table> \n"
							+ "</div>";
					
					Common.emailqueue.addToQueue(true,"Monitor", "Statistics ["+description+"] "+StartMain.appVersion+" on "+ util.getClientName(), report, "");
					
					logger.debug(report);
				}
			}

			if (allDone)
			{
				logger.debug("StatusThread closed.");
				return;
			}

		}
	}
}
