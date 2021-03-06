package com.commander4j.Transformation;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class nvl extends ExtensionFunctionDefinition
{

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[]{SequenceType.OPTIONAL_STRING, SequenceType.SINGLE_STRING};
	}

	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName("c4j_XSLT_Ext", "http://com.commander4j.Transformation", "nvl");
	}

	@Override
	public SequenceType getResultType(SequenceType[] arg0)
	{
		 return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression()
	{
	       return new ExtensionFunctionCall() {
	            @Override
	            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
	            	
	            	String value;
	            	try
	            	{
	            		value =  arguments[0].head().getStringValue();
	            	} catch (ClassCastException ex)
	            	{
	            	   value = "";	
	            	}
	            	catch (NullPointerException ex)
	            	{
	            		value = "";	
	            	}
	            	
	                String defaultValue;
	            	try
	            	{
	            		defaultValue =  arguments[1].head().getStringValue();
	            	} catch (ClassCastException ex)
	            	{
	            		defaultValue = "";	
	            	}
	            	catch (NullPointerException ex)
	            	{
	            		defaultValue = "";	
	            	}
	            	
	        		String result = "";

	        		result = value;

	        		if (result.equals(""))
	        		{
	        			result = defaultValue;
	        		}

	                return StringValue.makeStringValue(result);
	            }
	        };
	}

}
