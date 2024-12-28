package com.commander4j.mw;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import com.commander4j.util.JFileIO;
import com.commander4j.util.JXMLDocument;
import com.commander4j.util.Utility;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

public class ConfigUpdate
{
	private JFileIO jfileio = new JFileIO();
	private Utility utils = new Utility();
	
	private String config_home_dir = "";
	private String config_temp = "";
	private String config_filename = "";
	private String config_backup = "";
	
	private String xslt_home = "";
	private StreamSource xslt_input;
	private StreamSource xslt_transform;
	private Serializer xslt_output;

	private Xslt30Transformer transformer = null;
	private Processor processor = null;
	private XsltCompiler compiler = null;
	private XsltExecutable stylesheet = null;

	private Source xmlSource;
	
	private JXMLDocument doc;
	private Boolean result = false;
	private String version;

	private Document data;
	
	public Boolean upgrade(int reqd_ver)
	{

		config_home_dir = System.getProperty("user.dir") + File.separator + "xml" + File.separator + "config";
		config_filename = config_home_dir + File.separator + "config.xml";
		doc = new JXMLDocument(config_filename);
		version = doc.findXPath("//config/version");
		xmlSource = new StreamSource(new File(config_home_dir+ File.separator +"SaxonConfiguration.xml"));
		xslt_input = new StreamSource(new File(config_filename));
		config_temp = config_home_dir + File.separator + "config_temp.xml";
		xslt_home = System.getProperty("user.dir") + File.separator + "xslt";
		
		if (version.equals(""))
		{
			version = "1";
		}
		
		int actual_ver = Integer.valueOf(version);

		for (int x=actual_ver;x<reqd_ver;x++)
		{
			xslt_transform = new StreamSource(new File(xslt_home + File.separator + "update"+ File.separator +"update_v"+String.valueOf(x+1)+".xsl"));
			
			try
			{
				config_backup = config_home_dir + File.separator +"backup"+ File.separator +"config_v"+String.valueOf(x)+"_"+utils.getISODateTimeString().replace("T", "_").replace(" ", "_").replace("-", "_").replace(":", "_")+".xml";
				
				processor = new Processor(Configuration.readConfiguration(xmlSource));
				compiler = processor.newXsltCompiler();
				stylesheet = compiler.compile(xslt_transform);
				
				xslt_output = processor.newSerializer(new File(config_temp));
				xslt_output.setOutputProperty(Serializer.Property.METHOD, "xml");
				xslt_output.setOutputProperty(Serializer.Property.INDENT, "yes");

				transformer = stylesheet.load30();
				transformer.transform(xslt_input, xslt_output);

				doc = new JXMLDocument();
				result = doc.setDocument(config_temp);
				data = doc.getDocument();
				
				System.out.println("Move "+config_filename+ " to "+config_backup);
				jfileio.move_File(config_filename, config_backup);
				
				System.out.println("Delete "+config_temp);
				jfileio.deleteFile(config_temp);
				
				System.out.println("Create "+config_home_dir+File.separator+config_temp);
				jfileio.writeToDisk(config_home_dir+File.separator, data, "config_temp.xml");
				
				System.out.println("Rename "+config_home_dir+File.separator+config_temp+" to "+config_filename);
				jfileio.move_File(config_temp,config_filename);

				
			}
			catch (Exception ex)
			{
				System.out.print(ex.getMessage());
			}
			
		}

		return result;
	}
}
