<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <!-- Identity transform -->
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>

   <!-- Update <version>2</version> to <version>3</version> -->
   <xsl:template match="config/version[text()='2']">
      <version>3</version>
   </xsl:template>

   <!-- Handle <output>/<url> where <retainOriginalFilename> is not present -->
   <xsl:template match="output/url[not(retainOriginalFilename)]">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
         <!-- Conditionally set retainOriginalFilename based on ../type -->
         <xsl:choose>
            <xsl:when test="../type='RAW' or ../type='EMAIL'">
               <retainOriginalFilename>true</retainOriginalFilename>
            </xsl:when>
            <xsl:otherwise>
               <retainOriginalFilename>false</retainOriginalFilename>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
