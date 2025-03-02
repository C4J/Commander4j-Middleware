package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.sys.FixedASCIIColumns;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorASCII extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorASCII.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	private LinkedList<FixedASCIIColumns> parseCols = new LinkedList<FixedASCIIColumns>();
	private int maxColumn = 0;

	public OutboundConnectorASCII(OutboundInterface inter)
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
			if (end > maxColumn)
			{
				maxColumn = end;
			}
			FixedASCIIColumns col = new FixedASCIIColumns();
			col.position = x;
			col.start = start;
			col.end = end;
			parseCols.add(col);
		}
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;

		parsePattern(getOutboundInterface().getOutputPattern());

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		String cl = util.replaceNullStringwithBlank(document.findXPath("//data/@cols").trim());
		if (cl.equals(""))
		{
			cl = "0";
		}
		int columns = Integer.valueOf(cl);

		String rw = util.replaceNullStringwithBlank(document.findXPath("//data/@rows").trim());
		if (rw.equals(""))
		{
			rw = "0";
		}
		int rows = Integer.valueOf(rw);

		try
		{
			System.out.println(document.documentToString(getData()));
		}
		catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileWriter fw = null;
		String tempFilename = "";
		String finalFilename = "";
		String rowdata = null;
		char[] rowdataArray = null;

		try
		{
			if (rows > 0)
			{
				if (columns > 0)
				{

					fullPath = fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase();
					tempFilename = fullPath + ".tmp";
					finalFilename = fullPath;

					FileUtils.deleteQuietly(new File(tempFilename));

					fw = new FileWriter(tempFilename);

					// Read new row value from XML

					for (int r = 1; r <= rows; r++)
					{
						rowdata = util.padSpace(maxColumn);
						rowdataArray = rowdata.toCharArray();

						// Read new col value from XML

						for (int c = 1; c <= columns; c++)
						{
							if (c <= getPatternColumnCount())
							{
								// Get the data from the XML input
								String xpath = "//data/row[@id='" + String.valueOf(r) + "']/col[@id='" + String.valueOf(c) + "']";
								String dataString = util.replaceNullStringwithBlank(document.findXPath(xpath).trim());

								// Get the position of the data within the
								// ASCII
								// file for this column.
								FixedASCIIColumns coldef = parseCols.get(c - 1);

								// Do we have data to put into columns
								if (dataString.isEmpty() == false)
								{
									// For each column in the data field
									for (int x = 0; x < dataString.length(); x++)
									{
										// Make sure that the data string
										// fits
										// within output column range
										if ((coldef.start + x) <= coldef.end)
										{
											// Update character in output
											// row
											rowdataArray[coldef.start + x - 1] = dataString.charAt(x);
										}
									}
								}
								logger.debug("row=[" + String.valueOf(r) + "] col=[" + String.valueOf(c) + "] data=[" + dataString + "]");
							}
							else
							{
								logger.debug("Ignored row=[" + String.valueOf(r) + "] col=[" + String.valueOf(c) + "] - no column defined in config.xml");
							}
						}

						// Convert char array back into string
						String joinedString = String.copyValueOf(rowdataArray);
						joinedString = joinedString + "\n";
						// Write to output file
						fw.write(joinedString);
						fw.flush();
					}

					// Close output file
					fw.close();

					FileUtils.deleteQuietly(new File(finalFilename));

					FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

					result = true;

				}
			}
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
			ept.addRow(new ExceptionMsg("Source",fullPath));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Output Pattern",getOutboundInterface().getOutputPattern()));
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
				fw.close();
			}
			catch (Exception e)
			{
				// Suppress Error
			}

			fw = null;
			document = null;
			fullPath = null;
			tempFilename = null;
			finalFilename = null;
			rowdata = null;
			rowdataArray = null;
			document = null;
			cl = null;
			rw = null;
		}

		return result;
	}

}
