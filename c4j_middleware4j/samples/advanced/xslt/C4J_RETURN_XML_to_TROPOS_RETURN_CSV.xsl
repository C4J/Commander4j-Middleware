<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:c4j="http://www.commander4j.com"
	xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation"
	exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">
	
	<xsl:output encoding="UTF-8" indent='yes' method="xml" />
	<xsl:strip-space  elements="*"/>
	<xsl:template match="text() | @*"/>
	<xsl:variable name="sscc" select="/message/messageData[1]/palletIssue[1]/SSCC"/>

	<xsl:template match="message/messageData/palletIssue">
		<data type="CSV">
				
			<row id="1" cols="14">
				<col id="1">Transaction</col>
				<col id="2">Subtype</col>
				<col id="3">Order</col>
				<col id="4">SSCC</col>
				<col id="5">Location</col>
				<col id="6">Lane</col>
				<col id="7">Quantity</col>
				<col id="8">UOM</col>
				<col id="9">Material</col>
				<col id="10">Recipe</col>
				<col id="11">Version</col>
				<col id="12">Lot Number</col>
				<col id="13">Transaction Date</col>
				<col id="14">Batch</col>
			</row>				
			<row id="2" cols="14">
				<col id="1">RETURN</col>
				<col id="2">FROM</col>
				<col id="3"><xsl:value-of select="processOrder"/></col>
				<col id="4"><xsl:value-of select="SSCC"/></col>
				<col id="5"><xsl:value-of select="location"/></col>
				<col id="6"><xsl:value-of select="barcodeId"/></col>
				<col id="7"><xsl:value-of select="quantity"/></col>
				<col id="8"><xsl:value-of select="uom"/></col>
				<col id="9"><xsl:value-of select="material"/></col>
				<col id="10"><xsl:value-of select="recipe"/></col>
				<col id="11"><xsl:value-of select="recipeVersion"/></col>
				<col id="12"><xsl:value-of select="old_code"/><xsl:value-of select="substring($sscc,8,9)"/></col>
				<col id="13"><xsl:value-of select="transactionDate"/></col>
				<col id="14"><xsl:value-of select="batch"/></col>
			</row>
						
		</data>			
	</xsl:template>

</xsl:stylesheet>
