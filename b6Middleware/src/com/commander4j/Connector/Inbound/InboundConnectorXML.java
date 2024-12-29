package com.commander4j.Connector.Inbound;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorXML extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorXML.class));
	private JPropQuickAccess qa = new JPropQuickAccess();
	
	public InboundConnectorXML(InboundInterface inter)
	{
		super(Connector_XML, inter);
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{

		logger.debug("connectorLoad [" + fullFilename + "]");
		
		boolean result = false;

		if (backupInboundFile(fullFilename))
		{

			Integer retries = qa.getInteger(Common.props,qa.getRootURL()+"//retryOpenFileCount");
			Integer count = 0;

			do
			{

				DocumentBuilderFactory factory = null;
				DocumentBuilder builder = null;
				try
				{
					count++;
					factory = DocumentBuilderFactory.newInstance();
					builder = factory.newDocumentBuilder();
					data = builder.parse(new File(fullFilename));
					count = retries;
					result = true;

				}
				catch (Exception ex)
				{
					if (count >= retries)
					{
						result = false;

						try
						{
							FileUtils.moveFile(FileUtils.getFile(fullFilename), FileUtils.getFile(fullFilename + ".error"));
						}
						catch (IOException e)
						{

						}

						logger.error("connectorLoad " + getType() + " " + ex.getMessage());

						ExceptionHTML ept = new ExceptionHTML("Error opening file","Description","10%","Detail","30%");
						ept.clear();
						ept.addRow(new ExceptionMsg("Stage","connectorLoad"));
						ept.addRow(new ExceptionMsg("Map Id",getInboundInterface().getMap().getId()));
						ept.addRow(new ExceptionMsg("Connector Id",getInboundInterface().getId()));
						ept.addRow(new ExceptionMsg("Type",getType()));
						ept.addRow(new ExceptionMsg("Source",fullFilename));
						if (getInboundInterface().getXSLTFilename().equals("")==false)
						{
							ept.addRow(new ExceptionMsg("XSLT Path",getInboundInterface().getXSLTPath()));
							ept.addRow(new ExceptionMsg("XSLT File",getInboundInterface().getXSLTFilename()));
						}
						ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
						
						Common.emailqueue.addToQueue(getInboundInterface().getMap().isMapEmailEnabled(), "Error", "Error reading " + getType(),ept.getHTML(), "");
						
					}
					else
					{
						logger.error("connectorLoad " + getType() + " parse attempt (" + count + " of " + retries + ") of [" + fullFilename + " [" + ex.getMessage() + "]", "");

						util.retryDelay();

					}
				}
				finally
				{
					factory = null;
					builder = null;
				}
			}
			while (count < retries);

		}

		return result;
	}

}
