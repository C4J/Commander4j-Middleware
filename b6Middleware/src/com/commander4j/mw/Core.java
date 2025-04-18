package com.commander4j.mw;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.email.EmailHTML;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.thread.EmailThread;
import com.commander4j.thread.LogArchiveThread;
import com.commander4j.thread.StatusThread;
import com.commander4j.util.Utility;

public class Core
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((Core.class));
	public ConfigLoad cfg;
	public ConfigUpdate update;
	public static String appVersion = "6.35";
	public static int configVersion = 2;
	Boolean running = false;
	LogArchiveThread archiveLog;
	StatusThread statusthread;
	EmailThread emailthread;
	Utility util = new Utility();
	JPropQuickAccess qa = new JPropQuickAccess();

	public Boolean isRunning()
	{
		return running;
	}

	public Boolean init()
	{
		Boolean result = true;
		
		logger.debug("Application Initialisation");
		
		util.initLogging("");
		
		logger.debug("*************************");
		logger.debug("**     STARTING        **");
		logger.debug("*************************");
		
		return result;
	}
	
	public Boolean loadMaps()
	{
		Boolean result = true;
		
		update = new ConfigUpdate();
		
		update.upgrade(configVersion);
		
		update = null;
		
		cfg = new ConfigLoad();
		
		cfg.resetErrors();

		cfg.loadMaps(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "config.xml");
		
		logger.debug("*************************");
		logger.debug("**     MAPS LOADED     **");
		logger.debug("*************************");
		
		//check if config.xml has been updated
		
		try
		{
			String activeS = System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "config.xml";
			
			File active = new File(activeS);
			
			long activeLastModified = FileUtils.lastModified(active);
			
			Timestamp ts = new Timestamp(activeLastModified);
			
			System.out.println(util.getISOTimeStampStringFormat(ts).replace("-", "_").replace("T", "_").replace(":", "_"));
			
			String activeB = "config_updated_"+util.getISOTimeStampStringFormat(ts).replace("-", "_").replace("T", "_").replace(":", "_")+".xml";
			
			String backupS = System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "backup" + File.separator + activeB;
			
			Path path = Paths.get(backupS);
			
			File backup = new File(backupS);
			
			if (Files.exists(path)==false)
			{
				FileUtils.copyFile(active, backup, true);
				String messageContent = EmailHTML.header+"<p>Updated config attached</p><br>Updated : "+util.getISOTimeStampStringFormat(ts).replace("T", " ")+"<br><br>"+EmailHTML.footer;
				Common.emailqueue.addToQueue(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"), "Monitor", "Updated config.xml for ["+qa.getString(Common.props, qa.getRootURL()+"//description")+"] "+Core.appVersion+" on "+ util.getClientName(),messageContent, activeS);
			}
			else
			{
				
			}
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		
		return result;	
	}
	
	public Boolean runMaps()
	{
		
		Boolean result = true;
		
		if ((cfg.getMapDirectoryErrorCount() == 0) || (Common.runMode.equals("Service")))
		{

			archiveLog = new LogArchiveThread();
			archiveLog.setName("Log Archiver");
			archiveLog.start();
			
			statusthread = new StatusThread();
			statusthread.setName("Status Thread");
			statusthread.start();
			
			emailthread = new EmailThread();
			emailthread.setName("Email Thread");
			emailthread.start();			

			cfg.startMaps();

			logger.debug("*************************");
			logger.debug("**      STARTED        **");
			logger.debug("*************************");
			
			ExceptionHTML ept = new ExceptionHTML("Middleware Properties","Property","10%","Value","30%");
			ept.clear();
			
		
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("home folder",System.getProperty("user.dir")));
			ept.addRow(new ExceptionMsg("Config Version",qa.getString(Common.props, qa.getRootURL()+"//version")));
			ept.addRow(new ExceptionMsg("logArchiveRetentionDays",qa.getString(Common.props, qa.getRootURL()+"//logArchiveRetentionDays")));
			ept.addRow(new ExceptionMsg("retryOpenFileCount",qa.getString(Common.props, qa.getRootURL()+"//retryOpenFileCount")));
			ept.addRow(new ExceptionMsg("retryOpenFileDelay",qa.getString(Common.props, qa.getRootURL()+"//retryOpenFileDelay")));
			ept.addRow(new ExceptionMsg("enableEmailNotifications",qa.getString(Common.props, qa.getRootURL()+"//enableEmailNotifications")));
			ept.addRow(new ExceptionMsg("statusReportTime",qa.getString(Common.props, qa.getRootURL()+"//statusReportTime")));
			
			Common.emailqueue.addToQueue(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"), "Monitor", "Starting ["+qa.getString(Common.props, qa.getRootURL()+"//description")+"] "+Core.appVersion+" on "+ util.getClientName(),ept.getHTML(), "");
			
			running = true;

		} else
		{
			logger.debug("*************************");
			logger.debug("**      ERRORS         **");
			logger.debug("*************************");

			ExceptionHTML ept = new ExceptionHTML("Error during Middleware startup","Description","10%","Detail","60%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Stage","Startup"));
			
			for (int x = 0; x < cfg.getMapDirectoryErrorCount(); x++)
			{
				ept.addRow(new ExceptionMsg("Exception",cfg.getMapDirectoryErrors().get(x)));
				logger.error(cfg.getMapDirectoryErrors().get(x));
			}
			
			Common.emailqueue.addToQueue(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"), "Error", "Error during Middleware startup",ept.getHTML(), "");

			result = false;
		}
		
		return result;	
	}
	
	public Boolean stopMaps()
	{
		Boolean result = true;

		logger.debug("*************************");
		logger.debug("**      STOPPING       **");
		logger.debug("*************************");

		try
		{
			logger.debug("Shutting down Log File Archiver");
			while (archiveLog.isAlive())
			{
				archiveLog.allDone = true;
				com.commander4j.util.JWait.milliSec(100);
			}
			logger.debug("Log File Archiver terminated");
		} catch (Exception ex1)
		{

		}

		try
		{
			logger.debug("Shutting down Status Thread");
			while (statusthread.isAlive())
			{
				statusthread.allDone = true;
				com.commander4j.util.JWait.milliSec(100);
			}
			logger.debug("Status Thread terminated");
		} catch (Exception ex1)
		{

		}
		
		logger.debug("Shutting down Maps");
		cfg.stopMaps();
		logger.debug("Maps Terminated");

		if (qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"))
		{
			String statistics = EmailHTML.header + cfg.getInterfaceStatistics() + EmailHTML.footer;
			logger.info(statistics);
			Common.emailqueue.addToQueue(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"),"Monitor", "Stopping ["+Common.props.getChildById("description").getValueAsString()+"] "+Core.appVersion+" on "+ util.getClientName(), statistics, "");
		}

		logger.debug("*************************");
		logger.debug("**      STOPPED        **");
		logger.debug("*************************");
		
		try
		{
			Common.emailqueue.processQueue();
			logger.debug("Shutting down email thread");
			while (emailthread.isAlive())
			{
				emailthread.allDone = true;
				com.commander4j.util.JWait.milliSec(100);
			}
			logger.debug("Emailer terminated");
		} catch (Exception ex1)
		{

		}
		
		running = false;

		return result;
	}
}
