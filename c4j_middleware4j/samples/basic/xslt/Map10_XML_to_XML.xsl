<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:c4j="http://www.commander4j.com" exclude-result-prefixes="xs" version="2.0">
	
	<xsl:output encoding="UTF-8" indent="yes" method="xml"/>
	<xsl:strip-space elements="*"/>
	
	<!-- Local Variables -->
	
	<xsl:variable name="DATENOW" select="current-dateTime()"/>
	<xsl:variable name="MESSAGEDATE" select="format-dateTime($DATENOW, '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]')"/>
	
	<xsl:template match="message">
		<ProductionPerformance>
			<ID><xsl:value-of select="/message/messageRef"/></ID>
			<Plant><xsl:value-of select="/message/messageData/productionDeclaration/plant"/></Plant>
			<PublishedDate><xsl:value-of select="$MESSAGEDATE"/></PublishedDate>
			<ProductionRequestID><xsl:value-of select="/message/messageData/productionDeclaration/processOrder"/></ProductionRequestID>
			<Recipe><xsl:value-of select="/message/messageData/productionDeclaration/recipe"/></Recipe>
			<MaterialDefinitionID><xsl:value-of select="/message/messageData/productionDeclaration/material"/></MaterialDefinitionID>
			<MaterialLotID><xsl:value-of select="/message/messageData/productionDeclaration/batch"/></MaterialLotID>
			<MaterialSubLotID>00<xsl:value-of select="/message/messageData/productionDeclaration/SSCC"/></MaterialSubLotID>
			<Plant><xsl:value-of select="/message/messageData/productionDeclaration/plant"/></Plant>
			<Warehouse><xsl:value-of select="/message/messageData/productionDeclaration/warehouse"/></Warehouse>
			<Type><xsl:value-of select="/message/messageData/productionDeclaration/storageType"/></Type>
			<Section><xsl:value-of select="/message/messageData/productionDeclaration/storageSection"/></Section>
			<Bin><xsl:value-of select="/message/messageData/productionDeclaration/storageBin"/></Bin>
			<Quantity><xsl:value-of select="/message/messageData/productionDeclaration/productionQuantity"/></Quantity>
			<UnitOfMeasure><xsl:value-of select="/message/messageData/productionDeclaration/productionUOM"/></UnitOfMeasure>
		</ProductionPerformance>
	</xsl:template>
</xsl:stylesheet>