package com.commander4j.Connector.Outbound;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorEmail extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorEmail.class));

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
			String message = getOutboundInterface().getEmailMessage() + "\n\n";

			Common.emailqueue.addToQueue(addresses, subject, message, outputFilename);

			result = true;

		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");
			Common.emailqueue.addToQueue("Error", "Unable to Email file [" + inputFilename + "]", ex.getMessage() + "\n\n", "");
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
