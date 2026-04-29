package com.commander4j.Connector.Inbound;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorEmail extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorEmail.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	public InboundConnectorEmail(InboundInterface inter)
	{
		super(Connector_EMAIL, inter);
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{

		logger.debug("connectorLoad [" + fullFilename + "]");
		boolean result = false;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Element message = null;
		Element content = null;

		if (backupInboundFile(fullFilename))
		{
			try
			{
				factory = DocumentBuilderFactory.newInstance();
				builder = factory.newDocumentBuilder();

				data = builder.newDocument();

				message = (Element) data.createElement("email");

				content = (Element) data.createElement("inputFilename");

				content.setTextContent(fullFilename);

				message.appendChild(content);

				data.appendChild(message);

				result = true;

			}
			catch (Exception e)
			{
				logger.error("connectorLoad " + getType() + " " + e.getMessage());
				
				ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
				ept.clear();
				ept.addRow(new ExceptionMsg("Host Name", util.getClientName()));
				ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
				ept.addRow(new ExceptionMsg("Stage","connectorLoad"));
				ept.addRow(new ExceptionMsg("Map Id",getInboundInterface().getMap().getId()));
				ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getInboundInterface().getMap().getId())+"//description")));
				ept.addRow(new ExceptionMsg("Connector Id",getInboundInterface().getId()));
				ept.addRow(new ExceptionMsg("Connector Description",qa.getString(Common.props, qa.getMapInputURL(getInboundInterface().getMap().getId(), getInboundInterface().getId()))+"//description"));
				ept.addRow(new ExceptionMsg("Type",getType()));
				ept.addRow(new ExceptionMsg("Source",fullFilename));
				if (getInboundInterface().getXSLTFilename().equals("")==false)
				{
					ept.addRow(new ExceptionMsg("XSLT Path",getInboundInterface().getXSLTPath()));
					ept.addRow(new ExceptionMsg("XSLT File",getInboundInterface().getXSLTFilename()));
				}
				ept.addRow(new ExceptionMsg("Exception",e.getMessage()));
				
				Common.emailqueue.addToQueue(getInboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

			}
			finally
			{
				builder = null;
				factory = null;
				message = null;
				content = null;
			}
		}
		return result;
	}
}
