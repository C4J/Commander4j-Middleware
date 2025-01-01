package com.commander4j.Connector.Outbound;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.email.EmailHTML;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorEmail extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorEmail.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	public OutboundConnectorEmail(OutboundInterface inter)
	{
		super(Connector_EMAIL, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		String inputFilename = util.replaceNullStringwithBlank(document.findXPath("//email/inputFilename").trim());

		String outputFilename = path;

		if (outputFilename.endsWith(File.separator))
		{
			outputFilename = outputFilename + filename;
		}
		else
		{
			outputFilename = outputFilename + File.separator + filename;
		}

		logger.debug("connectorLoad " + getType() + " inputFilename" + inputFilename);
		logger.debug("connectorLoad " + getType() + " outputFilename" + outputFilename);

		try
		{
			FileUtils.deleteQuietly(new File(outputFilename));

			FileUtils.moveFileToDirectory(new File(inputFilename), new File(path), false);

			String addresses = getOutboundInterface().getEmailListID();
			String subject = getOutboundInterface().getEmailSubject();

			String messageContent = EmailHTML.header+"<p>"+getOutboundInterface().getEmailMessage()+"</p><br><br>"+EmailHTML.footer;

			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), addresses, subject, messageContent, outputFilename);

			result = true;

		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");
			
			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",getOutboundInterface().getMap().getId()));
			ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getOutboundInterface().getMap().getId())+"//description")));
			ept.addRow(new ExceptionMsg("Connector Id",getOutboundInterface().getId()));
			ept.addRow(new ExceptionMsg("Connector Description",qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMap().getId(), getOutboundInterface().getId()))+"//description"));
			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",filename));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
			
			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");
		}
		finally
		{
			document = null;
			inputFilename = null;
			outputFilename = null;
		}

		return result;
	}

}
