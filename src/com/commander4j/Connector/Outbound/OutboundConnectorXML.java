package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorXML extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorXML.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	public OutboundConnectorXML(OutboundInterface inter)
	{
		super(Connector_XML, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;

		logger.debug("connectorSave [" + fullPath + "]");

		DOMImplementationLS DOMiLS = null;
		FileOutputStream FOS = null;
		LSOutput LSO = null;
		LSSerializer LSS = null;
		String tempFilename = "";
		String finalFilename = "";

		try
		{

			if ((data.getFeature("Core", "3.0") != null) && (data.getFeature("LS", "3.0") != null))
			{
				DOMiLS = (DOMImplementationLS) (data.getImplementation()).getFeature("LS", "3.0");

				LSO = DOMiLS.createLSOutput();

				if (fullPath.endsWith("." + getType().toLowerCase()) == false)
				{
					fullPath = fullPath + "." + getType().toLowerCase();
				}

				tempFilename = fullPath + ".tmp";
				finalFilename = fullPath;

				FOS = new FileOutputStream(tempFilename);

				LSO.setByteStream((OutputStream) FOS);

				LSS = DOMiLS.createLSSerializer();

				LSS.write(getData(), LSO);

				FOS.close();

				FileUtils.deleteQuietly(new File(finalFilename));

				FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

				result = true;
				
				System.out.println(qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMapId(), getOutboundInterface().getId()) + "//description"));
			}
		}
		catch (Exception ex)
		{
			result = false;
			logger.error(ex.getMessage());

			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Host Name", util.getClientName()));
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",getOutboundInterface().getMap().getId()));
			ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getOutboundInterface().getMap().getId())+"//description")));
		
			ept.addRow(new ExceptionMsg("Connector Id",getOutboundInterface().getId()));
			ept.addRow(new ExceptionMsg("Connector Description",qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMapId(), getOutboundInterface().getId()) + "//description")));

			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",fullPath));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Prefix",prefix));
			ept.addRow(new ExceptionMsg("is83GUIDFilenameReqd",String.valueOf(getOutboundInterface().is83GUIDFilenameReqd())));
			ept.addRow(new ExceptionMsg("File Extension",getOutboundInterface().getOutputFileExtension().toLowerCase()));
			ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
			
			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

		}
		finally
		{
			try
			{
				FOS.close();
			}
			catch (Exception e)
			{
				// Suppress exception
			}

			DOMiLS = null;
			LSO = null;
			LSS = null;
			FOS = null;
			tempFilename = null;
			finalFilename = null;
		}

		return result;
	}

}
