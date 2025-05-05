<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:c4j="http://www.commander4j.com" xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation" xmlns:saxon="http://saxon.sf.net/" exclude-result-prefixes="saxon xs c4j c4j_XSLT_Ext" version="2.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>
	<xsl:variable name="bom_id" select="string(/xml/xml/HEADER/RECIPE_CODE)"/>
	<xsl:variable name="bom_version" select="string(/xml/xml/HEADER/RECIPE_VERSION)"/>
	<xsl:template match="/xml/xml">
		<message>
		<hostRef>service</hostRef>
		<messageRef><xsl:value-of select="$bom_id"/>/<xsl:value-of select="$bom_version"/></messageRef>
		<interfaceType>Bill of Material</interfaceType>
		<messageInformation>id=<xsl:value-of select="$bom_id"/> version=<xsl:value-of select="$bom_version"/></messageInformation>
		<interfaceDirection>Input</interfaceDirection>
		<messageDate>
			<xsl:value-of select="c4j_XSLT_Ext:subString(c4j_XSLT_Ext:date_DD_MMM_YY_to_ISO_Date(string(/xml/xml[1]/HEADER[1]/DATE[1])), 0, 10)"/>
			<xsl:text>T</xsl:text>
			<xsl:value-of select="/xml/xml[1]/HEADER[1]/TIME[1]"/>
		</messageDate>
		<messageData>
			<bom data_id="root" type="string" value="">
				<xsl:attribute name="id">
					<xsl:value-of select="$bom_id"/>
				</xsl:attribute>
				<xsl:attribute name="version">
					<xsl:value-of select="$bom_version"/>
				</xsl:attribute>

				<xsl:apply-templates select="HEADER"/>
			</bom>
		</messageData>
		</message>
	</xsl:template>
	<xsl:template match="HEADER">
		<element>
			<xsl:attribute name="data_id">description</xsl:attribute>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="RECIPE_DECRIPTION"/>
			</xsl:attribute>
		</element>
		<element>
			<xsl:attribute name="data_id">updated</xsl:attribute>
			<xsl:attribute name="type">timestamp</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="c4j_XSLT_Ext:subString(c4j_XSLT_Ext:date_DD_MMM_YY_to_ISO_Date(string(DATE)), 0, 10)"/>
				<xsl:text>T</xsl:text>
				<xsl:value-of select="TIME"/>
			</xsl:attribute>
		</element>
		<xsl:apply-templates select="PROCSS_STAGE"/>
	</xsl:template>

	<xsl:template match="PROCSS_STAGE">
		<xsl:variable name="stage_no" select="string(PROCESS_STAGE_NO)"/>

		<element>
			<xsl:attribute name="data_id">stage</xsl:attribute>
			<xsl:attribute name="type">string</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:value-of select="$stage_no"/>
			</xsl:attribute>
			<element data_id="input" type="string" value="input">
				<xsl:apply-templates select="LINE[IO = ('I')][ING_TYPE = ('IM')]"/>
			</element>
			<element data_id="output" type="string" value="output">
				<xsl:apply-templates select="LINE[IO = ('O')]"/>
			</element>
			
		</element>

	</xsl:template>

	<xsl:template match="LINE">
		<element data_id="material" type="string">
			<xsl:attribute name="value">
				<xsl:value-of select="ING_CODE"/>
			</xsl:attribute>

			<element data_id="description" type="string">
				<xsl:attribute name="value">
					<xsl:value-of select="ING_DESCRIPTION"/>
				</xsl:attribute>
			</element>

			<element data_id="type" type="string">
				<xsl:attribute name="value">
					<xsl:value-of select="ING_TYPE"/>
				</xsl:attribute>
			</element>

			<xsl:variable name="io" select="string(IO)"/>

			<xsl:if test="$io = 'I'">
				<xsl:variable name="desc" select="c4j_XSLT_Ext:lowercase(string(ING_DESCRIPTION))"/>
				
				<element data_id="location_id" type="string" value="">

				</element>
				
			</xsl:if>

			<element data_id="quantity" type="decimal">
				<xsl:attribute name="value">
					<xsl:value-of select="QUANTITY"/>
				</xsl:attribute>
			</element>

			<element data_id="uom" type="string">
				<xsl:attribute name="value">
					<xsl:value-of select="UOM"/>
				</xsl:attribute>
				<element data_id="gtin" type="string">
					<xsl:attribute name="value"> </xsl:attribute>
				</element>
				<element data_id="variant" type="string">
					<xsl:attribute name="value"> </xsl:attribute>
				</element>

			</element>

			<element data_id="sequence" type="string">
				<xsl:attribute name="value">
					<xsl:value-of select="ING_SEQ"/>
				</xsl:attribute>
			</element>

		</element>
	</xsl:template>

	<!-- ================
        FUNCTION getConfigItem
        ================ -->
	<xsl:function name="c4j:getConfigItem">
		<xsl:param name="type"/>
		<xsl:param name="string1"/>
		<xsl:variable name="item_info" select="document('configData.xml')/lookup"/>
		<xsl:value-of select="$item_info/item[@type = $type][@id = $string1]/value"/>
	</xsl:function>

	<!-- ================
        FUNCTION getReferenceItem
        ================ -->
	<xsl:function name="c4j:getReferenceItem">
		<xsl:param name="type"/>
		<xsl:param name="string1"/>
		<xsl:variable name="item_info" select="document('referenceData.xml')/lookup"/>
		<xsl:value-of select="$item_info/item[@type = $type][@id = $string1]/value"/>
	</xsl:function>
</xsl:stylesheet>
