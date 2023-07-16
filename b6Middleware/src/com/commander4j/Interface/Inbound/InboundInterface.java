package com.commander4j.Interface.Inbound;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Interface.Mapping.Map;
import com.commander4j.sys.Common;
import com.commander4j.util.JFileIO;
import com.commander4j.util.JXMLDocument;

import ABSTRACT.com.commander4j.Interface.InboundInterfaceABSTRACT;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class InboundInterface extends InboundInterfaceABSTRACT
{

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundInterface.class));
	JFileIO jfileio = new JFileIO();
	TransformerFactory fact = new net.sf.saxon.TransformerFactoryImpl();
	StreamSource source;
	StreamSource xslt;
	StreamResult destination;
	Transformer transformer;
	Boolean writeSuccess;
	Boolean loadFileResult;
	String filename_imported;
	String filename_transformed;

	public InboundInterface(Map map, String description)
	{
		super(map);
		setDescription(description);
	}

	boolean enabled = false;

	@Override
	public void run()
	{
		File dir = new File(getInputPath());
		String[] extensions = getInputFileMask();
		String prefix = getPrefix();
		List<File> files = null;
		Xslt30Transformer transformer = null;
		JXMLDocument doc = null;

		Processor processor = null;
		XsltCompiler compiler = null;
		XsltExecutable stylesheet = null;
		Serializer out = null;
		Source xmlSource = null;

		try
		{

			if (extensions.length > 0)
			{
				if (extensions[0].equals("*"))
				{
					extensions = null;
				}
			}

			files = (List<File>) FileUtils.listFiles(dir, extensions, false);

			if (files.size() > 0)
			{
				logger.debug("Checked for files with extension " + Arrays.toString(extensions) + " found " + files.size());

				for (File file : files)
				{
					if (file.length() > 0)
					{

						if (file.isHidden() == false)
						{

							if (file.getName().toLowerCase().startsWith(prefix.toLowerCase()))
							{

								logger.debug("Processing [" + file.getName() + "]");
								loadFileResult = false;
								writeSuccess = false;

								if (connector.processInboundFile(file.getName()))
								{

									data = connector.getData();

									if (isBinaryFile() == false)
									{
										filename_imported = util.getCurrentTimeStampString() + " INPUT_IMPORTED_" + connector.getType() + "_" + getId() + "_" + file.getName();

										if (filename_imported.endsWith(".xml") == false)
										{
											filename_imported = filename_imported + ".xml";
										}

										writeSuccess = jfileio.writeToDisk(Common.logDir, data, filename_imported);

										if (getXSLTFilename().equals("") == false)
										{

											filename_transformed = util.getCurrentTimeStampString() + " INPUT_TRANSFORMED_" + connector.getType() + "_" + getId() + "_" + file.getName();

											if (filename_transformed.endsWith(".xml") == false)
											{
												filename_transformed = filename_transformed + ".xml";
											}

											xmlSource = new StreamSource(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "SaxonConfiguration.xml"));

											source = new StreamSource(new File(Common.logDir + File.separator + filename_imported));
											destination = new StreamResult(new File(Common.logDir + File.separator + filename_transformed));
											xslt = new StreamSource(new File(getXSLTPath() + getXSLTFilename()));

											processor = new Processor(Configuration.readConfiguration(xmlSource));
											compiler = processor.newXsltCompiler();
											stylesheet = compiler.compile(xslt);
											out = processor.newSerializer(new File(Common.logDir + File.separator + filename_transformed));
											out.setOutputProperty(Serializer.Property.METHOD, "xml");
											out.setOutputProperty(Serializer.Property.INDENT, "yes");

											transformer = stylesheet.load30();
											transformer.transform(source, out);

											doc = new JXMLDocument();
											loadFileResult = doc.setDocument(Common.logDir + File.separator + filename_transformed);
											data = doc.getDocument();

										}
										else
										{
											data = connector.getData();
											loadFileResult = true;
										}

										if (writeSuccess && loadFileResult)
										{
											processConnectorToInterfaceData(connector.getFilename(), data);
										}
										else
										{

											if (writeSuccess == false)
											{
												logger.error("Error Map [" + map.getId() + "] Unable to save inbound xml" + " " + filename_imported);

												Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error Map [" + map.getId() + "]", "Unable to save inbound xml" + "\n\n", filename_imported);

											}

											if (loadFileResult == false)
											{
												logger.error("Error Map [" + map.getId() + "] Unable to load inbound xml" + " " + filename_imported);

												Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error Map [" + map.getId() + "]", "Unable to load inbound xml" + "\n\n", filename_imported);

											}

										}
									}
									else
									{
										processConnectorToInterfaceData(connector.getFilename(), data);
									}
								}
							}
						}
					}
				}
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			logger.error("InboundInterface Map [" + map.getId() + "] error :" + ex.getMessage());

			Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "InboundInterface Map [" + map.getId() + "]", " error :" + ex.getMessage(), "");

		}
		finally
		{
			processor = null;
			compiler = null;
			stylesheet = null;
			out = null;
			dir = null;
			extensions = null;
			prefix = null;
			files = null;
			transformer = null;
			doc = null;
			xmlSource = null;
		}
	}

	public void processConnectorToInterfaceData(String filename, Document data)
	{
		this.data = data;

		logger.debug("processConnectorToInterfaceData [" + util.getStringFromDocument(data) + "]");
		map.processInboundInterfaceToMap(filename, data);

	}

}
