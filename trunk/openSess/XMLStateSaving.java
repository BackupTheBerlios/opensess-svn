package openSess;
import java.io.PrintWriter;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     21.02.2005
 * Revision ID: $Id: XMLStateSaving.java 48 2005-03-01 11:12:27Z awi $
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

/**
 * A simple base interface for classes that can represent their
 * state as an XML fragment. A public class Indenter provides an
 * easy way to get everything nicely indented.
 *  
 * @author andreas
 */
public interface XMLStateSaving
{
  /**
   * Save state to a PrintWriter. The XML code written should
   * be indented at the specified level.
   * If implementations of this method call the save-methods of other
   * objects (or the same object) recursively, they should do so
   * with the proper indentation level (e.g. "level+1").
   * 
   * @param writer  the PrintWriter to write to.
   * @param level   the level that XML code should be indented to.
   */
  public void save(PrintWriter writer, int level);
  
  /**
   * Indenter helps with indenting the XML code.
   * To indent a line to the correct level within save(),
   * call Indenter.println(writer, level, line).
   * You can use "level+n", or "level-n" to indent at another
   * level relative to the current level.
   * Each indentation level is indented by two spaces.
   * 
   * @author andreas
   */
  public class Indenter	
  {
    static final String space = "                                                        ";
    static final int    indentAmount = 2;
    
    /**
     * Print a line to the PrintWriter at the specified indentation level.
     * 
     * @param writer the PrintWriter.
     * @param level  the indentation level.
     * @param line   the line to write.
     */
    protected static void println(PrintWriter writer, int level, String line)
    {
      writer.print(space.substring(0, level*indentAmount));
      writer.println(line);
    }
  }
}
