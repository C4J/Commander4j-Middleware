package com.commander4j.mw;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.Interface.Mapping.Map;
import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.prop.JPropBoolean;
import com.commander4j.prop.JPropInteger;
import com.commander4j.prop.JPropLong;
import com.commander4j.prop.JPropPrint;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.prop.JPropString;
import com.commander4j.prop.JPropStringArray;
import com.commander4j.sys.Common;
import com.commander4j.util.JFileIO;
import com.commander4j.util.JXMLDocument;
import com.commander4j.util.Utility;

import INTERFACE.com.commander4j.Connector.InboundConnectorINTERFACE;
import INTERFACE.com.commander4j.Connector.OutboundConnectorINTERFACE;

public class ConfigLoad
{
	LinkedList<Map> maps = new LinkedList<Map>();
	LinkedList<String> directoryErrors = new LinkedList<String>();
	
	JFileIO fio = new JFileIO();
	Utility util = new Utility();
	
	JPropString jPropString = new JPropString();
	JPropStringArray jPropStringArray = new JPropStringArray();
	JPropInteger jPropInteger = new JPropInteger();
	JPropBoolean jPropBoolean = new JPropBoolean();
	JPropLong jPropLong = new JPropLong();
	JPropPrint jPropPrint = new JPropPrint();
	JPropQuickAccess qa = new JPropQuickAccess();

	public String getInterfaceStatistics()
	{
		String result = "<div id=\"statistics\" >\n"
					+ "		<table border=\"3\">\n"
					+ "		    <thead>\n"
					+ "		      <caption>Interface Statistics</caption>\n"
					+ "		      <tr>\n"
					+ "		        <th style=\"width:10%; text-align: center\">Map ID</th>\n"
					+ "		        <th style=\"width:40%; text-align: center\">Description</th>\n"
					+ "		        <th style=\"width:10%; text-align: center\">Inbound</th>\n"
					+ "		        <th style=\"width:10%; text-align: center\">Outbound</th>\n"
					+ "		      </tr>\n"
					+ "		    </thead>\n"
					+ "		    <tbody>\n";
		
					for (int x = 0; x < getMaps().size(); x++)
					{
						result = result + "<tr>\n"
						+ "	<td style=\"width:10%; text-align: center\">"+getMaps().get(x).getId()+"</td>\n"
						+ " <td style=\"width:40%; text-align: left\">"+getMaps().get(x).getDescription()+"</td>\n"
						+ " <td style=\"width:10%; text-align: right\">"+getMaps().get(x).getInboundMapMessageCount().toString()+"</td>\n"
						+ " <td style=\"width:10%; text-align: right\">"+getMaps().get(x).getOutboundMapMessageCount().toString()+"</td>\n"
						+ "</tr>\n";
					}
					
					result = result + " </tbody>\n"
					+ "		</table>\n"
					+ "</div>";
		
		return result;
	}

	public void resetInterfaceStatistics()
	{
		for (int x = 0; x < getMaps().size(); x++)
		{
			getMaps().get(x).resetInboundMapMessageCount();
			getMaps().get(x).resetOutboundMapMessageCount();
		}
	}

	public void resetErrors()
	{
		directoryErrors.clear();
	}
	
	public int getMapDirectoryErrorCount()
	{
		return directoryErrors.size();
	}

	public LinkedList<String> getMapDirectoryErrors()
	{
		return directoryErrors;
	}

	public ConfigLoad()
	{

	}

	public LinkedList<Map> getMaps()
	{
		return maps;
	}

	public void startMaps()
	{
		for (int x = 0; x < maps.size(); x++)
		{
			maps.get(x).setEnabled(true);
		}
	}

	public void stopMaps()
	{
		for (int x = 0; x < maps.size(); x++)
		{
			maps.get(x).setEnabled(false);
		}
	}

