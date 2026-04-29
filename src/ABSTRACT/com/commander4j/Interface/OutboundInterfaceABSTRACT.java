package ABSTRACT.com.commander4j.Interface;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Connector.Outbound.OutboundConnectorASCII;
import com.commander4j.Connector.Outbound.OutboundConnectorCSV;
import com.commander4j.Connector.Outbound.OutboundConnectorEmail;
import com.commander4j.Connector.Outbound.OutboundConnectorExcel;
import com.commander4j.Connector.Outbound.OutboundConnectorIDOC;
import com.commander4j.Connector.Outbound.OutboundConnectorMQTT;
import com.commander4j.Connector.Outbound.OutboundConnectorPDF_PRINT;
import com.commander4j.Connector.Outbound.OutboundConnectorRAW;
import com.commander4j.Connector.Outbound.OutboundConnectorSOCKET;
import com.commander4j.Connector.Outbound.OutboundConnectorXML;
import com.commander4j.Interface.Mapping.Map;
import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

import ABSTRACT.com.commander4j.Connector.OutboundConnectorABSTRACT;
import INTERFACE.com.commander4j.Connector.OutboundConnectorINTERFACE;

public abstract class OutboundInterfaceABSTRACT extends TimerTask
{
	boolean enabled = false;
	private String type = "";
	public OutboundConnectorABSTRACT connector;
	private Long timerFrequency = (long) 2000;
	private boolean running = false;
	Timer timer;
	protected Map map;
	protected Document data;

	private String id;
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((OutboundInterfaceABSTRACT.class));

	protected Utility util = new Utility();
	private JPropQuickAccess qa = new JPropQuickAccess();

	public Map getMap()
	{
		return map;
	}

