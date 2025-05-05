package com.commander4j.Connector.Outbound;

import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.exception.OutboundPrinterQueueException;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.prop.JPropString;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorPDF_PRINT extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorPDF_PRINT.class));
	JPropQuickAccess qa = new JPropQuickAccess();
	JPropString jPropString = new JPropString();

	public OutboundConnectorPDF_PRINT(OutboundInterface inter)
	{
		super(Connector_PDF_PRINT, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		String fullPath = path + File.separator + filename;

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		String inputFilename = util.replaceNullStringwithBlank(document.findXPath("//pdf_print/inputFilename").trim());

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

		PrintService service = null;
		PDDocument pdfdocument = null;
		PrinterJob printerjob = null;

		try
		{
			FileUtils.deleteQuietly(new File(outputFilename));

			FileUtils.moveFileToDirectory(new File(inputFilename), new File(path), false);

			logger.debug("Printing PDF file called :" + outputFilename);

			if (getOutboundInterface().getQueueName().equals(""))
			{
				logger.debug("No print queue specified - getting default queue.");

				service = PrintServiceLookup.lookupDefaultPrintService();

				if (service != null)
				{
					qa.setValue(Common.props,qa.getMapOutputURL(getOutboundInterface().getMapId(), getOutboundInterface().getId())+"//print//queueName", jPropString.getValue(service.getName()));
					logger.debug("Using default print queue : " + getOutboundInterface().getQueueName());

				}
				else
				{
					logger.debug("No default print service found");
				}
			}
			else
			{
				logger.debug(("Requested Queue Name    : " + getOutboundInterface().getQueueName()));
			}

			if (getOutboundInterface().getQueueName().equals("") == false)
			{

				pdfdocument = Loader.loadPDF(new File(outputFilename));

				printerjob = PrinterJob.getPrinterJob();

				String validQueueNames = "";

				for (PrintService printService : PrinterJob.lookupPrintServices())
				{
					logger.debug("Found printer " + printService);

					validQueueNames = validQueueNames + "[" + printService.getName() + "]\n";

					if (getOutboundInterface().getQueueName().equalsIgnoreCase(printService.getName()))
					{
						logger.debug("Found specified printer queue. " + getOutboundInterface().getQueueName());
						printerjob.setPrintService(printService);
						printerjob.setPageable(new PDFPageable(pdfdocument));
						printerjob.print();
						FileUtils.deleteQuietly(new File(outputFilename));
						pdfdocument.close();
						result = true;
						break;
					}
				}

				if (result == false)
				{
					throw new OutboundPrinterQueueException("\n\nValid queues are \n\n" + validQueueNames);
				}

			}
			else
			{
				logger.debug("Unable to find print queue ");
			}

		}
		catch (Exception e)
		{
			logger.error("connectorLoad " + getType() + " " + e.getMessage());

			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",getOutboundInterface().getMap().getId()));
			ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getOutboundInterface().getMap().getId())+"//description")));
			ept.addRow(new ExceptionMsg("Connector Id",getOutboundInterface().getId()));
			ept.addRow(new ExceptionMsg("Connector Description",qa.getString(Common.props, qa.getMapInputURL(getOutboundInterface().getMap().getId(), getOutboundInterface().getId()))+"//description"));
			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",fullPath));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Exception",e.getMessage()));
			
			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");
		}
		finally
		{
			document = null;
			inputFilename = null;
			outputFilename = null;
			service = null;
			pdfdocument = null;
			printerjob = null;
		}

		return result;
	}

}
