package com.commander4j.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.logging.log4j.Logger;

public class JArchive
{
	Utility util = new Utility();
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((JArchive.class));

	public int archiveBackupFiles(String path, int daysToKeep)
	{
		int result = 0;

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1 * daysToKeep);
		Date cutoffDate = cal.getTime();

		File directory = new File(path);

		result = result + deleteFiles(directory, new AgeFileFilter(cutoffDate));

		return result;
	}

	public int deleteFiles(File directory, FileFilter fileFilter)
	{
		int count = 0;
		File[] files = directory.listFiles(fileFilter);

		for (File file : files)
		{
			if (file.getName().equals("dirClean.ok") == false)
			{
				try
				{
					Date lastMod = new Date(file.lastModified());
					logger.debug("Removing log file [" + file.getName() + ", Date: " + lastMod + "] from [" + directory.getName() + "]");
					file.delete();
					count++;
				}
				catch (Exception ex)
				{
					logger.error(ex.getMessage());
				}
			}
		}

		return count;
	}

}
