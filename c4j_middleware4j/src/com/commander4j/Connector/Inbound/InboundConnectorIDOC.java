package com.commander4j.Connector.Inbound;

import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.idoc.IdocDataSegment;
import com.commander4j.idoc.IdocParser;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.idoc.IdocOutputData;
import com.commander4j.sys.Common;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;

public class InboundConnectorIDOC extends InboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundConnectorIDOC.class));
	private JPropQuickAccess qa = new JPropQuickAccess();

	public InboundConnectorIDOC(InboundInterface inter)
	{
		super(Connector_IDOC, inter);
	}

	@Override
	public boolean connectorLoad(String fullFilename)
	{

		logger.debug("connectorLoad [" + fullFilename + "]");
		boolean result = false;

		if (backupInboundFile(fullFilename))
		{

			String idocSchemaFilename = getInboundInterface().getIdocSchemaFilename();

			// TODO Auto-generated method stub
			IdocParser idp = new IdocParser(idocSchemaFilename, fullFilename);
			try
			{

				idp.ReadConfigFile();
				idp.GetConfigData();
				idp.GetData();

				IdocOutputData od = idp.GetOutputData();

				/* DOCUMENT */
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				data = builder.newDocument();
				result = true;

				String msgId = od.getMsgId();

				System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				System.out.println("<MESSAGE number=\"" + idp.getFileToProcess() + "\" type=\"mm\" id=\"" + msgId + "\">");

				Element message = (Element) data.createElement("MESSAGE");
				message.setAttribute("number", idp.getFileToProcess());
				message.setAttribute("type", "idoc");
				message.setAttribute("id", msgId);

				int dscount = od.GetDataSegments().size();

				for (int x = 1; x <= dscount; x++)
				{
					IdocDataSegment ds = od.GetDataSegments().get(x - 1);

					/* DATA */
					Element outputdata = (Element) data.createElement("DATA");
					outputdata.setAttribute("type", ds.SegmentName);

					Set<String> keyset = ds.Properties.keySet();

					Iterator<String> it = keyset.iterator();
					while (it.hasNext())
					{
						Object obj = it.next();

						Object val = ds.Properties.get(obj.toString());

						/* FIELD */
						Element outputfield = (Element) data.createElement("FIELD");
						outputfield.setAttribute("name", obj.toString());

						if (val.toString().length() > 0)
						{
							StringBuilder filtered = new StringBuilder(val.toString().length());
							for (int i = 0; i < val.toString().length(); i++)
							{
								char current = val.toString().charAt(i);

								if ((current >= 0x20) || (current == 0x13) || (current == 0x10))
								{
									filtered.append(current);
								}
								else
								{
									filtered.append("_");
								}

							}
							outputfield.setAttribute("value", filtered.toString());
						}
						else
						{
							outputfield.setAttribute("value", val.toString());
						}

						outputdata.appendChild(outputfield);

					}
					message.appendChild(outputdata);
					it = null;
					ds = null;
					outputdata = null;
				}

				data.appendChild(message);

				message = null;
				factory = null;
				builder = null;

				result = true;

			}
			catch (Exception ex)
			{
				result = false;
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
				ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
				
				Common.emailqueue.addToQueue(getInboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

			}

			idp = null;

		}

		return result;
	}

}
