<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:c4j="http://www.commander4j.com"
	xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation"
	exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">

	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>

	<xsl:variable name="sscc" select="/message/SSCC"/>
	<xsl:variable name="material" select="/message/material"/>
	<xsl:variable name="batch" select="/message/batch"/>
	<xsl:variable name="productionDate" select="/message/productionDate"/>

	<xsl:template match="message">

		<function_demo>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function substring(string,start,length)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			
			<input><xsl:value-of select="SSCC"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:subString($sscc, 8, 9)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function removeLeadingZeros(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			
			<input><xsl:value-of select="processOrder"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:removeLeadingZeros(processOrder)"/></output>
	
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function formatDate(string,string,string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			
			<input><xsl:value-of select="$productionDate"/></input>
                        <output><xsl:value-of select="c4j_XSLT_Ext:formatDate($productionDate, 'yyyy-MM-dd''T''HH:mm:ss', 'ddMMyy')"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function ISO_Date_to_date_MMYYYY(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="$productionDate"/></input>			
			<output><xsl:value-of select="c4j_XSLT_Ext:ISO_Date_to_date_MMYYYY($productionDate)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function ISO_Date_to_date_DD_MM_YYYY_HH_MM_SS(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="$productionDate"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:ISO_Date_to_date_DD_MM_YYYY_HH_MM_SS($productionDate)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function ISO_Date_to_date_DDMMYYYY(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="$productionDate"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:ISO_Date_to_date_DDMMYYYY($productionDate)"/></output>

			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function removeCommas(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="group1"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:removeCommas(group1)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function removeSpaces(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="group2"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:removeSpaces(group2)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function trim(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="storageSection"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:trim(storageSection)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function padVariant(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<storageSection>:<xsl:value-of select="storageSection"/>:</storageSection>
			<input><xsl:value-of select="variant"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:padVariant(variant)"/></output>
	
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function padEAN(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>	
			<input><xsl:value-of select="ean"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:padEAN(ean)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function padStringLeft(string,integer,character)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="plant"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:padStringLeft(plant,10,'X')"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function padStringRight(string,integer,character)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>	
			<input><xsl:value-of select="plant"/></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:padStringRight(plant,10,'X')"/></output>

			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function getISODateTimeString()</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<output><xsl:value-of select="c4j_XSLT_Ext:getISODateTimeString()" /></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function getISODateTimeFilenameString()</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<output><xsl:value-of select="c4j_XSLT_Ext:getISODateTimeFilenameString()" /></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function date_DDMMYYYY_to_ISO_Date(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="shortdate1" /></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:date_DDMMYYYY_to_ISO_Date(shortdate1)" /></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function date_DD_MM_YY_HH_MM_SS_to_ISO_Date(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="shortdate2" /></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:date_DD_MM_YY_HH_MM_SS_to_ISO_Date(shortdate2)" /></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function date_DD_MMM_YY_to_ISO_Date(string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input><xsl:value-of select="shortdate3" /></input>
			<output><xsl:value-of select="c4j_XSLT_Ext:date_DD_MMM_YY_to_ISO_Date(shortdate3)" /></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function concat(string,string)</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<input1><xsl:value-of select="$material" /></input1>
			<input2><xsl:value-of select="$batch" /></input2>
			<output><xsl:value-of select="c4j_XSLT_Ext:concat($material,$batch)"/></output>
			
			<xsl:text>&#xA;</xsl:text>
			<xsl:comment>Function getUUID()</xsl:comment>
			<xsl:text>&#xA;</xsl:text>
			<output><xsl:value-of select="c4j_XSLT_Ext:getUUID()" /></output>
			
		</function_demo>
		
	</xsl:template>


</xsl:stylesheet>
