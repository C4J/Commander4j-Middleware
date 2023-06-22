package ABSTRACT.com.commander4j.Connector;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

import INTERFACE.com.commander4j.Connector.InboundConnectorINTERFACE;

public abstract class InboundConnectorABSTRACT implements InboundConnectorINTERFACE
{
	private boolean enabled = true;
	private String type = "";
	private String filename = "";
	private String destination = "";
	private Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorABSTRACT.class));
	private Long inboundConnectorMessageCount = (long) 0;
	protected Utility util = new Utility();
	protected Document data;

	protected InboundInterface inint;

	public boolean isBinaryFile()
	{
		return inint.isBinaryFile();
	}

	public Boolean backupInboundFile(String fullFilename)
	{
		Boolean result = false;

		Integer retries = Common.retryOpenFileCount;
		Integer count = 0;

		do
		{

			try
			{
				count++;

				destination = Common.logDir + java.io.File.separator + util.getCurrentTimeStampString() + " INPUT_BACKUP_" + getType() + " " + (new File(fullFilename)).getName();

				logger.debug("connectorLoad Backup [" + fullFilename + "] to [" + destination + "]");

				File fromFile = new File(fullFilename);
				File toFile = new File(destination);

				FileUtils.deleteQuietly(toFile);

				FileUtils.copyFile(fromFile, toFile, false);

				fromFile = null;
				toFile = null;

				count = retries;
				result = true;

			}
			catch (Exception ex)
			{

				if (count >= retries)
				{
					logger.error("backupInboundFile unable to backup attempt (" + count + " attempts) for [" + fullFilename + "]");

					logger.error("Error message [" + ex.getMessage() + "]");
					Common.emailqueue.addToQueue(inint.isMapEmailEnabled(), "Error", "Error backing up file", "Error backing up file \\n" + fullFilename + "\\nto\\n" + destination + "\\n[" + ex.getMessage() + "]", "");

				}
				else
				{
					logger.error("backupInboundFile backup attempt (" + count + " of " + retries + ") for [" + fullFilename + "[" + ex.getMessage() + "]", "");

					util.retryDelay();

				}

			}
			finally
			{
				try
				{

				}
				catch (Exception ex)
				{

				}
			}

		}
		while (count < retries);

		return result;
	}

	public Long getInboundConnectorMessageCount()
	{
		return inboundConnectorMessageCount;
	}

	public InboundInterface getInboundInterface()
	{
		return inint;
	}

	public InboundConnectorABSTRACT(String type, InboundInterface inter)
	{
		this.type = type;
		this.inint = inter;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean getEnabled()
	{
		return this.enabled;
	}

	public Boolean processInboundFile(String filename)
	{
		Boolean result = false;

		inboundConnectorMessageCount++;

		setFilename(filename);
		if (connectorLoad(inint.getInputPath() + File.separator + filename))
		{
			if (isBinaryFile() == false)
			{
				if (connectorDelete(filename))
				{
					result = true;
				}
			}
			else
			{
				// Don't delete email input file as we don't pass the contents
				// of the file
				// through the map - only a reference to its filename
				result = true;
			}

		}

		if (result == false)
		{
			com.commander4j.util.JWait.milliSec(1000);
		}

		return result;
	}

	private void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setData(Document data)
	{
		this.data = data;
	}

	public Document getData()
	{
		return data;
	}

	public Element addElement(Document doc, String name, String value)
	{
		Element temp = (Element) doc.createElement(name);
		Text temp_value = doc.createTextNode(value);
		temp.appendChild(temp_value);
		return temp;
	}

	public boolean connectorDelete(String filename)
	{
		Boolean result = false;
		File source = new File(inint.getInputPath() + File.separator + filename);

		try
		{
			logger.debug(inint.getDescription() + " Delete input file :" + source.getAbsolutePath());
			FileDeleteStrategy.NORMAL.delete(source);
			result = true;
		}
		catch (IOException | NullPointerException e)
		{
			logger.error("Error deleting file " + filename + "[" + e.getMessage() + "]");

			Common.emailqueue.addToQueue(inint.isMapEmailEnabled(), "Error", "Error deleting file", "Error deleting file " + filename + "[" + e.getMessage() + "]", "");

			result = false;
		}

		return result;
	}

}
