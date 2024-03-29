package com.commander4j.Connector.Outbound;

import java.io.File;

import org.apache.commons.io.FileUtils;
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

		String[] multiPath = path.split(";");
		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		for (int temp = 0; temp < multiPath.length; temp++)
		{
			String fullPath = multiPath[temp] + File.separator + filename;

			String tempFilename = fullPath + ".tmp";
			String finalFilename = fullPath;

			logger.debug("connectorSave [" + fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase() + "]");

			JXMLDocument document = new JXMLDocument();
			document.setDocument(getData());

			String sourceFile = util.replaceNullStringwithBlank(document.findXPath("//RAW/@sourceFile").trim());

			try
			{
				if (new File(sourceFile).exists())
				{
					FileUtils.deleteQuietly(new File(tempFilename));

					FileUtils.deleteQuietly(new File(finalFilename));

					FileUtils.copyFile(new File(sourceFile), new File(tempFilename), true);

					FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

					if (temp == (multiPath.length - 1))
					{
						FileUtils.deleteQuietly(new File(sourceFile));
					}
				}

				result = true;

			}
			catch (Exception ex)
			{
				logger.error("Message failed to process.");

				Common.emailqueue.addToQueue(outint.isMapEmailEnabled(), "Error", "Unable to copy RAW file [" + tempFilename + "]", ex.getMessage() + "\n\n", "");

			}
			finally
			{

				fullPath = null;
				tempFilename = null;
				finalFilename = null;
				document = null;
				sourceFile = null;
			}

		}

		return result;
	}

}
