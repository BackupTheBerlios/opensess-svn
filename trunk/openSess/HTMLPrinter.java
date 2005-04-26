/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     2005-04-19 
 * Revision ID: $Id$
 * 
 * This file is part of OpenSess.
 * OpenSess is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * OpenSess is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with OpenSess; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package openSess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;

import javax.swing.JEditorPane;
import javax.swing.RepaintManager;

/**
 * @author andreas
 *
 */
public class HTMLPrinter
  extends JEditorPane 
  implements Printable, Serializable
{
  public HTMLPrinter(String html)
  {
    setContentType("text/html");
    getDocument().putProperty("IgnoreCharsetDirective", new Boolean(true));
    setText(html);
    setEditable(false);
    setSize(500,500);
  }
  
  public int print(Graphics g, PageFormat pf, int pageIndex)
      throws PrinterException
  {
    System.out.println("HTMLPrinter.print");
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(Color.black);

    RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
    Dimension d = this.getSize();
    double panelWidth = d.width;
    double panelHeight = d.height;
    System.out.println("w " + panelWidth + ", h " + panelHeight);
    double pageWidth = pf.getImageableWidth();
    double pageHeight = pf.getImageableHeight();
    System.out.println("iw " + pageWidth + ", ih " + pageHeight);
    double scale = pageWidth / panelWidth;
    System.out.println("scale " + scale);
    int totalNumPages = (int) Math.ceil(scale * panelHeight / pageHeight);

    System.out.println("total " + totalNumPages + ", index " + pageIndex);
    
    // Check for empty pages
    if (pageIndex >= totalNumPages)
      return Printable.NO_SUCH_PAGE;

    g2.translate(pf.getImageableX(), pf.getImageableY());
    g2.translate(0f, -pageIndex * pageHeight);
    g2.scale(scale, scale);
    this.paint(g2);

    return Printable.PAGE_EXISTS;
  }
}

