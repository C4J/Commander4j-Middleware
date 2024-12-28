package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorSOCKET extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorSOCKET.class));

	public OutboundConnectorSOCKET(OutboundInterface inter)
	{
		super(Connector_SOCKET, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;

		logger.debug("connectorSave [" + fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase() + "]");

		if (fullPath.endsWith("." + getType().toLowerCase()) == false)
		{
			fullPath = fullPath + "." + getType().toLowerCase();
		}

		String tempFilename = fullPath + ".tmp";
		String finalFilename = fullPath;

		FileUtils.deleteQuietly(new File(tempFilename));

		Socket socket = null;
		String send = "";
		String eol = "";

		PrintWriter out = null;
		FileWriter fw = null;

		try // File Output
		{
			fw = new FileWriter(tempFilename);

			JXMLDocument document = new JXMLDocument();
			document.setDocument(getData());

			if (document.findXPath("//data/line[contains(text(),'*EOF*')]").equals("*EOF*"))
			{

				socket = new Socket(getOutboundInterface().getHostIP(), getOutboundInterface().getHostPort());
				out = new PrintWriter(socket.getOutputStream(), true);

				for (int rep = 0; rep < getOutboundInterface().getHostRepeat(); rep++)
				{

					int line = 1;
					while (util.replaceNullStringwithBlank(document.findXPath("/data/line[" + String.valueOf(line) + "]")).equals("*EOF*") == false)
					{
						send = util.replaceNullStringwithBlank(document.findXPath("/data/line[" + String.valueOf(line) + "]"));
						eol = util.replaceNullStringwithBlank(document.findXPath("/data/line[" + String.valueOf(line) + "]/@eol"));

						if (eol.equals("") == false)
						{
							eol = eol.replace("cr", "\r");
							eol = eol.replace("lf", "\n");
						}

						if (send.equals("*EOF*") == false)
						{
							out.print(send + eol);
							out.flush();
							fw.write(send + "\n");
							fw.flush();
							logger.debug(send);
						}
						line++;
					}
				}

				out.close();
				socket.close();

				FileUtils.deleteQuietly(new File(finalFilename));

				FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

				tempFilename = null;
				finalFilename = null;

				result = true;

			}
			else
			{
				logger.error("Missing *EOF* in input file");
			}

		}
		catch (Exception err)
		{
			logger.error(err.getMessage());

			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",outint.getMap().getId()));
			ept.addRow(new ExceptionMsg("Connector Id",outint.getId()));
			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",fullPath));
			ept.addRow(new ExceptionMsg("Exception",err.getMessage()));
			
			Common.emailqueue.addToQueue(outint.getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

		}
		finally
		{
			try
			{
				fw.close();
			}
			catch (Exception e)
			{

			}

			out.close();

			try
			{
				socket.close();
			}
			catch (Exception ex)
			{

			}

			socket = null;

			out = null;

			fw = null;
		}

		return result;

	}
}
