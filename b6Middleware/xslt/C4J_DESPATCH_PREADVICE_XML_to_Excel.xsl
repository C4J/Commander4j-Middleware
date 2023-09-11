<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:c4j="http://www.commander4j.com"
	xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation"
	exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">
	
	<xsl:output encoding="UTF-8" indent='yes' method="xml" />
	<xsl:strip-space  elements="*"/>
	<xsl:template match="text() | @*"/>
	<xsl:variable name="fromLocation" select="/message/messageData/despatchPreAdvice/fromLocation"/>
	<xsl:variable name="toLocation" select="/message/messageData/despatchPreAdvice/toLocation"/>
	<xsl:variable name="despatchNo" select="/message/messageData/despatchPreAdvice/despatchNo"/>
	<xsl:variable name="despatchDate" select="/message/messageData/despatchPreAdvice/despatchDate"/>
	<xsl:variable name="palletCount" select="count(/message/messageData/despatchPreAdvice/contents/pallet/SSCC)"/>
	
	<xsl:template match="/message/messageData/despatchPreAdvice">
		
		<excel sheets="1">
			<xsl:attribute name="filename">
				<xsl:value-of select="$fromLocation"/>_<xsl:value-of select="$toLocation"/>_<xsl:value-of select="$despatchNo"/>
			</xsl:attribute>

			<sheet id="1" name="Sheet 1" cols="12">
				<xsl:attribute name="rows">
					<xsl:value-of select="$palletCount"/>
				</xsl:attribute>

				<header>
					<col id="1" type="string">Despatch No</col>
					<col id="2" type="isodate">Despatch Date</col>
					<col id="3" type="string">SSCC</col>
					<col id="4" type="string">Material</col>
					<col id="5" type="string">Description</col>
					<col id="6" type="string">Batch</col>
					<col id="7" type="double">Quantity</col>
					<col id="8" type="string">UOM</col>
					<col id="9" type="isodate">Production Date</col>
					<col id="10" type="isodate">Expiry Date</col>
					<col id="11" type="string">SSCC Status</col>
					<col id="12" type="string">Batch Status</col>
				</header>

				<xsl:apply-templates select='contents/pallet' />
				
			</sheet>

		</excel>	
		
	</xsl:template>
	
	<xsl:template match="contents/pallet">
		
			<row>
				<xsl:attribute name="id">
					<xsl:value-of select="item"/>
				</xsl:attribute>
				
				<col id="1"><xsl:value-of select="$despatchNo"/></col>
				<col id="2"><xsl:value-of select="$despatchDate"/></col>
				<col id="3"><xsl:value-of select="SSCC"/></col>
				<col id="4"><xsl:value-of select="material"/></col>
				<col id="5"><xsl:value-of select="materialDescription"/></col>
				<col id="6"><xsl:value-of select="batch"/></col>	
				<col id="7"><xsl:value-of select="quantity"/></col>
				<col id="8"><xsl:value-of select="UOM"/></col>
				<col id="9"><xsl:value-of select="productionDate"/></col>
				<col id="10"><xsl:value-of select="bestBefore"/></col>
				<col id="11"><xsl:value-of select="status"/></col>
				<col id="12"><xsl:value-of select="batchStatus"/></col>
			</row>

	</xsl:template>	
	
</xsl:stylesheet>
