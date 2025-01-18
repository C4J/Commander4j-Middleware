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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Interface.Mapping.Map;
import com.commander4j.exception.ExceptionHTML;
import com.commander4j.exception.ExceptionMsg;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JFileIO;
import com.commander4j.util.JXMLDocument;
import com.commander4j.util.RemoteShareChecker;

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
	String filename_imported = "";
	String filename_transformed = "";
	RemoteShareChecker rsc = new RemoteShareChecker();
	private JPropQuickAccess qa = new JPropQuickAccess();

	public InboundInterface(Map map)
	{
		super(map);
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
			
			filename_imported = "";
			filename_transformed = "";
			
			if (rsc.isValidPath(getInputPath()))
			{

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

											writeSuccess = jfileio.writeToDisk(Common.props.getChildById("logDir").getValueAsString(), data, filename_imported);

											if (getXSLTFilename().equals("") == false)
											{

												filename_transformed = util.getCurrentTimeStampString() + " INPUT_TRANSFORMED_" + connector.getType() + "_" + getId() + "_" + file.getName();

												if (filename_transformed.endsWith(".xml") == false)
												{
													filename_transformed = filename_transformed + ".xml";
												}

												xmlSource = new StreamSource(new File(System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config" + File.separator + "SaxonConfiguration.xml"));

												source = new StreamSource(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_imported));
												destination = new StreamResult(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
												xslt = new StreamSource(new File(getXSLTPath() + getXSLTFilename()));

												processor = new Processor(Configuration.readConfiguration(xmlSource));
												compiler = processor.newXsltCompiler();
												stylesheet = compiler.compile(xslt);
												out = processor.newSerializer(new File(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
												out.setOutputProperty(Serializer.Property.METHOD, "xml");
												out.setOutputProperty(Serializer.Property.INDENT, "yes");

												transformer = stylesheet.load30();
												transformer.transform(source, out);

												doc = new JXMLDocument();
												loadFileResult = doc.setDocument(Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed);
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
													logger.error("Inbound Map [" + map.getId() + "] XML Save Failure" + " " + filename_imported);

													ExceptionHTML ept = new ExceptionHTML("Inbound Map [" + map.getId() + "] XML Save Failure", "Description", "10%", "Detail", "30%");
													ept.clear();
													ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
													ept.addRow(new ExceptionMsg("Map Id", getMap().getId()));
													ept.addRow(new ExceptionMsg("Type", getType()));
													if (getXSLTFilename().equals("") == false)
													{
														ept.addRow(new ExceptionMsg("XSLT Path", getXSLTPath()));
														ept.addRow(new ExceptionMsg("XSLT File", getXSLTFilename()));
													}
													if (util.replaceNullStringwithBlank(filename_imported).equals("") == false)
													{
														ept.addRow(new ExceptionMsg("Source", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_imported));
													}
													if (util.replaceNullStringwithBlank(filename_transformed).equals("") == false)
													{
														ept.addRow(new ExceptionMsg("Destination", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
													}

													ept.addRow(new ExceptionMsg("Exception", "Unable to save inbound xml"));
													Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error Map [" + map.getId() + "] Unable to save inbound xml", ept.getHTML(), "");

												}

												if (loadFileResult == false)
												{
													logger.error("Inbound Map [" + map.getId() + "] XML Load Failure" + " " + filename_imported);

													ExceptionHTML ept = new ExceptionHTML("Inbound Map [" + map.getId() + "] XML Load Failure", "Description", "10%", "Detail", "30%");
													ept.clear();
													ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
													ept.addRow(new ExceptionMsg("Stage", "InboundInterface"));
													ept.addRow(new ExceptionMsg("Map Id", getMap().getId()));
													ept.addRow(new ExceptionMsg("Type", getType()));
													if (getXSLTFilename().equals("") == false)
													{
														ept.addRow(new ExceptionMsg("XSLT Path", getXSLTPath()));
														ept.addRow(new ExceptionMsg("XSLT File", getXSLTFilename()));
													}
													if (util.replaceNullStringwithBlank(filename_imported).equals("") == false)
													{
														ept.addRow(new ExceptionMsg("Source", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_imported));
													}
													if (util.replaceNullStringwithBlank(filename_transformed).equals("") == false)
													{
														ept.addRow(new ExceptionMsg("Destination", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
													}

													ept.addRow(new ExceptionMsg("Exception", "Unable to load inbound xml"));
													Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Error Map [" + map.getId() + "] Unable to load inbound xml", ept.getHTML(), "");

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
			else
			{
				logger.error("InboundInterface Map [" + map.getId() + "] error :" + rsc.getErrorMessage());

				ExceptionHTML ept = new ExceptionHTML("Inbound Map [" + getMap().getId() + "] Exception", "Description", "10%", "Detail", "30%");
				ept.clear();
				ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
				ept.addRow(new ExceptionMsg("Stage", "InboundInterface"));
				ept.addRow(new ExceptionMsg("Map Id", getMap().getId()));
				ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getMap().getId())+"//description")));
				ept.addRow(new ExceptionMsg("Type", getType()));
				
				if (getXSLTFilename().equals("") == false)
				{
					ept.addRow(new ExceptionMsg("XSLT Path", getXSLTPath()));
					ept.addRow(new ExceptionMsg("XSLT File", getXSLTFilename()));
				}
				
				if (util.replaceNullStringwithBlank(filename_imported).equals("") == false)
				{
					ept.addRow(new ExceptionMsg("Source", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_imported));
				}
				
				if (util.replaceNullStringwithBlank(filename_transformed).equals("") == false)
				{
					ept.addRow(new ExceptionMsg("Destination", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
				}
				
				ept.addRow(new ExceptionMsg("Exception", rsc.getErrorMessage()));
				
				Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Inbound Map [" + map.getId() + "] Exception", ept.getHTML(), "");
			}

		}
		catch (Exception ex)
		{

			logger.error("InboundInterface Map [" + map.getId() + "] error :" + ex.getMessage());

			ExceptionHTML ept = new ExceptionHTML("Inbound Map [" + getMap().getId() + "] Exception", "Description", "10%", "Detail", "30%");
			ept.clear();
			ept.addRow(new ExceptionMsg("Description",qa.getString(Common.props, qa.getRootURL()+"//description")));
			ept.addRow(new ExceptionMsg("Stage", "InboundInterface"));
			ept.addRow(new ExceptionMsg("Map Id", getMap().getId()));
			ept.addRow(new ExceptionMsg("Map Description",qa.getString(Common.props, qa.getMapURL(getMap().getId())+"//description")));

			ept.addRow(new ExceptionMsg("Type", getType()));
			if (getXSLTFilename().equals("") == false)
			{
				ept.addRow(new ExceptionMsg("XSLT Path", getXSLTPath()));
				ept.addRow(new ExceptionMsg("XSLT File", getXSLTFilename()));
			}
			if (util.replaceNullStringwithBlank(filename_imported).equals("") == false)
			{
				ept.addRow(new ExceptionMsg("Source", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_imported));
			}
			if (util.replaceNullStringwithBlank(filename_transformed).equals("") == false)
			{
				ept.addRow(new ExceptionMsg("Destination", Common.props.getChildById("logDir").getValueAsString() + File.separator + filename_transformed));
			}
			ept.addRow(new ExceptionMsg("Exception", ex.getMessage()));
			ept.addRow(new ExceptionMsg("Details", ExceptionUtils.getStackTrace(ex)));
			Common.emailqueue.addToQueue(map.isMapEmailEnabled(), "Error", "Inbound Map [" + map.getId() + "] Exception", ept.getHTML(), "");

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
