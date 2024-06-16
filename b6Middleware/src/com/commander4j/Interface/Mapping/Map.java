package com.commander4j.Interface.Mapping;

import java.util.LinkedList;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.commander4j.Interface.Inbound.InboundInterface;
import com.commander4j.Interface.Outbound.OutboundInterface;
import com.commander4j.util.Utility;

public class Map implements Comparable<Map>
{
	boolean enabled = true;
	InboundInterface inboundInterface;
	LinkedList<OutboundInterface> outboundInterface = new LinkedList<OutboundInterface>();
	Logger logger = org.apache.logging.log4j.LogManager.getLogger((Map.class));

	private String id = "";
	private String description = "";
	private String emailEnabled = "Y";
	private Long inboundMapMsgCount = (long) 0;
	private Long outboundMapMsgCount = (long) 0;
	private Utility util = new Utility();
	
	public void run()
	{

	}

	public Long getInboundMapMessageCount()
	{
		return inboundMapMsgCount;
	}

	public void resetOutboundMapMessageCount()
	{
		outboundMapMsgCount = (long) 0;
		
		int OutboundIntCount = getNumberofOutboundInterfaces();
		
		if (OutboundIntCount > 0)
		{
			for (int x = 0; x < OutboundIntCount; x++)
			{
				getOutBoundInterface(x).connector.resetOutBoundConnectorCount();
			}
		}
	}
	
	public void resetInboundMapMessageCount()
	{
		inboundMapMsgCount = (long) 0;
		
		getInboundInterface().connector.resetInBoundConnectorCount();
	}	
	
	public Long getOutboundMapMessageCount()
	{
		return outboundMapMsgCount;
	}

	public String toString()
	{
		int OutboundIntCount = getNumberofOutboundInterfaces();
		String outboundTypeList = "";
		String outboundPathList = "";

		if (OutboundIntCount > 0)
		{
			for (int x = 0; x < OutboundIntCount; x++)
			{	
				if (x > 0)
				{
					outboundTypeList = outboundTypeList + "+";
				}
				outboundTypeList = outboundTypeList + getOutBoundInterface(x).getType();
				outboundPathList = outboundPathList + "<br><font color='red'>" +util.padString("",true,70," ")+util.padString(getOutBoundInterface(x).getType(),true,10," ")+"   "+util.padString(getOutBoundInterface(x).connector.getOutboundConnectorCount().toString(),true,5," ")+"   "+getOutBoundInterface(x).getOutputPath();
				
//				System.out.println("----------------------------");
//				System.out.println("Map ID                      :"+getId());
//				System.out.println("Map Description             :"+getDescription());
//				System.out.println("Map InboundMapMessageCount  :"+getInboundMapMessageCount());
//				System.out.println("Map OutboundMapMessageCount :"+getOutboundMapMessageCount());
//				System.out.println("Interface ID                :"+getOutBoundInterface(x).getId());
//				System.out.println("Interface Description       :"+getOutBoundInterface(x).getDescription());
//				System.out.println("Connector Type              :"+getOutBoundInterface(x).connector.getType());
//				System.out.println("Connector Path              :"+getOutBoundInterface(x).connector.getPath());
//				System.out.println("Connector Count             :"+getOutBoundInterface(x).connector.getOutboundConnectorCount());
//				System.out.println("----------------------------");
			}
		}

		
		String result = "<html><font color='blue'>"+
						util.padString(getId(), true, 7, " ") + 
						"  " + 
						util.padString(getDescription(), true, 35, " ") + 
						"<font color='green'>"+
						util.padString(getInboundMapMessageCount().toString(), false, 8, " ") + 
						" " + "<font color='red'>" + 
						util.padString(getOutboundMapMessageCount().toString(), false, 8, " ") + 
						"         "+"<font color='green'>"+
						util.padString(getInboundInterface().getType(), true, 10, " ") + 
						"   " + 
						util.padString(getInboundInterface().connector.getInboundConnectorMessageCount().toString(),true,5," ")+
						"   " + 
						getInboundInterface().getInputPath()+outboundPathList+"</html>";
		
		result = result.replace(" ", "&nbsp;");
		
		return result;
	}

	public void setId(String ID)
	{
		id = ID;
	}

	public void setEmailEnabled(String email)
	{
		emailEnabled = util.replaceNullStringwithBlank(email);
		if (emailEnabled.equals(""))
		{
			emailEnabled = "Y";
		}
	}
	
	public String getEmailEnabled()
	{
		return emailEnabled;
	}
	
	public boolean isMapEmailEnabled()
	{
		if (getEmailEnabled().equals("Y"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setDescription(String desc)
	{
		description = desc;
	}

	public String getId()
	{
		return id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setEnabled(boolean yesno)
	{
		logger.debug("Map [" + getId() + "] Description [" + getDescription() + "] setEnabled " + String.valueOf(yesno));
		if (yesno == true)
		{
			for (int x = 0; x < outboundInterface.size(); x++)
			{
				outboundInterface.get(x).setEnabled(yesno);
			}
			this.inboundInterface.setEnabled(yesno);
		} else
		{
			this.inboundInterface.setEnabled(yesno);
			for (int x = 0; x < outboundInterface.size(); x++)
			{
				outboundInterface.get(x).setEnabled(yesno);
			}
		}

	}

	public InboundInterface getInboundInterface()
	{
		return this.inboundInterface;
	}

	public int getNumberofOutboundInterfaces()
	{
		int result = outboundInterface.size();
		return result;
	}

	public OutboundInterface getOutBoundInterface(int index)
	{
		return outboundInterface.get(index);
	}

	public void setInboundInterface(InboundInterface inint)
	{
		this.inboundInterface = inint;
	}

	public void addOutboundInterface(OutboundInterface outint)
	{
		outboundInterface.addLast(outint);
	}

	public void processMapToOutboundInterface(String filename, OutboundInterface outint, Document data)
	{
		logger.debug(">> processMapToOutboundInterface [" + filename + " - " + util.getStringFromDocument(data) + "]");
		outboundMapMsgCount++;
		logger.debug(">> outboundMapMessages count [" + getOutboundMapMessageCount().toString() + "]");
		outint.processInterfaceToConnector(filename, data);
	}

	public void processInboundInterfaceToMap(String filename, Document data)
	{

		logger.debug("<< processInboundInterfaceToMap  [" + filename + " - " + util.getStringFromDocument(data) + "]");
		inboundMapMsgCount++;
		logger.debug(">> inboundMapMessages count [" + getInboundMapMessageCount().toString() + "]");
		for (int x = 0; x < outboundInterface.size(); x++)
		{
			processMapToOutboundInterface(filename, outboundInterface.get(x), data);

		}

	}

	@Override
	public int compareTo(Map o)
	{
		String id = o.getId();
		int result = this.getId().compareTo(id);
		if (result > 0) result = 1;
		if (result < 0) result = -1;
		
		return result;
	}

}
