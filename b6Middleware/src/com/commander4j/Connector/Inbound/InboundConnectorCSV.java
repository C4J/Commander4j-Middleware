package com.commander4j.Connector.Inbound;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
//import com.commander4j.util.JFileIO;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorCSV extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorCSV.class));
	private JPropQuickAccess qa = new JPropQuickAccess();
	
	private LinkedList<String> parseOptions = new LinkedList<String>();
	boolean disableQuotes = false;
	char seperator = ',';
	char quote = '"';

	public InboundConnectorCSV(InboundInterface inter)
	{
		super(Connector_CSV, inter);
	}

	private void getCSVOptions()
	{
		String options = getInboundInterface().getCSVOptions();
		String delimeter = getInboundInterface().getOptionDelimeter();
		parsePattern(options, delimeter);
	}

	private void parsePattern(String pattern, String delim)
	{
		parseOptions.clear();
		delim = "\\" + delim;
		String[] opts = pattern.split(delim);
		for (int x = 0; x < opts.length; x++)
		{
			String two = opts[x];
			String[] three = two.split("=");

			String opt = three[0];
			String val = three[1];

			switch (opt)
			{
			case "separator":
				seperator = val.charAt(0);
				break;
			case "quote":
				if (val.equals("none"))
				{
					disableQuotes = true;
				}
				else
				{
					quote = val.charAt(0);
					disableQuotes = false;
				}
				break;
			default:
				opt = "";
				break;
			}
		}
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{

		logger.debug("connectorLoad [" + fullFilename + "]");
		boolean result = false;

		getCSVOptions();

		if (backupInboundFile(fullFilename))
		{

			Integer delay = qa.getInteger(Common.props, qa.getRootURL()+"//retryOpenFileDelay");
			Integer retries = qa.getInteger(Common.props,qa.getRootURL()+"//retryOpenFileCount");
			Integer count = 0;

			do
			{
				long row = 0;
				long col = 0;
				long maxcol = 0;

				CSVParser csvParser = null;
				CSVReader reader = null;
				String[] nextLine = null;
				Element message = null;

				try
				{
					count++;

					if (disableQuotes)
					{
						csvParser = new CSVParserBuilder().withSeparator(seperator).withIgnoreQuotations(true).build();
						reader = new CSVReaderBuilder(new FileReader(fullFilename)).withCSVParser(csvParser).build();
					}
					else
					{
						csvParser = new CSVParserBuilder().withSeparator(seperator).withIgnoreQuotations(false).withQuoteChar(quote).build();
						reader = new CSVReaderBuilder(new FileReader(fullFilename)).withCSVParser(csvParser).build();
					}

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();

					data = builder.newDocument();

					message = (Element) data.createElement("data");
					message.setAttribute("type", Connector_CSV);

					while ((nextLine = reader.readNext()) != null)
					{
						row++;
						Element xmlrow = (Element) data.createElement("row");
						xmlrow.setAttribute("id", String.valueOf(row));
						xmlrow.setNodeValue(String.valueOf(row));

						for (int x = 0; x < nextLine.length; x++)
						{
							col = x + 1;
							if (col > maxcol)
								maxcol = col;

							Element xmlcol = addElement(data, "col", nextLine[x].trim());
							xmlcol.setAttribute("id", String.valueOf(col));
							xmlcol.setNodeValue(nextLine[x].trim());
							xmlrow.appendChild(xmlcol);

						}
						message.appendChild(xmlrow);
					}

					message.setAttribute("type", Connector_CSV);
					message.setAttribute("cols", String.valueOf(maxcol));
					message.setAttribute("rows", String.valueOf(row));
					message.setAttribute("filename", (new File(fullFilename)).getName());

					data.appendChild(message);

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

						ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
						ept.clear();
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
						ept.addRow(new ExceptionMsg("Retry Delay",String.valueOf(delay)));
						ept.addRow(new ExceptionMsg("Retries",String.valueOf(count)+" of "+String.valueOf(retries)));
						ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
						ept.addRow(new ExceptionMsg("Renamed",fullFilename+ ".error"));
						
						Common.emailqueue.addToQueue(getInboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

						result = false;
					}
					else
					{
						logger.error("connectorLoad " + getType() + " parse attempt (" + count + " of " + retries + ") of [" + fullFilename + " [" + ex.getMessage() + "]", "");

						util.retryDelay(delay);

					}
				}
				finally
				{
					csvParser = null;
					try
					{
						reader.close();
					}
					catch (IOException e)
					{
						// Suppress Error
					}

					reader = null;
					nextLine = null;
					message = null;
				}
			}
			while (count < retries);
		}
		return result;
	}

}
