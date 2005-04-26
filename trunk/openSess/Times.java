/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     2005-04-17 
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

import java.io.PrintWriter;

import javax.swing.DefaultListModel;

/**
 * @author andreas
 *
 */
public class Times
  implements XMLStateSaving
{
  private DefaultListModel names;

  /**
   * Constructs a new Times object.
   * 
   * @param dimTimes the number of times.
   */
  public Times(int dimTimes)
  {
    names = new DefaultListModel();
    
    for (int p = 0; p < dimTimes; p++)
      names.addElement("Time " + (p+1));
  }


  /**
   * Returns the number of times.
   * 
   * @return the number of times.
   */
  public int getNumber()
  {
    return names.getSize();
  }
  
  /**
   * Returns all time names as a DefaultListModel.
   * 
   * @return a list of all time names.
   */
  public DefaultListModel getNames()
  {
    return names;
  }
  
  /**
   * Returns the name of the time with a given index.
   * 
   * @param index the index of the time
   * @return the name of the time.
   */
  public String getName(int index)
  {
    return (String)names.getElementAt(index);
  }
  
  /**
   * Set the name of a time.
   * 
   * @param time the time number.
   * @param name     the new name.
   */
  protected void setName(int time, String name)
  {
    names.setElementAt(name, time);
  }
  
  /**
   * Save the Times data in XML form.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<times>");

    for (int p = 0;  p < getNumber();  ++p)
      Indenter.println(stream, level+1, "<time name=\"" + getName(p) + "\"/>");
    
    Indenter.println(stream, level, "</times>");
  }
}
