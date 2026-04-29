package com.commander4j.Transformation;

import java.util.UUID;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class getUUID extends ExtensionFunctionDefinition
{
	
	UUID uuid = UUID.randomUUID();

	@Override
	public SequenceType[] getArgumentTypes()
	{
		return new SequenceType[]{};
	}

	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName("c4j_XSLT_Ext", "http://com.commander4j.Transformation", "getUUID");
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
	            	
	        		uuid = UUID.randomUUID();
	        		
	        		String result = uuid.toString();

	                return StringValue.makeStringValue(result);
	            }
	        };
	}
	
}
