package com.commander4j.thread;

import org.apache.logging.log4j.Logger;

import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JArchive;
import com.commander4j.util.JWait;

public class LogArchiveThread extends Thread
{
	public boolean allDone = false;
	Integer counter = 600;

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((LogArchiveThread.class));
	JArchive archiver = new JArchive();
	JPropQuickAccess qa = new JPropQuickAccess();
	Integer ArchiveRetentionDays = 3;
	

	public LogArchiveThread()
	{
		super();

	}

	public void run()
	{
		logger.debug("LogArchiveThread started.");
		
		ArchiveRetentionDays = qa.getInteger(Common.props,qa.getRootURL()+"//logArchiveRetentionDays");
		
		while (true)
		{
			
			JWait.oneSec();

			if (counter >= 600)
			{
				archiver.archiveBackupFiles(qa.getString(Common.props,qa.getRootURL()+"//logDir"), ArchiveRetentionDays);
				counter = 0;
			} else
			{
				counter++;
			}

			if (allDone)
			{
				logger.debug("LogArchiveThread closed.");
				return;
			}

		}
	}
}
