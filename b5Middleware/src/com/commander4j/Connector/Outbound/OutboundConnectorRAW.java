package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorRAW extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorRAW.class));

	public OutboundConnectorRAW(OutboundInterface inter)
	{
		super(Connector_RAW, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);
		String fullPath = path + File.separator + filename;

		String tempFilename = fullPath + ".tmp";
		String finalFilename = fullPath;

		FileUtils.deleteQuietly(new File(tempFilename));

		logger.debug("connectorSave [" + fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase() + "]");

		// parsePattern(getOutboundInterface().getOutputPattern());

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		String byteArray64String = util.replaceNullStringwithBlank(document.findXPath("//data/content").trim());
		byte[] returnedBytes = Base64.decodeBase64(byteArray64String);

		FileOutputStream output = null;;
		try
		{

			output = new FileOutputStream(new File(tempFilename));
			IOUtils.write(returnedBytes, output);
			output.flush();
			
			output.close();
			returnedBytes = null;
			byteArray64String = null;
			output = null;

			FileUtils.deleteQuietly(new File(finalFilename));

			FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

			result = true;

		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");
			Common.emailqueue.addToQueue("Error", "Unable to create RAW file [" + tempFilename + "]", ex.getMessage() + "\n\n", "");
		}
		finally
		{
			try
			{
				output.close();
			}
			catch (IOException e)
			{
				//Suppress Error
			}
			output = null;
			fullPath = null;
			tempFilename = null;
			finalFilename = null;
			byteArray64String = null;
			returnedBytes = null;
			document = null;
		}

		return result;
	}

}
