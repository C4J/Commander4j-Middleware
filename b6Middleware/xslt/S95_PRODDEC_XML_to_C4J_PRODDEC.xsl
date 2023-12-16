<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:c4j="http://www.commander4j.com"
	xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation"
	exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">
	
	<xsl:output encoding="UTF-8" indent='yes' method="xml" />
	<xsl:strip-space  elements="*"/>
	
	<!-- CONFIG DATA -->
	<xsl:variable name="HOSTREF"><xsl:value-of select="c4j:getConfigItem('config','HostRef')"/></xsl:variable>
	<xsl:variable name="SAPUsername"><xsl:value-of select="c4j:getConfigItem('config','SAPUsername')"/></xsl:variable>
	
	<!-- Local Variables -->
	
	<xsl:variable name="DATENOW" select="current-dateTime()"/>
	<xsl:variable name="MESSAGEDATE" select="format-dateTime($DATENOW, '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]')"></xsl:variable>
	<xsl:variable name="LONG_SSCC" select="/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/MaterialSubLotID"/>	
	<xsl:variable name="SSCC" select="substring($LONG_SSCC,3,18)"/>
	
	<xsl:template match='ProductionPerformance'>
		
		<ProductionPerformance>
			<message>
				<hostRef><xsl:value-of select='$HOSTREF' /></hostRef>
				<messageRef><xsl:value-of select='/ProductionPerformance/ID' /></messageRef>
				<interfaceType>Production Declaration</interfaceType>
				<messageInformation>SSCC=<xsl:value-of select='$SSCC' /></messageInformation>
				<interfaceDirection>Input</interfaceDirection>
				<messageDate><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/globe_PostingDate'/></messageDate>
				<messageData>
					<productionDeclaration>
						<SSCC><xsl:value-of select='$SSCC' /></SSCC>
						<productionQuantity><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/Quantity/QuantityString' /></productionQuantity>
						<productionUOM><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/Quantity/UnitOfMeasure' /></productionUOM>
						<expiryDate><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/globe_ExpirationDate'/></expiryDate>
						<productionDate><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/globe_ProductionDate'/></productionDate>
						<batch><xsl:value-of select='/ProductionPerformance/ProductionResponse/SegmentResponse/MaterialProducedActual/MaterialLotID' /></batch>
						<processOrder><xsl:value-of select='c4j_XSLT_Ext:removeLeadingZeros(/ProductionPerformance/ProductionResponse/ProductionRequestID)' /></processOrder>
						<StorageModule><xsl:value-of select='/ProductionPerformance/ProductionResponse[1]/SegmentResponse[1]/MaterialProducedActual[1]/Location[1]/Location[1]/Location[1]/Location[1]/Location[1]/EquipmentID[1]' /></StorageModule>
						<confirmed>N</confirmed>
					</productionDeclaration>
				</messageData>	

			</message>
		</ProductionPerformance>
	</xsl:template>
	
	<!-- ================
        FUNCTION get config data 
        ================ -->
	
	<xsl:function name="c4j:getConfigItem">
		<xsl:param name="type"/>
		<xsl:param name="string1"/>
		
		<xsl:variable name="item_info" select="document('configData.xml')/lookup"/>
		
		<xsl:value-of select="$item_info/item[@type=$type][@id=$string1]/value"/>
		
	</xsl:function>
	
		<!-- ================
        FUNCTION get reference data 
        ================ -->
	
	<xsl:function name="c4j:getReferenceItem">
		<xsl:param name="type"/>
		<xsl:param name="string1"/>
		
		<xsl:variable name="item_info" select="document('referenceData.xml')/lookup"/>
		
		<xsl:value-of select="$item_info/item[@type=$type][@id=$string1]/value"/>
		
	</xsl:function>

</xsl:stylesheet>
