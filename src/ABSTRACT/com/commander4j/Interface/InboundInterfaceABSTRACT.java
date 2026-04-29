package ABSTRACT.com.commander4j.Interface;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Connector.Inbound.InboundConnectorASCII;
import com.commander4j.Connector.Inbound.InboundConnectorCSV;
import com.commander4j.Connector.Inbound.InboundConnectorEmail;
import com.commander4j.Connector.Inbound.InboundConnectorExcel;
import com.commander4j.Connector.Inbound.InboundConnectorIDOC;
import com.commander4j.Connector.Inbound.InboundConnectorPDF_PRINT;
import com.commander4j.Connector.Inbound.InboundConnectorRAW;
import com.commander4j.Connector.Inbound.InboundConnectorXML;
import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.Interface.Mapping.Map;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

import ABSTRACT.com.commander4j.Connector.InboundConnectorABSTRACT;
import INTERFACE.com.commander4j.Connector.InboundConnectorINTERFACE;

public abstract class InboundInterfaceABSTRACT extends TimerTask
{
	boolean enabled = false;
	//private String type = "";
	public InboundConnectorABSTRACT connector;

	private boolean running = false;
	private Timer timer;
	protected Map map;
	protected Document data;

	private String inputFilename = "";;
	private String id = "";

	protected Utility util = new Utility();
	private boolean binaryFile = false;
	private JPropQuickAccess qa = new JPropQuickAccess();

	Logger logger = org.apache.logging.log4j.LogManager.getLogger((InboundInterfaceABSTRACT.class));
	
	public boolean isBinaryFile()
	{
		return binaryFile;
	}

	public void setBinaryFile(boolean binaryFile)
	{
		this.binaryFile = binaryFile;
	}
		
	public Map getMap()
	{
		return map;
	}

	
	public String getMapId()
	{
		return this.map.getId();
	}

	public String getXSLTFilename()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//xsl//XSLT");
	}

	public String getXSLTPath()
	{
		String temp = qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//xsl//XSLTPath");
		
		if (temp.equals("")==true)
		{
			temp = qa.getString(Common.props, qa.getRootURL()+"//XSLTPath");
		}
		return temp;
	}

	public InboundInterfaceABSTRACT(Map map)
	{
		this.map = map;
	}

	public void processInboundData()
	{
		map.processInboundInterfaceToMap(getFilename(), getData());
	}

	public String getFilename()
	{
		return this.inputFilename;
	}

	public String getIdocSchemaFilename()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//idoc//idocSchemaFilename");
	}
	
	public Document getData()
	{
		return this.data;
	}

	public boolean getEnabled()
	{
		return this.enabled;
	}

	public void setInputFileMask(String[] mask)
	{
		qa.setValue(Common.props, qa.getMapInputURL(getMapId(), getId())+"//url//mask",mask);
	}

	public String[] getInputFileMask()
	{		
		return qa.getStringArray(Common.props, qa.getMapInputURL(getMapId(), getId())+"//url//mask");
	}
	
	public String getPrefix()
	{	
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//url//prefix");
	}

	public void setInputFilename(String filename)
	{
		this.inputFilename = filename;
	}

	public String getInputPath()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//url//path");
	}

	public void setEnabled(boolean enable)
	{
		logger.debug("setEnabled " + String.valueOf(enable));
		if ((enable == true) && (enabled == false) && (running == false))
		{
			// start
			timer = new Timer("Input_"+getMapId()+"_"+getId());
			connector.setEnabled(enabled);
			this.enabled = enable;
			setRunning(true);
			
			logger.debug("Start Requested : [" + qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//description") + "]");
			timer.schedule(this, 0, qa.getLong(Common.props, qa.getMapInputURL(getMapId(), getId())+"//url//pollingInterval"));
			
		} else
		{
			// stop
			timer.cancel();
			logger.debug("Stop Requested : [" + qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//description") + "]");
			setRunning(false);
		}
	}

	private void setRunning(boolean yes)
	{
		this.running = yes;
	}

	public boolean isRunning()
	{
		return this.running;
	}

	public String getType()
	{	
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//type");
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public String getInputPattern()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//ascii//pattern");
	}
	
	public String getCSVOptions()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//csv//csvOptions");
	}

	public String getOptionDelimeter()
	{
		return qa.getString(Common.props, qa.getMapInputURL(getMapId(), getId())+"//csv//optionDelimeter");
	}

	public String[] getFileMask(String type)
	{
		String[] result; 
		switch (type)
		{
		case InboundConnectorINTERFACE.Connector_PDF_PRINT:
			result = InboundConnectorINTERFACE.Mask_PDF_PRINT;
			break;
		case InboundConnectorINTERFACE.Connector_ASCII:
			result=InboundConnectorINTERFACE.Mask_ASCII;
			break;
		case InboundConnectorINTERFACE.Connector_CSV:
			result=InboundConnectorINTERFACE.Mask_CSV;
			break;
		case InboundConnectorINTERFACE.Connector_EMAIL:
			result=InboundConnectorINTERFACE.Mask_EMAIL;
			break;
		case InboundConnectorINTERFACE.Connector_RAW:
			result=InboundConnectorINTERFACE.Mask_RAW;
			break;			
		case InboundConnectorINTERFACE.Connector_Excel:
			result=InboundConnectorINTERFACE.Mask_Excel;
			break;			
		case InboundConnectorINTERFACE.Connector_IDOC:
			result=InboundConnectorINTERFACE.Mask_IDOC;
			break;
		case InboundConnectorINTERFACE.Connector_XML:
			result=InboundConnectorINTERFACE.Mask_XML;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return result;

	}
	
	public void setType(String type)
	{
		switch (type)
		{
		case InboundConnectorINTERFACE.Connector_PDF_PRINT:
			setBinaryFile(true);
			connector = new InboundConnectorPDF_PRINT((InboundInterface) this);
			break;
		case InboundConnectorINTERFACE.Connector_ASCII:
			setBinaryFile(false);
			connector = new InboundConnectorASCII((InboundInterface) this);
			break;
		case InboundConnectorINTERFACE.Connector_CSV:
			setBinaryFile(false);
			connector = new InboundConnectorCSV((InboundInterface) this);
			break;
		case InboundConnectorINTERFACE.Connector_EMAIL:
			setBinaryFile(true);
			connector = new InboundConnectorEmail((InboundInterface) this);
			break;
		case InboundConnectorINTERFACE.Connector_RAW:
			setBinaryFile(true);
			connector = new InboundConnectorRAW((InboundInterface) this);
			break;			
		case InboundConnectorINTERFACE.Connector_Excel:
			setBinaryFile(false);
			connector = new InboundConnectorExcel((InboundInterface) this);
			break;			
		case InboundConnectorINTERFACE.Connector_IDOC:
			setBinaryFile(false);
			connector = new InboundConnectorIDOC((InboundInterface) this);
			break;
		case InboundConnectorINTERFACE.Connector_XML:
			setBinaryFile(false);
			connector = new InboundConnectorXML((InboundInterface) this);
			break;
		default:
			throw new IllegalArgumentException();
		}

	}

}
