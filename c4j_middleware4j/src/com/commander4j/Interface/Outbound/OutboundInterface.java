package com.commander4j.Interface.Outbound;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Interface.Mapping.Map;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JFileIO;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Interface.OutboundInterfaceABSTRACT;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.trans.XPathException;

public class OutboundInterface extends OutboundInterfaceABSTRACT
{

	JFileIO jfileio = new JFileIO();
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundInterface.class));
	TransformerFactory fact = new net.sf.saxon.TransformerFactoryImpl();
	StreamSource source;
	StreamSource xslt;
	StreamResult destination;
	Transformer transformer;
	Boolean writeSuccess;
	String filename_outputImported;
	String filename_outputTransformed;
	private JPropQuickAccess qa = new JPropQuickAccess();

	Document data;
	boolean enabled = false;

	public OutboundInterface(Map map)
	{
		super(map);
	}

	@Override
	public void run()
	{

	}

	public void processInterfaceToConnector(String filename, Document data)
	{
		this.data = data;

		logger.debug("processInterfaceToConnector [" + util.getStringFromDocument(data) + "]");

		if (getXSLTFilename().equals("") == false)
		{
			getDescription();

			filename_outputImported = util.getCurrentTimeStampString() + " OUTPUT_IMPORTED_" + connector.getType() + "_" + getId() + "_" + filename;
			if (filename_outputImported.endsWith(".xml") == false)
			{
				filename_outputImported = filename_outputImported + ".xml";
			}

			writeSuccess = jfileio.writeToDisk(Common.props.getChildById("logDir").getValueAsString(), data, filename_outputImported);

			if (writeSuccess)
			{

				filename_outputTransformed = util.getCurrentTimeStampString() + " OUTPUT_TRANSFORMED_" + connector.getType() + "_" + getId() + "_" + filename;
				if (filename_outputTransformed.endsWith(".xml") == false)
				{
					filename_outputTransformed = filename_outputTransformed + ".xml";
				}

				try
				{
					Source xmlSource = new StreamSource(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "SaxonConfiguration.xml"));
					Configuration.readConfiguration(xmlSource);

					source = new StreamSource(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputImported));
					destination = new StreamResult(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputTransformed));
					xslt = new StreamSource(new File(getXSLTPath() + getXSLTFilename()));

					try
					{
						Processor processor = new Processor(Configuration.readConfiguration(xmlSource));
						XsltCompiler compiler = processor.newXsltCompiler();
						XsltExecutable stylesheet = compiler.compile(xslt);
						Serializer out = processor.newSerializer(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputTransformed));
						out.setOutputProperty(Serializer.Property.METHOD, "xml");
						out.setOutputProperty(Serializer.Property.INDENT, "yes");
						Xslt30Transformer transformer = stylesheet.load30();
						transformer.transform(source, out);

						JXMLDocument doc = new JXMLDocument(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputTransformed);
						data = doc.getDocument();
					}
					catch (SaxonApiException e)
					{
						logger.error(e.getMessage());
						
						ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
						ept.clear();
						ept.addRow(new ExceptionMsg("Host Name", util.getClientName()));
						ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
						ept.addRow(new ExceptionMsg("Stage","OutboundInterface - SaxonAPI"));
						ept.addRow(new ExceptionMsg("Map Id",getMap().getId()));
						ept.addRow(new ExceptionMsg("Type",getType()));
						ept.addRow(new ExceptionMsg("Saxon Config",System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "SaxonConfiguration.xml"));
						ept.addRow(new ExceptionMsg("XSLT File",getXSLTPath() + getXSLTFilename()));
						ept.addRow(new ExceptionMsg("Source",Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputImported));
						ept.addRow(new ExceptionMsg("Destination",Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputTransformed));
						ept.addRow(new ExceptionMsg("Exception",e.getMessage()));
						
						Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");
					}

				}
				catch (XPathException e)
				{
					logger.error(e.getMessage());

					ExceptionHTML ept = new ExceptionHTML("Error processing message","Description","10%","Detail","30%");
					ept.clear();
					ept.addRow(new ExceptionMsg("Host Name", util.getClientName()));
					ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
					ept.addRow(new ExceptionMsg("Stage","OutboundInterface - XPath"));
					ept.addRow(new ExceptionMsg("Map Id",getMap().getId()));
					ept.addRow(new ExceptionMsg("Type",getType()));
					ept.addRow(new ExceptionMsg("Saxon Config",System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "SaxonConfiguration.xml"));
					ept.addRow(new ExceptionMsg("XSLT File",getXSLTPath() + getXSLTFilename()));
					ept.addRow(new ExceptionMsg("Source",Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputImported));
					ept.addRow(new ExceptionMsg("Destination",Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_outputTransformed));
					ept.addRow(new ExceptionMsg("Exception",e.getMessage()));
					
					Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error processing message",ept.getHTML(), "");

				}
			}
		}

		connector.processOutboundData(getOutputPath(), filename, data);
	}
}
