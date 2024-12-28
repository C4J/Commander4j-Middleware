<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" 
    exclude-result-prefixes="xs math" version="3.0">

    <xsl:output encoding="UTF-8" indent="yes" method="xml"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <config>
            <xsl:apply-templates select="config"/>
        </config>
    </xsl:template>

    <xsl:template match="config">
        <version>2</version>
        <description>
            <xsl:value-of select="@description"/>
        </description>
        <logPath>
            <xsl:value-of select="string(logPath)"/>
        </logPath>
        <XSLTPath>
            <xsl:value-of select="string(XSLTPath)"/>
        </XSLTPath>
        <logArchiveRetentionDays>
            <xsl:value-of select="logArchiveRetentionDays"/>
        </logArchiveRetentionDays>
        <retryOpenFileCount>
            <xsl:value-of select="retryOpenFileCount"/>
        </retryOpenFileCount>
        <retryOpenFileDelay>
            <xsl:value-of select="retryOpenFileDelay"/>
        </retryOpenFileDelay>
        <enableEmailNotifications>
            <xsl:value-of select="enableEmailNotifications"/>
        </enableEmailNotifications>
        <statusReportTime>
            <xsl:value-of select="statusReportTime"/>
        </statusReportTime>
        <maps>
            <xsl:apply-templates select="map"/>
        </maps>
    </xsl:template>

    <xsl:template match="map">
        <map>
            <id>
                <xsl:value-of select="@id"/>
            </id>
            <enabled>
                <xsl:variable name="mapEnabled" select="string(@enabled)"/>
                <xsl:if test="(($mapEnabled = 'N') or ($mapEnabled = 'n') or ($mapEnabled = ''))">false</xsl:if>
                <xsl:if test="(($mapEnabled = 'Y') or ($mapEnabled = 'y'))">true</xsl:if>
            </enabled>
            <description>
                <xsl:value-of select="@description"/>
            </description>
            <enableEmailNotifications>true</enableEmailNotifications>
            <connectors>
                <xsl:apply-templates select="input"/>

                <xsl:apply-templates select="output"/>
            </connectors>
        </map>
    </xsl:template>

    <xsl:template match="input">
        <input>
            <id>
                <xsl:value-of select="string(@id)"/>
            </id>
            <description>
                <xsl:value-of select="string(@description)"/>
            </description>
            <type>
                <xsl:value-of select="string(type)"/>
            </type>
            <xsl:if test="path != ''">
                <url>
                    <path>
                        <xsl:value-of select="string(path)"/>
                    </path>
                    <mask>
                        <xsl:value-of select="string(mask)"/>
                    </mask>
                    <prefix>
                        <xsl:value-of select="string(prefix)"/>
                    </prefix>
                    <pollingInterval>
                        <xsl:value-of select="pollingInterval"/>
                    </pollingInterval>
                </url>
            </xsl:if>

            <xsl:if test="(XSLT != '')">
                <xsl>
                    <XSLTPath>
                        <xsl:value-of select="string(XSLTPath)"/>
                    </XSLTPath>
                    <XSLT>
                        <xsl:value-of select="XSLT"/>
                    </XSLT>
                </xsl>
            </xsl:if>

            <xsl:if test="inputPattern != ''">
                <ascii>
                    <pattern>
                        <xsl:value-of select="string(inputPattern)"/>
                    </pattern>
                </ascii>
            </xsl:if>

            <xsl:if test="((optionDelimeter != '') or (csvOptions != ''))">
                <csv>
                    <optionDelimeter>
                        <xsl:value-of select="optionDelimeter"/>
                    </optionDelimeter>
                    <csvOptions>
                        <xsl:value-of select="csvOptions"/>
                    </csvOptions>
                </csv>
            </xsl:if>

            <xsl:if test="string(idocSchemaFilename) != ''">
                <idoc>
                    <idocSchemaFilename>
                        <xsl:value-of select="idocSchemaFilename"/>
                    </idocSchemaFilename>
                </idoc>
            </xsl:if>

        </input>
    </xsl:template>

    <xsl:template match="output">
        <output>
            <id>
                <xsl:value-of select="@id"/>
            </id>
            <enabled>
                <xsl:variable name="outputEnabled" select="string(@enabled)"/>
                <xsl:if test="(($outputEnabled = 'N') or ($outputEnabled = 'n') or ($outputEnabled = ''))">false</xsl:if>
                <xsl:if test="(($outputEnabled = 'Y') or ($outputEnabled = 'y'))">true</xsl:if>
            </enabled>
            <description>
                <xsl:value-of select="string(@description)"/>
            </description>
            <type>
                <xsl:value-of select="string(type)"/>
            </type>

            <xsl:if test="path != ''">
                <url>
                    <path>
                        <xsl:value-of select="string(path)"/>
                    </path>
                    <prefix>
                        <xsl:value-of select="string(prefix)"/>
                    </prefix>
                    <fileExtension>
                        <xsl:value-of select="string(outputFileExtension)"/>
                    </fileExtension>
                    <use83GUID>
                        <xsl:variable name="use83GUID" select="string(use83GUID)"/>
                        <xsl:if test="(($use83GUID = 'N') or ($use83GUID = 'n') or ($use83GUID = ''))">false</xsl:if>
                        <xsl:if test="(($use83GUID = 'Y') or ($use83GUID = 'y'))">true</xsl:if>
                    </use83GUID>
                </url>
            </xsl:if>

            <xsl:if test="(XSLT != '')">
                <xsl>
                    <XSLT>
                        <xsl:value-of select="XSLT"/>
                    </XSLT>
                    <XSLTPath>
                        <xsl:value-of select="XSLTPath"/>
                    </XSLTPath>
                </xsl>
            </xsl:if>

            <xsl:if test="outputPattern != ''">
                <ascii>
                    <pattern>
                        <xsl:value-of select="outputPattern"/>
                    </pattern>
                </ascii>
            </xsl:if>

            <xsl:if test="((optionDelimeter != '') or (csvOptions != ''))">
                <csv>
                    <optionDelimeter>
                        <xsl:value-of select="optionDelimeter"/>
                    </optionDelimeter>
                    <csvOptions>
                        <xsl:value-of select="csvOptions"/>
                    </csvOptions>
                </csv>
            </xsl:if>

            <xsl:if test="(host/ip != '')">
                <host>
                    <ip>
                        <xsl:value-of select="host/ip"/>
                    </ip>
                    <port>
                        <xsl:value-of select="host/socket"/>
                    </port>

                    <xsl:variable name="rep" select="string(host/repeat)"/>
                    <xsl:variable name="cop" select="string(copies)"/>

                    <xsl:if test="$rep != ''">
                        <repeat>
                            <xsl:value-of select="$rep"/>
                        </repeat>
                    </xsl:if>

                    <xsl:if test="(($rep = '') or ($rep = null))">
                        <xsl:if test="$cop != ''">
                            <repeat>
                                <xsl:value-of select="$cop"/>
                            </repeat>
                        </xsl:if>
                    </xsl:if>
                </host>
            </xsl:if>

            <xsl:if test="(emailListID != '')">
                <email>
                    <emailListID>
                        <xsl:value-of select="emailListID"/>
                    </emailListID>
                    <subject>
                        <xsl:value-of select="subject"/>
                    </subject>
                    <message>
                        <xsl:value-of select="message"/>
                    </message>
                </email>
            </xsl:if>

            <xsl:if test="((mqtt/broker != '') or (mqtt/topic != ''))">
                <mqtt>
                    <mqttBroker>
                        <xsl:value-of select="mqtt/broker"/>
                    </mqttBroker>
                    <mqttTopic>
                        <xsl:value-of select="mqtt/topic"/>
                    </mqttTopic>
                    <mqttClient>
                        <xsl:value-of select="mqtt/clientID"/>
                    </mqttClient>
                    <mqttContentXPath>
                        <xsl:value-of select="mqtt/contentXPath"/>
                    </mqttContentXPath>
                    <mqttQos>
                        <xsl:value-of select="mqtt/qos"/>
                    </mqttQos>
                </mqtt>
            </xsl:if>

            <xsl:if test="queueName != ''">
                <print>
                    <queueName>
                        <xsl:value-of select="queueName"/>
                    </queueName>
                </print>
            </xsl:if>

            <xsl:if test="((condition/param1 != '') or (condition/param2 != '') or (condition/comparitor != '') or (condition/param1/@type != '') or (condition/param2/@type != ''))">
                <condition>
                    <param1>
                        <xsl:value-of select="condition/param1"/>
                    </param1>
                    <param1_Type>
                        <xsl:value-of select="condition/param1/@type"/>
                    </param1_Type>
                    <param2>
                        <xsl:value-of select="condition/param2"/>
                    </param2>
                    <param2_Type>
                        <xsl:value-of select="condition/param2/@type"/>
                    </param2_Type>
                    <comparitor>
                        <xsl:value-of select="condition/comparitor"/>
                    </comparitor>
                </condition>
            </xsl:if>

        </output>
    </xsl:template>

</xsl:stylesheet>
