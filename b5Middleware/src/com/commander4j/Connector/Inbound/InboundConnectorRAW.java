package com.commander4j.Connector.Inbound;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.sys.Common;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorRAW extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorRAW.class));

	public InboundConnectorRAW(InboundInterface inter)
	{
		super(Connector_RAW, inter);
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{

		logger.debug("connectorLoad [" + fullFilename + "]");
		boolean result = false;

		if (backupInboundFile(fullFilename))
		{

			// here

			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				data = builder.newDocument();

				Element message = (Element) data.createElement("RAW");

				message.setAttribute("sourceFile", fullFilename);

				data.appendChild(message);

				result = true;

			}
			catch (Exception e)
			{
				logger.error("connectorLoad " + getType() + " " + e.getMessage());

				Common.emailqueue.addToQueue(inint.isMapEmailEnabled(), "Error", "Error processing " + getType(), "connectorLoad " + getType() + " " + e.getMessage() + "\n\n" + fullFilename, "");

			}
			finally
			{

			}
		}
		return result;
	}
}
