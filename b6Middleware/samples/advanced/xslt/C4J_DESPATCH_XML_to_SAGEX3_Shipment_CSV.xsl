<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:c4j="http://www.commander4j.com"
	xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation"
	exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">
	
	<xsl:output encoding="UTF-8" indent='yes' method="xml" />
	<xsl:strip-space  elements="*"/>
	<xsl:template match="text() | @*"/>
	<xsl:variable name="despatchNo" select="/message/messageData/despatchConfirmation/despatchNo"/>
	<xsl:variable name="despatchDate" select="/message/messageData/despatchConfirmation/despatchDate"/>
	<xsl:variable name="despatchYear" select="substring($despatchDate,1,4)"/>
	<xsl:variable name="despatchMonth" select="substring($despatchDate,6,2)"/>
	<xsl:variable name="despatchDay" select="substring($despatchDate,9,2)"/>
	<xsl:variable name="palletCount" select="count(/message/messageData/despatchConfirmation/contents/pallet/SSCC)"/>
	
	<xsl:template match="message/messageData/despatchConfirmation">
		
		<data type="CSV">
				
			<row id="*" rows="1" cols="11">
				<col id="1">H</col>
				<col id="2">LIV</col>
				<col id="3">LIV</col>
				<col id="4"></col>
				<col id="5">SDH</col>
				<col id="6"><xsl:value-of select="/message/messageData/despatchConfirmation/toLocation"/></col>
				<col id="7">PST</col>
				<col id="8"><xsl:value-of select="$despatchYear"/><xsl:value-of select="$despatchMonth"/><xsl:value-of select="$despatchDay"/></col>
				<col id="9"><xsl:value-of select="$despatchYear"/><xsl:value-of select="$despatchMonth"/><xsl:value-of select="$despatchDay"/></col>
				<col id="10">1</col>
				<col id="11"><xsl:value-of select="$palletCount"/></col>
			</row>
			
			<xsl:apply-templates select='contents/pallet' />

		</data>	
		
	</xsl:template>
	
	
	<xsl:template match="contents/pallet">
		
		<xsl:for-each select="material[not(.=preceding::*)]">
			<xsl:sort select="material"/>
			<xsl:variable name="materialList" select="."></xsl:variable>

			<row id="*" rows="1" cols="6">
				<col id="1">L</col>
				<col id="2"></col>
				<col id="3">0</col>
				<col id="4"><xsl:value-of select="$materialList"/></col>
				<col id="5"><xsl:value-of select="./../UOM"/></col>
				<col id="6"><xsl:value-of select="sum(/message/messageData/despatchConfirmation/contents/pallet/material[.=$materialList]/../quantity)"/></col>
			</row>
			
			<xsl:for-each select="/message/messageData/despatchConfirmation/contents/pallet/material[.=$materialList]/..">
				<xsl:sort select="."/>
				<xsl:variable name="palletNumber" select="./SSCC"></xsl:variable>
				<xsl:variable name="batchOverride" select="./customerBatchOverride"/>
				<xsl:variable name="batchFormat" select="./customerBatchFormat"/>
				
			    <row id="*" rows="1" cols="7">
					<col id="1">S</col>
					<col id="2">A</col>
					<col id="3"><xsl:value-of select="./UOM"/></col>
					<col id="4">-<xsl:value-of select="./quantity"/></col>
			        <col id="5">DESP</col>	
			    	
			    	<xsl:if test="$batchOverride='N'">
			    		<!--<Default Format>-->
			    		<col id="6"><xsl:value-of select="batch"/><xsl:value-of select="substring($palletNumber,12,3)"/></col>
			    	</xsl:if>
			    	
			    	<xsl:if test="$batchOverride='Y'">	
			    		<xsl:if test="$batchFormat='{YEAR1}{JULIAN_DAY}{PLANT}'">
			    			<!--<NPPE Format>-->
			    			<col id="6"><xsl:value-of select="./batch"/></col>
			    		</xsl:if>	
			    	</xsl:if>				    	
			    	
			    	<col id="7"><xsl:value-of select="substring($palletNumber,15,3)"/></col>
			    	
				</row>	
			</xsl:for-each>		

		</xsl:for-each>	

	</xsl:template>	
	
</xsl:stylesheet>
