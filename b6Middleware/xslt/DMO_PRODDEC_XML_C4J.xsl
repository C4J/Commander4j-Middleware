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
	
	<xsl:variable name="MESSAGEDATE" select="/message/messageDate"></xsl:variable>
	<xsl:variable name="MESSAGEDATE_DAY" select="substring($MESSAGEDATE,1,10)"/>
	<xsl:variable name="MESSAGEDATE_TIME" select="substring($MESSAGEDATE,12,8)"/>	
	
	<xsl:variable name="EXPIRY_DATE" select="/message/messageData/productionDeclaration/expiryDate"/>							

	<xsl:variable name="EXPIRY_SHORT" select="substring($EXPIRY_DATE,1,10)"/>
	<xsl:variable name="ORDER" select="/message/messageData/productionDeclaration/processOrder"/>
	<xsl:variable name="ORDER_SHORT" select="c4j_XSLT_Ext:removeLeadingZeros($ORDER)" />
	
	
	<xsl:variable name="PROD_DATE" select="/message/messageData/productionDeclaration/productionDate" />
	<xsl:variable name="PROD_DATE_DAY" select="substring($PROD_DATE,1,10)"/>
	<xsl:variable name="PROD_DATE_TIME" select="substring($PROD_DATE,12,8)"/>

	
	<xsl:template match='message'>
		
		<message>
			<hostRef>
				<xsl:value-of select='/message/hostRef'/>
			</hostRef>
			<messageRef>
				<xsl:value-of select='/message/messageRef'/>
			</messageRef>
			<interfaceType>
				<xsl:value-of select='/message/interfaceType'/>
			</interfaceType>
			<messageInformation>
				<xsl:value-of select='/message/messageInformation'/>
			</messageInformation>	
			<interfaceDirection>
				<xsl:value-of select='/message/interfaceDirection'/>
			</interfaceDirection>				
			<messageDate>
				<xsl:value-of select='$MESSAGEDATE_DAY'/>T<xsl:value-of select='$MESSAGEDATE_TIME'/>
			</messageDate>	
		
			<messageData>
				<productionDeclaration>
					<SSCC><xsl:value-of select='/message/messageData/productionDeclaration/SSCC'/></SSCC>
					<processOrder><xsl:value-of select='$ORDER_SHORT'/></processOrder>
					<batch><xsl:value-of select='/message/messageData/productionDeclaration/batch'/></batch>
					<expiryDate><xsl:value-of select='$EXPIRY_SHORT'/>T23:59:59</expiryDate>
					<productionQuantity><xsl:value-of select='/message/messageData/productionDeclaration/productionQuantity'/></productionQuantity>
					<productionUOM><xsl:value-of select='/message/messageData/productionDeclaration/productionUOM'/></productionUOM>
					<confirmed><xsl:value-of select='/message/messageData/productionDeclaration/confirmed'/></confirmed>
					<productionDate><xsl:value-of select='$PROD_DATE_DAY'/>T<xsl:value-of select='$PROD_DATE_TIME'/></productionDate>
				</productionDeclaration>
			</messageData>

		</message>
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
