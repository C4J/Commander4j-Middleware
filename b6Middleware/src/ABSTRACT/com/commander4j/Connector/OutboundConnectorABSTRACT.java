package ABSTRACT.com.commander4j.Connector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.File;

import org.apache.logging.log4j.Logger;

import INTERFACE.com.commander4j.Connector.OutboundConnectorINTERFACE;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.util.JXMLDocument;
import com.commander4j.util.Utility;

public abstract class OutboundConnectorABSTRACT implements OutboundConnectorINTERFACE
{
	private boolean enabled = true;
	private String type = "";
	private String filename = "";
	private String path = "";
	protected Utility util = new Utility();

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorABSTRACT.class));

	private Long outboundConnectorCount = (long) 0;

	protected Document data;

	protected OutboundInterface outint;

	public Long getOutboundConnectorCount()
	{
		return outboundConnectorCount;
	}
	
	public void resetOutBoundConnectorCount()
	{
		outboundConnectorCount = (long) 0;
	}

	public OutboundInterface getOutboundInterface()
	{
		return outint;
	}

	public void processOutboundData(String path, String filename, Document data)
	{
		outboundConnectorCount++;

		setData(data);
		setPath(path);
		setFilename(filename);

		// ==================

		boolean saveFile = true;

		String comparitor = getOutboundInterface().getComparator();

		if (comparitor.equals("") == false)
		{
			// We need to do a comparison
			logger.debug("Comparitor =" + comparitor);

			JXMLDocument document = new JXMLDocument();
			document.setDocument(data);
			logger.debug(util.getStringFromDocument(data));
			
			//document.setDocument(getData());

			// Determine parm1 value
			String parm1value = getOutboundInterface().getCompareParam1();
			String parm1type = getOutboundInterface().getCompareParam1_Type();

			String parm2value =getOutboundInterface().getCompareParam2();
			String parm2type = getOutboundInterface().getCompareParam2_Type();

			
			if (parm1type.equals("xquery"))
			{
				parm1value = document.findXPath(parm1value);

			}

			if (parm2type.equals("xquery"))
			{
				parm2value = util.replaceNullStringwithBlank(document.findXPath(parm2value));

			}

			logger.debug("parm1type 	=" + parm1type);
			logger.debug("parm1value 	=" + parm1value);

			logger.debug("parm2type 	=" + parm2type);
			logger.debug("parm2value 	=" + parm2value);

			logger.debug("comparitor 	=" + comparitor);
			
			logger.debug("Is [" + parm1value + "] "+comparitor+" to [" + parm2value + "]");
			
			if (comparitor.equals("EQUAL"))
			{
				if (parm1value.equals(parm2value) == true)
				{
					logger.debug("SAME - PASS");
					saveFile = true;
				}
				else
				{
					logger.debug("DIFFERENT - FAIL");
					saveFile = false;
				}
			}

			if (comparitor.equals("NOT EQUAL"))
			{				
				if (parm1value.equals(parm2value) == false)
				{

					logger.debug("DIFFERENT - PASS");
					saveFile = true;
				}
				else
				{
					logger.debug("SAME - FAIL");
					saveFile = false;
				}
			}

		}

		if (saveFile == true)
		{
			
			while (connectorSave(getPath(), getOutboundInterface().getPrefix(), getFilename()) == false)
			{
				logger.error("processOutboundData - remote path unavailable [" + getPath(), getOutboundInterface().getPrefix() + getFilename() + "] - waiting 20 seconds before retry");
				com.commander4j.util.JWait.milliSec(20000);
			}

		}

	}

	public OutboundConnectorABSTRACT(String type, OutboundInterface outer)
	{
		this.type = type;
		this.outint = outer;
	}

	public String getPathFilename()
	{
		return getPath() + File.separator + getFilename();
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean getEnabled()
	{
		return this.enabled;
	}

	private void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return this.filename;
	}

	private void setPath(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
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

}