	public String getHostIP()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//host//ip");
	}

	public int getHostPort()
	{
		return qa.getInteger(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//host//port");
	}

	public int getHostRepeat()
	{
		return qa.getInteger(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//host//repeat");
	}

	public String getMapId()
	{
		return this.map.getId();
	}

	public String getMQTTTopic()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//mqtt//mqttTopic");
	}

	public String getMQTTContentXPath()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//mqtt//mqttContentXPath");
	}

	public String getMQTBroker()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//mqtt//mqttBroker");
	}

	public int getMQTTQos()
	{
		return qa.getInteger(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//mqtt//mqttQos");
	}

	public String getMQTTClient()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//mqtt//mqttClient");
	}

	public String getQueueName()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//print//queueName");
	}
	
	public boolean retainOriginalFilename()
	{
		return qa.getBoolean(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//url//retainOriginalFilename");
	}

	public String get83GUIDFilename(String prefix, String originalFilename)
	{
		String result = originalFilename;

		prefix = util.replaceNullStringwithBlank(prefix);

		int prefixLength = prefix.length();

		if (is83GUIDFilenameReqd())
		{

			String uuid = UUID.randomUUID().toString().replace("-", "");

			uuid = uuid.substring(0, 8 - prefixLength);

			result = prefix + uuid;

			logger.debug("Filename changed from [" + originalFilename + "] to [" + result + "]");
		}
		else
		{
			if (retainOriginalFilename())
			{
				result = prefix + originalFilename;
			}
			else
			{
				String gap = "";
				if (prefixLength > 0)
				{
					gap = "_";
				}
				result = prefix + gap + map.getId() + "_" + getId() + "_" + originalFilename;
			}
		}

		return result;
	}

	public boolean is83GUIDFilenameReqd()
	{
		return qa.getBoolean(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//url//use83GUID");
	}

	public String getCompareParam1()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//condition//param1");
	}

	public String getCompareParam1_Type()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//condition//param1_Type");
	}

	public String getCompareParam2()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//condition//param2");
	}

	public String getCompareParam2_Type()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//condition//param2_Type");
	}

	public String getComparator()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//condition//comparitor");
	}

	public String getPrefix()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//url//prefix");
	}

	public String getDescription()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//description");
	}

	public String getXSLTFilename()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//xsl//XSLT");
	}

	public OutboundInterfaceABSTRACT(Map map)
	{
		this.map = map;
	}

	public Document getData()
	{
		return this.data;
	}

	public boolean getEnabled()
	{
		return this.enabled;
	}

	public String getOutputFileExtension()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//url//fileExtension");
	}

	public String getOutputPath()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//url//path");
	}

	public void setEnabled(boolean enable)
	{
		logger.debug("setEnabled " + String.valueOf(enable));
		if ((enable == true) && (enabled == false) && (running == false))
		{
			// start
			timer = new Timer("Output_" + getMapId() + "_" + getId());
			connector.setEnabled(enabled);
			this.enabled = enable;
			setRunning(true);
			logger.debug("Start Requested : [" + getDescription() + "]");
			timer.schedule(this, 0, timerFrequency);
		}
		else
		{
			// stop
			timer.cancel();
			logger.debug("Stop Requested : [" + getDescription() + "]");
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
		return type;
	}

	public String getOutputPattern()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//ascii//pattern");
	}

	public String getCSVOptions()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//csv//csvOptions");
	}

	public String getOptionDelimeter()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//csv//optionDelimeter");
	}

	public String getFileMask(String type)
	{
		String result = "";

		switch (type)
		{
		case OutboundConnectorINTERFACE.Connector_PDF_PRINT:
			result = "pdf";
			break;
		case OutboundConnectorINTERFACE.Connector_ASCII:
			result = "txt";
			break;
		case OutboundConnectorINTERFACE.Connector_CSV:
			result = "csv";
			break;
		case OutboundConnectorINTERFACE.Connector_Excel:
			result = "xlsx";
			break;
		case OutboundConnectorINTERFACE.Connector_EMAIL:
			result = "";
			break;
		case OutboundConnectorINTERFACE.Connector_IDOC:
			result = "idoc";
			break;
		case OutboundConnectorINTERFACE.Connector_XML:
			result = "xml";
			break;
		case OutboundConnectorINTERFACE.Connector_MQTT:
			result = "xml";
			break;
		case OutboundConnectorINTERFACE.Connector_SOCKET:
			result = "xml";
			break;
		case OutboundConnectorINTERFACE.Connector_RAW:
			result = "";
			break;
		default:
			throw new IllegalArgumentException();
		}

		return result;
	}

	public void setType(String type)
	{
		this.type = type;

		switch (type)
		{
		case OutboundConnectorINTERFACE.Connector_PDF_PRINT:
			connector = new OutboundConnectorPDF_PRINT((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_ASCII:
			connector = new OutboundConnectorASCII((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_CSV:
			connector = new OutboundConnectorCSV((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_Excel:
			connector = new OutboundConnectorExcel((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_EMAIL:
			connector = new OutboundConnectorEmail((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_IDOC:
			connector = new OutboundConnectorIDOC((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_XML:
			connector = new OutboundConnectorXML((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_MQTT:
			connector = new OutboundConnectorMQTT((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_SOCKET:
			connector = new OutboundConnectorSOCKET((OutboundInterface) this);
			break;
		case OutboundConnectorINTERFACE.Connector_RAW:
			connector = new OutboundConnectorRAW((OutboundInterface) this);
			break;
		default:
			throw new IllegalArgumentException();
		}

	}

	public String getEmailSubject()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//email//emailSubject");
	}

	public String getEmailMessage()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//email//emailMessage");
	}

	public String getEmailListID()
	{
		return qa.getString(Common.props, qa.getMapOutputURL(map.getId(), getId()) + "//email//emailListID");
	}

	public String getXSLTPath()
	{
		String temp = qa.getString(Common.props, qa.getMapOutputURL(getMapId(), getId()) + "//xsl//XSLTPath");

		if (temp.equals("") == true)
		{
			temp = qa.getString(Common.props, qa.getRootURL() + "//XSLTPath");
		}

		return temp;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

}
