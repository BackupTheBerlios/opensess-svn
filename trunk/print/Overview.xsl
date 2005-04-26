<xsl:stylesheet version = '1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:variable name="sessions" select="/openconclave/@sessions"/>
<xsl:variable name="s" select="//selectedSolution/@index"/>
<xsl:variable name="sol" select="//solution[position() = $s]"/>

<xsl:template match="/">
  <html>
  <head>
    <title>OpenSess Solution Overview</title>
  </head>
  <body>
    <h1>
      OpenSess Solution Overview
    </h1>

    <table>
      <tr><td></td><xsl:apply-templates select="//topic" mode="names"/></tr>
      <tr><td></td><xsl:apply-templates select="//topic" mode="times"/></tr>
      <tr><td></td><xsl:apply-templates select="//topic" mode="locations"/></tr>
      <xsl:apply-templates select="//person"/>
    </table>    
  </body>
  </html> 
</xsl:template>

<xsl:template match="topic" mode="names">
  <xsl:variable name="t" select="position()"/>
  <td>
    <xsl:value-of select="//topic[position() = $t]/@name"/>
  </td>
</xsl:template>

<xsl:template match="topic" mode="times">
  <xsl:variable name="t" select="position()-1"/>
  <td>
    <xsl:for-each select="$sol//topicGroup">
      <xsl:if test="groupTopic[@index = $t]">
        <xsl:variable name="group" select="position()"/>
        <xsl:value-of select="//time[position() = $group]/@name"/>
      </xsl:if>
    </xsl:for-each>
  </td>
</xsl:template>

<xsl:template match="topic" mode="locations">
  <xsl:variable name="t" select="position()-1"/>
  <td>
    <xsl:for-each select="$sol//groupTopic">
      <xsl:if test="@index = $t">
       <xsl:variable name="target" select="."/>
         <xsl:variable name="session" select="((position()-1) mod $sessions) + 1"/>
        <xsl:value-of select="//location[position() = $session]/@name"/>
       </xsl:if>
    </xsl:for-each>
  </td>
</xsl:template>

<xsl:template match="person">
  <xsl:variable name="p" select="position()-1"/>

  <tr>
    <td><xsl:value-of select="@name"/></td>
       
    <xsl:for-each select="//topic">
      <xsl:variable name="t" select="position()-1"/>
      <xsl:variable name="assign" select="$sol//roleAssignment[@person = $p and @topic = $t]"/>
      <td><xsl:choose>
            <xsl:when test="$assign">
              <xsl:value-of select="//role[position() = $assign/@role]/@name"/>
            </xsl:when>
            <xsl:otherwise> - </xsl:otherwise>
          </xsl:choose>
      </td>
    </xsl:for-each>  
  </tr>    
</xsl:template>

</xsl:stylesheet>
