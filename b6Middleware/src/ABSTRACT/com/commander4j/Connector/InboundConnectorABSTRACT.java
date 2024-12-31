package ABSTRACT.com.commander4j.Connector;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.RemoteShareChecker;
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
	private JPropQuickAccess qa = new JPropQuickAccess();
	private String errorMessage = "";
	private RemoteShareChecker rsc = new RemoteShareChecker();

	protected InboundInterface inint;

	public boolean isBinaryFile()
	{
		return inint.isBinaryFile();
	}

	public Boolean backupInboundFile(String sourceFile)
	{
		Boolean result = false;

		Integer delay = qa.getInteger(Common.props, qa.getRootURL() + "//retryOpenFileDelay");
		Integer retries = qa.getInteger(Common.props, qa.getRootURL() + "//retryOpenFileCount");
		
		String destinationFile = qa.getString(Common.props, qa.getRootURL() + "//logDir") + java.io.File.separator + util.getCurrentTimeStampString() + " INPUT_BACKUP_" + getType() + " " + (new File(sourceFile)).getName();
	
		int attempt = 0;

		File fromFile = new File(sourceFile);
		File toFile = new File(destinationFile);

		do
		{
			if (rsc.isValidPath(sourceFile))
			{

				if (FileUtils.isRegularFile(toFile, LinkOption.NOFOLLOW_LINKS))
				{
					FileUtils.deleteQuietly(toFile);
				}

				try
				{
					FileUtils.copyFile(fromFile, toFile, true);
					result = true;
				}
				catch (IOException e)
				{
					setErrorMessage(e.getMessage());
				}
			}
			else
			{
				setErrorMessage(rsc.getErrorMessage());
			}

			attempt++;
			
			if (result == false)
			{
				util.retryDelay(delay);
			}

		}
		while ((attempt <= retries) && (result == false));
		
		fromFile = null;
		toFile = null;
		
		if (result == false)
		{

			logger.error("Error message [" + getErrorMessage() + "]");

			ExceptionHTML ept = new ExceptionHTML("Error backing up file", "Description", "10%", "Detail", "30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Map Id", inint.getMap().getId()));
			ept.addRow(new ExceptionMsg("Connector Id", inint.getId()));
			ept.addRow(new ExceptionMsg("Source", sourceFile));
			ept.addRow(new ExceptionMsg("Destination", destination));
			ept.addRow(new ExceptionMsg("Retry Delay", String.valueOf(delay)));
			ept.addRow(new ExceptionMsg("Retries", String.valueOf(retries)));
			ept.addRow(new ExceptionMsg("Exception", getErrorMessage()));

			Common.emailqueue.addToQueue(inint.getMap().isMapEmailEnabled(), "Error", "Error backing up file", ept.getHTML(), "");
		}

		return result;
	}

	private void setErrorMessage(String msg)
	{
		errorMessage = msg;
	}

	public String getErrorMessage()
	{
		return errorMessage;
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

	public void resetInBoundConnectorCount()
	{
		inboundConnectorMessageCount = (long) 0;
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
			logger.debug(qa.getString(Common.props, qa.getMapInputURL(inint.getMapId(), inint.getId()) + "//description") + " Delete input file :" + source.getAbsolutePath());
			FileDeleteStrategy.NORMAL.delete(source);
			result = true;
		}
		catch (IOException | NullPointerException e)
		{
			logger.error("Error deleting file " + filename + "[" + e.getMessage() + "]");

			ExceptionHTML ept = new ExceptionHTML("Error deleting file", "Description", "10%", "Detail", "30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Map Id", inint.getMap().getId()));
			ept.addRow(new ExceptionMsg("Connector Id", inint.getId()));
			ept.addRow(new ExceptionMsg("Source", filename));
			ept.addRow(new ExceptionMsg("Destination", destination));
			ept.addRow(new ExceptionMsg("Exception", e.getMessage()));

			Common.emailqueue.addToQueue(inint.getMap().isMapEmailEnabled(), "Error", "Error deleting file", ept.getHTML(), "");

			result = false;
		}

		return result;
	}

}