	public LinkedList<Map> loadMaps(String filename)
	{

		int mapSeq = 1;

		maps.clear();

		JXMLDocument doc = new JXMLDocument(filename);

		String version  =  doc.findXPath("//config/version");
		String description =  doc.findXPath("//config/description");
		String XSLTPath = doc.findXPath("//config/XSLTPath");
		String logDir = doc.findXPath("//config/logPath");
		String enableEmailNotifications = doc.findXPath("//config/enableEmailNotifications");
		String retryOpenFileCount = doc.findXPath("//config/retryOpenFileCount");
		String retryOpenFileDelay = doc.findXPath("//config/retryOpenFileDelay");
		String ArchiveRetentionDays = doc.findXPath("//config/logArchiveRetentionDays");
		String statusReportTime = doc.findXPath("//config/statusReportTime");

		qa.setValue(Common.props,qa.getRootURL()+"//description",jPropString.getValue(description));
		qa.setValue(Common.props,qa.getRootURL()+"//version",jPropString.getValue(version));
		qa.setValue(Common.props,qa.getRootURL()+"//XSLTPath",jPropString.getValue(XSLTPath),jPropString.getValue(System.getProperty("user.dir") + File.separator + "xslt" + File.separator));
		qa.setValue(Common.props,qa.getRootURL()+"//enableEmailNotifications",jPropBoolean.getValue(enableEmailNotifications));
		qa.setValue(Common.props,qa.getRootURL()+"//logDir",jPropString.getValue(logDir),jPropString.getValue(System.getProperty("user.dir") + java.io.File.separator + "interface" + java.io.File.separator + "log"));
		qa.setValue(Common.props,qa.getRootURL()+"//statusReportTime",jPropString.getValue(statusReportTime),"09:00:00");
		qa.setValue(Common.props,qa.getRootURL()+"//logArchiveRetentionDays",jPropInteger.getValue(ArchiveRetentionDays),3);
		qa.setValue(Common.props,qa.getRootURL()+"//retryOpenFileDelay",jPropInteger.getValue(retryOpenFileDelay),1000);
		qa.setValue(Common.props,qa.getRootURL()+"//retryOpenFileCount",jPropInteger.getValue(retryOpenFileCount),3);
		
		while (doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/id").trim() != "")
		{
			Boolean mapEnabled = Boolean.valueOf(util.replaceNullStringwithBlank(doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/enabled").trim()));
			
			if (mapEnabled)
			{
				Map map = new Map();

				String mapId = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/id").trim();
				map.setId(mapId);
				
				String mapDescription = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/description").trim();
				String mapEmailEnabled = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/enableEmailNotifications").trim();	
				
				qa.setValue(Common.props,qa.getMapURL(mapId), "");
				qa.setValue(Common.props,qa.getMapURL(mapId)+"//enabled", jPropBoolean.getValue(mapEnabled));
				qa.setValue(Common.props,qa.getMapURL(mapId)+"//description", jPropString.getValue(mapDescription));
				
				if (mapEmailEnabled.equals(""))
				{
					mapEmailEnabled = enableEmailNotifications;
				}
				qa.setValue(Common.props,qa.getMapURL(mapId)+"//enableEmailNotifications", jPropBoolean.getValue(mapEmailEnabled));
				
				qa.setValue(Common.props,qa.getMapURL(mapId)+"//mapSeq", jPropInteger.getValue(mapSeq));
				
				int inputSeq = 1;

				String inputId = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/id").trim();
				String inputDescription = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/description").trim();
				String inputType = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/type").trim();
				String inputPath = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/url/path").trim();
				String inputMask = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/url/mask").trim();
				String inputPrefix = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/url/prefix").trim();
				String inputPattern = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/ascii/pattern").trim();
				String idocSchemaFilename = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/idoc/idocSchemaFilename").trim();
				String pollingInterval = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/url/pollingInterval").trim();
				String inputXSLT = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/xsl/XSLT").trim();
				String csvOptionsIn = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/csv/csvOptions").trim();
				String optionDelimeterIn = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/input[" + String.valueOf(inputSeq) + "]/csv/optionDelimeter").trim();

				InboundInterface inboundInterface = new InboundInterface(map);
				inboundInterface.setId(inputId);	
				
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId), "");
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//description", jPropString.getValue(inputDescription));
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//type", jPropString.getValue(inputType));
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//url", "");
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//url//prefix", jPropString.getValue(inputPrefix));
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//url//path", jPropString.getValue(inputPath),jPropString.getValue(System.getProperty("user.dir") + File.separator + "interface" + File.separator + "input"));
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//url//pollingInterval", jPropLong.getValue(pollingInterval));
				
				if (fio.isValidDirectory(inputPath) == false) directoryErrors.addLast("Map : [" + mapId + "] inputId : [" + inputId + "] Invalid Directory : " + inputPath);

				inboundInterface.setType(inputType);
				
				qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//url//mask", jPropStringArray.getValue(inputMask),inboundInterface.getFileMask(inputType));

				if (inputXSLT.equals("")==false)
				{
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//xsl", "");
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//xsl//XSLTPath", jPropString.getValue(XSLTPath));
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//xsl//XSLT", jPropString.getValue(inputXSLT));
				}

				if (inputType.equals(InboundConnectorINTERFACE.Connector_ASCII))
				{
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//ascii", "");
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//ascii//pattern", jPropString.getValue(inputPattern));
				}
				
				if (inputType.equals(InboundConnectorINTERFACE.Connector_IDOC))
				{
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//idoc", "");
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//idoc//idocSchemaFilename", jPropString.getValue(idocSchemaFilename));
				}
								
				if (inputType.equals(InboundConnectorINTERFACE.Connector_CSV))
				{
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//csv", "");
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//csv//csvOptions", jPropString.getValue(csvOptionsIn));
					qa.setValue(Common.props,qa.getMapInputURL(mapId, inputId)+"//csv//optionDelimeter", jPropString.getValue(optionDelimeterIn));
				}

				map.setInboundInterface(inboundInterface);

				int outputSeq = 1;

				while (doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/id").trim() != "")
				{
					Boolean outputEnabled = Boolean.valueOf(util.replaceNullStringwithBlank(doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/enabled").trim()));
					
					if (outputEnabled)
					{
						String outputId = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/id").trim();
						
						String outputDescription = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/description").trim();

						String outputType = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/type").trim();
						String outputPath = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/url/path").trim();
						
						String outputXSLT = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/xsl/XSLT").trim();
						String outputPattern = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/ascii/pattern").trim();
						String optionDelimeterOut = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/csv/optionDelimeter").trim();
						String csvOptionsOut = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/csv/csvOptions").trim();
						String outputFileExtension = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/url/fileExtension").trim();
						String outputPrefix = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/url/prefix").trim();

						String param1_Type = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/condition/param1_Type").trim();
						String param1 = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/condition/param1").trim();
						String param2_Type = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/condition/param2_Type").trim();
						String param2 = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/condition/param2").trim();
						String comparitor = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/condition/comparitor").trim();

						String emailSubject = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/email/subject").trim();
						String emailMessage = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/email/message").trim();
						String emailListID = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/email/emailListID").trim();
						String use83GUID = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/url/use83GUID").trim();
						String queueName = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/print/queueName").trim();

						String mqttBroker = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/mqtt/mqttBroker").trim();
						String mqttTopic = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/mqtt/mqttTopic").trim();
						String mqttClient = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/mqtt/mqttClient").trim();
						String mqttContentXPath = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/mqtt/mqttContentXPath").trim();
						String mqttQos = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/mqtt/mqttQos").trim();

						String host_ip = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/host/ip").trim();
						String host_port = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/host/port").trim();
						String repeat = doc.findXPath("//config/maps/map[" + String.valueOf(mapSeq) + "]/connectors/output[" + String.valueOf(outputSeq) + "]/host/repeat").trim();
							
						OutboundInterface outboundInterface = new OutboundInterface(map);

						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId), "");
						
						outboundInterface.setId(outputId);

						outboundInterface.setType(outputType);
						
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//enabled", jPropString.getValue(outputEnabled));
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//description", jPropString.getValue(outputDescription));
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//type", jPropString.getValue(outputType));
						
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//url", "");
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//url//path", jPropString.getValue(outputPath),System.getProperty("user.dir") + File.separator + "interface" + File.separator + "output");
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//url//use83GUID", jPropBoolean.getValue(use83GUID));
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//url//prefix", jPropString.getValue(outputPrefix));
						qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//url//fileExtension", jPropString.getValue(outputFileExtension),outboundInterface.getFileMask(outputType));
						
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_PDF_PRINT))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//print", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//print//queueName", jPropString.getValue(queueName));
						}
						
						if (outputXSLT.equals("")==false)
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//xsl", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//xsl//XSLTPath", jPropString.getValue(XSLTPath));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//xsl//XSLT", jPropString.getValue(outputXSLT));
						}
						
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_ASCII))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//ascii", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//ascii//pattern", jPropString.getValue(outputPattern));
						}
						
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_CSV))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//csv", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//csv//csvOptions", jPropString.getValue(csvOptionsOut));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//csv//optionDelimeter", jPropString.getValue(optionDelimeterOut));
							
						}
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_EMAIL))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//email", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//email//emailSubject", jPropString.getValue(emailSubject));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//email//emailMessage", jPropString.getValue(emailMessage));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//email//emailListID", jPropString.getValue(emailListID));
						}
						
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_MQTT))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt//mqttBroker", jPropString.getValue(mqttBroker));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt//mqttTopic", jPropString.getValue(mqttTopic));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt//mqttClient", jPropString.getValue(mqttClient));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt//mqttContentXPath", jPropString.getValue(mqttContentXPath));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//mqtt//mqttQos", jPropString.getValue(mqttQos));
						}
						
						if (outputType.equals(OutboundConnectorINTERFACE.Connector_MQTT) || outputType.equals(OutboundConnectorINTERFACE.Connector_SOCKET))
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//host", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//host//ip", jPropString.getValue(host_ip),"127.0.0.1");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//host//port", jPropInteger.getValue(host_port));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//host//repeat", jPropInteger.getValue(repeat));		
						}

						if ((param1_Type.equals("")==false) && (param2_Type.equals("")==false) )
						{
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition", "");
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition//param1", jPropString.getValue(param1));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition//param1_Type", jPropString.getValue(param1_Type));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition//param2", jPropString.getValue(param2));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition//param2_Type", jPropString.getValue(param2_Type));
							qa.setValue(Common.props,qa.getMapOutputURL(mapId, outputId)+"//condition//comparitor", jPropString.getValue(comparitor));
						}
						
						String[] multiPath = outputPath.split(";");
						
						for (int temp2 = 0; temp2 < multiPath.length; temp2++)
						{
							String outputPath2 = multiPath[temp2];
							
							if (fio.isValidDirectory(outputPath2) == false)
							{
								directoryErrors.addLast("Map : [" + mapId + "] outputId : [" + outputId + "] Invalid Directory : " + outputPath2);
							}
						}

						map.addOutboundInterface(outboundInterface);

					}

					outputSeq++;
				}

				maps.add(map);
			}

			mapSeq++;

		}
		Collections.sort(maps);
		
		
		jPropPrint.print(Common.props,0);
		
		return maps;
	}
}
