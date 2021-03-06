<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:c4j="http://www.commander4j.com"
    xmlns:c4j_XSLT_Ext="http://xml.apache.org/xalan/java/com.commander4j.Transformation.XSLTExtension"
    exclude-result-prefixes="xs c4j c4j_XSLT_Ext"  version="2.0">
    
    <xsl:output encoding="UTF-8" indent='yes' method="xml" />
    <xsl:strip-space  elements="*"/>


    <!-- CONFIG DATA -->
    <xsl:variable name="HOSTREF"><xsl:value-of select="c4j:getConfigItem('config','HostRef')"/></xsl:variable>
    
    <!-- Local Variables -->

    <xsl:variable name="ID" select="string(/TransferSchedule/ID[1])"></xsl:variable>
    <xsl:variable name="DATENOW" select="current-dateTime()"/>
    <xsl:variable name="MESSAGEDATE" select="format-dateTime($DATENOW, '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]')"></xsl:variable>
  
    <xsl:template match='TransferSchedule'>

        <message>
            <hostRef><xsl:value-of select="$HOSTREF" /></hostRef>
            <messageRef>ID <xsl:value-of select='$ID' /></messageRef>
            <interfaceType>Pallet Status Change</interfaceType>
            <messageInformation>TransferSchedule=<xsl:value-of select='$ID'/></messageInformation>
            <interfaceDirection>Input</interfaceDirection>
            <messageDate><xsl:value-of select="$MESSAGEDATE"/></messageDate>
            <messageData>
                <palletStatusChange>
                    <xsl:apply-templates select='TransferRequest/SegmentRequirement/MaterialProducedRequirement' />
                </palletStatusChange>
            </messageData>
        </message>
    </xsl:template>


    <xsl:template match="TransferRequest/SegmentRequirement/MaterialProducedRequirement">
        <xsl:variable name="SSCC_20" select="MaterialSubLotID" />
        <xsl:variable name="SSCC_18" select="substring($SSCC_20,3,18)" />
        <sscc><xsl:value-of select='$SSCC_18'/></sscc>
        <status><xsl:value-of select="globe_StockType"/></status>
        <update>
            <xsl:comment>>New XML structure for future enhancement</xsl:comment>
            <sscc><xsl:value-of select='$SSCC_18'/></sscc>
            <status><xsl:value-of select="globe_StockType"/></status> 
        </update>
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

