<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:c4j="http://www.commander4j.com" xmlns:c4j_XSLT_Ext="http://com.commander4j.Transformation" xmlns:saxon="http://saxon.sf.net/" exclude-result-prefixes="saxon xs c4j c4j_XSLT_Ext" version="2.0">

    <xsl:output encoding="UTF-8" indent="yes" method="xml"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="bom_id" select="string(/ProductInformation/ProductDefinition/BillOfMaterialsID)"/>
    <xsl:variable name="bom_version" select="string(/ProductInformation/ProductDefinition/BillOfMaterialsHeader/BOMAlternative)"/>

    <xsl:template match="/">
        <message>
            <xsl:apply-templates select="/ProductInformation"/>
        </message>
    </xsl:template>

    <xsl:template match="/ProductInformation">
        <xsl:variable name="originaldate" select="PublishedDate"/>
        <xsl:variable name="dateonly" select="c4j_XSLT_Ext:subString($originaldate, 0, 10)"/>
        <xsl:variable name="timeonly" select="c4j_XSLT_Ext:subString($originaldate, 11, 8)"/>
        <hostRef>service</hostRef>
        <messageRef><xsl:value-of select="$bom_id"/>/<xsl:value-of select="$bom_version"/></messageRef>
        <interfaceType>Bill of Materials</interfaceType>
        <messageInformation>BOM=<xsl:value-of select="$bom_id"/>/<xsl:value-of select="$bom_version"/></messageInformation>
        <interfaceDirection>Input</interfaceDirection>
        <messageDate>
            <xsl:value-of select="$dateonly"/>
            <xsl:text>T</xsl:text>
            <xsl:value-of select="$timeonly"/>
        </messageDate>
        <messageData>
            <bom data_id="root" type="string" value="">
                <xsl:attribute name="id">
                    <xsl:value-of select="$bom_id"/>
                </xsl:attribute>
                <xsl:attribute name="version">
                    <xsl:value-of select="$bom_version"/>
                </xsl:attribute>

                <element>
                    <xsl:attribute name="data_id">description</xsl:attribute>
                    <xsl:attribute name="type">string</xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:value-of select="Description"/>
                    </xsl:attribute>
                </element>

                <element>
                    <xsl:attribute name="data_id">updated</xsl:attribute>
                    <xsl:attribute name="type">timestamp</xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:value-of select="$dateonly"/>
                        <xsl:text>T</xsl:text>
                        <xsl:value-of select="$timeonly"/>
                    </xsl:attribute>
                </element>

                <element>
                    <xsl:attribute name="data_id">stage</xsl:attribute>
                    <xsl:attribute name="type">string</xsl:attribute>
                    <xsl:attribute name="value">01</xsl:attribute>

                    <element data_id="input" type="string" value="input">
                        <xsl:apply-templates select="ProductDefinition/ProductSegment/MaterialSpecification"/>
                    </element>
                    <element data_id="output" type="string" value="output">
                        <xsl:apply-templates select="ProductDefinition/BillOfMaterialsHeader"/>
                    </element>

                </element>

            </bom>
        </messageData>
    </xsl:template>

    <xsl:template match="ProductDefinition/ProductSegment/MaterialSpecification">

        <element data_id="material" type="string">
            <xsl:attribute name="value">
                <xsl:value-of select="c4j_XSLT_Ext:removeLeadingZeros(string(MaterialDefinitionID))"/>
            </xsl:attribute>


            <element data_id="description" type="string">
                <xsl:attribute name="value">
                </xsl:attribute>
            </element>

            <element data_id="type" type="string">
                <xsl:attribute name="value">
                    <xsl:value-of select="BillOfMaterialItem/BOMItemCategory"/>
                </xsl:attribute>
            </element>

            <element data_id="location_id" type="string" value=""/>

            <element data_id="quantity" type="decimal">
                <xsl:attribute name="value">
                    <xsl:value-of select="Quantity/QuantityString"/>
                </xsl:attribute>
            </element>

            <element data_id="uom" type="string">
                <xsl:attribute name="value">
                    <xsl:value-of select="Quantity/UnitOfMeasure"/>
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
                    <xsl:value-of select="BillOfMaterialItem/BOMItemNode"/>
                </xsl:attribute>
            </element>
        </element>

    </xsl:template>

    <xsl:template match="ProductDefinition/BillOfMaterialsHeader">

        <element data_id="material" type="string">
            <xsl:attribute name="value">
                <xsl:value-of select="c4j_XSLT_Ext:removeLeadingZeros(string(BillOfMaterialsMainMaterialSpecification/MaterialDefinitionID))"/>
            </xsl:attribute>
            
            
            <element data_id="description" type="string">
                <xsl:attribute name="value">
                    <xsl:value-of select="AltBOMText"/>
                </xsl:attribute>
            </element>
            
            <element data_id="type" type="string">
                <xsl:attribute name="value">
                    <xsl:apply-templates select="BillOfMaterialsMainMaterialSpecification/Quantity[Key = ('Base')]"/>
                </xsl:attribute>
            </element>
            
            <xsl:apply-templates select="BillOfMaterialsMainMaterialSpecification/Quantity[Key = ('Base')]"/>
            
            <element data_id="sequence" type="string">
                <xsl:attribute name="value">
                    <xsl:value-of select="BillOfMaterialItem/BOMItemNode"/>
                </xsl:attribute>
            </element>
            
        </element>
       
    </xsl:template>

    <xsl:template match="BillOfMaterialsMainMaterialSpecification/Quantity">
        
        <element data_id="quantity" type="decimal">
            <xsl:attribute name="value">
                <xsl:value-of select="c4j_XSLT_Ext:trim(QuantityString)"/>
            </xsl:attribute>
        </element>

        <element data_id="uom" type="string">
            <xsl:attribute name="value">
                <xsl:value-of select="UnitOfMeasure"/>
            </xsl:attribute>
            <element data_id="gtin" type="string">
                <xsl:attribute name="value"> </xsl:attribute>
            </element>
            <element data_id="variant" type="string">
                <xsl:attribute name="value"> </xsl:attribute>
            </element>
        </element>

    </xsl:template>


</xsl:stylesheet>
