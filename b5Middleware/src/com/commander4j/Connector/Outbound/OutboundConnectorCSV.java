package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;
import com.opencsv.CSVWriter;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorCSV extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorCSV.class));
	private LinkedList<String> parseOptions = new LinkedList<String>();
	boolean disableQuotes = false;
	char seperator = ',';
	char quote = '"';
	String endOfLine = "default";

	public OutboundConnectorCSV(OutboundInterface inter)
	{
		super(Connector_CSV, inter);
	}

	private void getCSVOptions()
	{
		String options = getOutboundInterface().getCSVOptions();
		String delimeter = getOutboundInterface().getOptionDelimeter();
		parsePattern(options, delimeter);
	}

	private void parsePattern(String pattern, String delim)
	{
		parseOptions.clear();

		if (pattern.equals("") == false)
		{
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
				case "endofline":
					endOfLine = val;
					break;
				default:
					opt = "";
					break;
				}
			}
		}
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;

		fullPath = fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase();

		String tempFilename = fullPath + ".tmp";
		String finalFilename = fullPath;

		FileUtils.deleteQuietly(new File(tempFilename));

		getCSVOptions();

		logger.debug("connectorSave [" + fullPath + "]");

		JXMLDocument document = new JXMLDocument();
		document.setDocument(getData());

		CSVWriter writer = null;

		try
		{

			System.out.println(document.documentToString(getData()));

			// Create Writer

			if (disableQuotes)
			{
				if (endOfLine.equals("default"))
				{
					writer = new CSVWriter(new FileWriter(tempFilename), seperator, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
				}
				else
				{
					writer = new CSVWriter(new FileWriter(tempFilename), seperator, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\r\n");
				}

			}
			else
			{
				if (endOfLine.equals("default"))
				{
					writer = new CSVWriter(new FileWriter(tempFilename), seperator, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
				}
				else
				{
					writer = new CSVWriter(new FileWriter(tempFilename), seperator, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, "\r\n");
				}

			}

			int currentRow = 1;
			int colsSpecified = 1;

			// Loop through each row

			while (colsSpecified > 0)
			{

				// Get current row and check number of cols is present
				String noOfCols = util.replaceNullStringwithBlank(document.findXPath("/data/row[" + String.valueOf(currentRow) + "]/@cols").trim()); // Yes
																																						// cols

				if (noOfCols.equals(""))
				{
					noOfCols = "0"; // Trigger end of file.
				}

				colsSpecified = Integer.valueOf(noOfCols);

				if (colsSpecified > 0)
				{

					String[] csvrow = new String[colsSpecified];

					for (int currentCol = 1; currentCol <= colsSpecified; currentCol++)
					{
						String xpath = "/data/row[" + String.valueOf(currentRow) + "]/col[" + String.valueOf(currentCol) + "]";
						String dataString = util.replaceNullStringwithBlank(document.findXPath(xpath).trim());
						csvrow[currentCol - 1] = dataString;

					}

					writer.writeNext(csvrow);
				}

				currentRow++;

			}

			writer.flush();
			writer.close();

			FileUtils.deleteQuietly(new File(finalFilename));

			FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

			result = true;
		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");

			Common.emailqueue.addToQueue(outint.isMapEmailEnabled(), "Error", "Error writing to CSV file [" + fullPath + "]", ex.getMessage() + "\n\n", "");

		}
		finally
		{
			try
			{
				writer.close();
			}
			catch (Exception e)
			{
				// Suppress Error
			}

			writer = null;
			document = null;
			fullPath = null;
			tempFilename = null;
			finalFilename = null;
		}

		return result;
	}

}
