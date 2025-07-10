package com.commander4j.Connector.Inbound;

import java.io.BufferedReader;
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
import com.commander4j.sys.FixedASCIIColumns;
import com.commander4j.sys.FixedASCIIData;
//import com.commander4j.util.JFileIO;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorASCII extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorASCII.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	private LinkedList<FixedASCIIColumns> parseCols = new LinkedList<FixedASCIIColumns>();

	public InboundConnectorASCII(InboundInterface inter)
	{
		super(Connector_ASCII, inter);
	}

	private int getPatternColumnCount()
	{
		return parseCols.size();
	}

	private void parsePattern(String pattern)
	{
		parseCols.clear();
		String[] one = pattern.split(",");
		for (int x = 0; x < one.length; x++)
		{
			String two = one[x];
			String[] three = two.split("-");
			int start = Integer.valueOf(three[0]);
			int end = Integer.valueOf(three[1]);
			FixedASCIIColumns col = new FixedASCIIColumns();
			col.position = x;
			col.start = start;
			col.end = end;
			parseCols.add(col);
		}
	}

	private LinkedList<FixedASCIIData> getASCIIColumnData(String line)
	{
		LinkedList<FixedASCIIData> result = new LinkedList<FixedASCIIData>();

		if (parseCols.size() > 0)
		{
			if (line.length() > 0)
			{
				for (int x = 0; x < parseCols.size(); x++)
				{
					int firstcol = parseCols.get(x).start;
					int lastcol = parseCols.get(x).end;

					FixedASCIIData data = new FixedASCIIData();

					data.columnId = x + 1;
					data.columnData = line.substring(firstcol - 1, lastcol);
					result.addLast(data);
				}
			}
		}

		return result;
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{
		long row = 0;

		parsePattern(getInboundInterface().getInputPattern());

		logger.debug("connectorLoad [" + fullFilename + "]");
		boolean result = false;

		// backupInboundFile(fullFilename);

		if (backupInboundFile(fullFilename))
		{

			Integer delay = qa.getInteger(Common.props, qa.getRootURL()+"//retryOpenFileDelay");
			Integer retries = qa.getInteger(Common.props,qa.getRootURL()+"//retryOpenFileCount");
			Integer count = 0;

			do
			{

				String line = null;
				FileReader fileReader = null;
				BufferedReader bufferedReader = null;
				DocumentBuilderFactory factory = null;
				DocumentBuilder builder = null;
				Element message = null;

				try
				{
					count++;
					fileReader = new FileReader(fullFilename);

					bufferedReader = new BufferedReader(fileReader);

					factory = DocumentBuilderFactory.newInstance();
					builder = factory.newDocumentBuilder();

					data = builder.newDocument();

					message = (Element) data.createElement("data");
					message.setAttribute("type", Connector_ASCII);

					row = 0;
					while ((line = bufferedReader.readLine()) != null)
					{
						row++;
						Element xmlrow = (Element) data.createElement("row");
						xmlrow.setAttribute("id", String.valueOf(row));
						xmlrow.setAttribute("cols", String.valueOf(getPatternColumnCount()));
						xmlrow.setNodeValue(String.valueOf(row));

						logger.debug(line);

						LinkedList<FixedASCIIData> nextLine = getASCIIColumnData(line);

						for (int x = 0; x < getPatternColumnCount(); x++)
						{

							logger.debug(nextLine.get(x).columnId);
							logger.debug(nextLine.get(x).columnData);

							Element xmlcol = addElement(data, "col", nextLine.get(x).columnData);
							xmlcol.setAttribute("id", String.valueOf(nextLine.get(x).columnId));
							xmlcol.setNodeValue(nextLine.get(x).columnData);
							xmlrow.appendChild(xmlcol);

						}
						message.appendChild(xmlrow);
					}

					message.setAttribute("type", Connector_ASCII);
					message.setAttribute("cols", String.valueOf(getPatternColumnCount()));
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
						ept.addRow((new ExceptionMsg("Input Pattern",getInboundInterface().getInputPattern())));
						ept.addRow(new ExceptionMsg("Retry Delay",String.valueOf(delay)));
						ept.addRow(new ExceptionMsg("Retries",String.valueOf(count)+" of "+String.valueOf(retries)));
						ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
						ept.addRow(new ExceptionMsg("Renamed",fullFilename+ ".error"));
						
						Common.emailqueue.addToQueue(getInboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

					}
					else
					{
						logger.error("connectorLoad " + getType() + " parse attempt (" + count + " of " + retries + ") of [" + fullFilename + " [" + ex.getMessage() + "]", "");

						util.retryDelay(delay);

					}
				}
				finally
				{

					try
					{
						bufferedReader.close();
					}
					catch (IOException e)
					{
						// Suppress Error
					}
					bufferedReader = null;

					try
					{
						fileReader.close();
					}
					catch (IOException e)
					{
						// Suppress Error
					}
					fileReader = null;

					line = null;
					factory = null;
					builder = null;
					message = null;
				}
			}
			while (count < retries);
		}

		return result;
	}

}
