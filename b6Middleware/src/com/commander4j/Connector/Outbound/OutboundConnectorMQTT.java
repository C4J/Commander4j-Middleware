package com.commander4j.Connector.Outbound;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.sys.Common;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;

public class OutboundConnectorMQTT extends OutboundConnectorABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundConnectorMQTT.class));

	public OutboundConnectorMQTT(OutboundInterface inter)
	{
		super(Connector_MQTT, inter);
	}

	@Override
	public boolean connectorSave(String path, String prefix, String filename)
	{
		boolean result = false;

		filename = getOutboundInterface().get83GUIDFilename(prefix, filename);

		String fullPath = path + File.separator + filename;
		String tempFilename = null;
		String finalFilename = null;
		MemoryPersistence persistence = null;
		MqttClient sampleClient = null;
		MqttConnectOptions connOpts = null;
		MqttMessage message = null;
		FileWriter fw = null;

		logger.debug("connectorSave [" + fullPath + "." + getOutboundInterface().getOutputFileExtension().toLowerCase() + "]");

		try
		{

			if (fullPath.endsWith("." + getType().toLowerCase()) == false)
			{
				fullPath = fullPath + "." + getType().toLowerCase();
			}

			tempFilename = fullPath + ".tmp";
			finalFilename = fullPath;

			FileUtils.deleteQuietly(new File(tempFilename));

			//////// MQTT Code Start

			fw = new FileWriter(tempFilename);

			fw.write("MQTT Broker : " + getOutboundInterface().getMQTBroker() + "\n");
			fw.write("MQTT Client : " + getOutboundInterface().getMQTTClient() + "\n");
			fw.write("MQTT Content : " + getOutboundInterface().getMQTTContentXPath() + "\n");
			fw.write("MQTT Topic : " + getOutboundInterface().getMQTTTopic() + "\n");
			fw.write("MQTT QOS : " + getOutboundInterface().getMQTTQos() + "\n\n");
			fw.flush();

			JXMLDocument document = new JXMLDocument();
			document.setDocument(getData());
			String messageContent = util.replaceNullStringwithBlank(document.findXPath(getOutboundInterface().getMQTTContentXPath()));

			persistence = new MemoryPersistence();

			sampleClient = new MqttClient(getOutboundInterface().getMQTBroker(), getOutboundInterface().getMQTTClient(), persistence);
			connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);

			fw.write("Connecting to broker: " + getOutboundInterface().getMQTBroker() + "\n");
			fw.flush();

			sampleClient.connect(connOpts);
			fw.write("Connected\n");
			fw.flush();

			fw.write("Publishing message: " + messageContent + "\n");
			fw.flush();

			message = new MqttMessage(messageContent.getBytes());
			message.setQos(getOutboundInterface().getMQTTQos());
			sampleClient.publish(getOutboundInterface().getMQTTTopic(), message);
			fw.write("Message published\n");
			fw.flush();

			sampleClient.disconnect();
			fw.write("Disconnected\n\n");
			fw.flush();

			sampleClient.close();

			fw.write("SUCCESS\n");
			fw.flush();

			//////// MQTT Code End

			FileUtils.deleteQuietly(new File(finalFilename));

			FileUtils.moveFile(new File(tempFilename), new File(finalFilename));

			result = true;

		}
		catch (Exception ex)
		{
			logger.error("Message failed to process.");

			ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Stage","connectorSave"));
			ept.addRow(new ExceptionMsg("Map Id",getOutboundInterface().getMap().getId()));
			ept.addRow(new ExceptionMsg("Connector Id",getOutboundInterface().getId()));
			ept.addRow(new ExceptionMsg("Type",getType()));
			ept.addRow(new ExceptionMsg("Source",fullPath));
			if (getOutboundInterface().getXSLTFilename().equals("")==false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path",getOutboundInterface().getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File",getOutboundInterface().getXSLTFilename()));
			}
			ept.addRow(new ExceptionMsg("Exception",ex.getMessage()));
			
			Common.emailqueue.addToQueue(getOutboundInterface().getMap().isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

		}
		finally
		{
			try
			{
				fw.close();
				fw = null;

			}
			catch (Exception ex)
			{
				// Suppress Error
			}
			fullPath = null;
			tempFilename = null;
			finalFilename = null;
			persistence = null;
			sampleClient = null;
			connOpts = null;
			message = null;
		}

		return result;
	}

}
