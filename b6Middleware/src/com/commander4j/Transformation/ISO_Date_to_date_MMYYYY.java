package com.commander4j.Transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class ISO_Date_to_date_MMYYYY extends ExtensionFunctionDefinition
{

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[]{SequenceType.OPTIONAL_STRING};
	}

	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName("c4j_XSLT_Ext", "http://com.commander4j.Transformation", "ISO_Date_to_date_MMYYYY");
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
	            	
	            	String inputString;
	            	try
	            	{
	            		inputString = arguments[0].head().getStringValue();

	            	} catch (ClassCastException ex)
	            	{
	            		inputString = "";	
	            	}
	            	catch (NullPointerException ex)
	            	{
	            		inputString = "";	
	            	}

	        		String result = inputString;
	        		
	        		result = inputString.replace("T", " ");

	        		SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	        		SimpleDateFormat myFormat = new SimpleDateFormat("MMyyyy", Locale.ENGLISH);

	        		try
	        		{

	        			result = myFormat.format(fromUser.parse(result));

	        		}
	        		catch (ParseException e)
	        		{

	        			result = "Input date yyyy-MM-dd HH:mm:ss format error [" + result + "]";

	        		}
	                return StringValue.makeStringValue(result);
	            }
	        };
	}

}
