package openSess;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;

/*
 * Copyright 2005 Gero Scholz, Andreas Wickner
 * 
 * Created:     2005-02-11
 * Revision ID: $Id$
 * 
 * 2005-02-17/AW: Complete rewrite for GUI.
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
 * This is a bit tricky since when there are three roles, the
 * GUI will display three roles, but the algorithm uses an additional
 * role "no role". To make things worse, the "no role" role must have 
 * index zero for the algorithm.
 * This means that some methods of Roles are zero-based, while others
 * are one-based. If the number of roles is three and the first role
 * is "Speaker":
 * 
 * - getNumber() returns 3.
 * - getNames() returns an array with 3 entries.
 * - getNameExtended(0) returns ""
 * - getNameExtended(1) returns "Speaker"
 * - getName(0) returns "Speaker"
 * - setName(0, "Sprecher") replaces "Speaker"
 * 
 * @author Andreas Wickner
 */

public class Roles
  implements XMLStateSaving
{
  private DefaultListModel names;

  /**
   * Constructs a new Roles object from information in the Solver object.
   * 
   * @param solver the Solver object containing configuration information.
   */
  public Roles(Solver solver)
  {
    names = new DefaultListModel();

    for (int role = 0;  role < solver.dimRoles;  ++role)
      names.addElement("Role " + (role+1));
  
    if (getNumber() > 0)
      setName(0, "Speaker");
    
    if (getNumber() > 1)
      setName(1, "Critic");
    
    if (getNumber() > 2)
      setName(2, "Observer");
  }

  /**
   * Return the number of roles.
   * 
   * @return the number of roles.
   */
  public int getNumber()
  {
    return names.getSize();
  }

  /**
   * Return the list of all role names as a DefaultListModel.
   * 
   * @return the list of all role names.
   */
  public DefaultListModel getNames()
  {
    return names;
  }
  
  /**
   * Return the name of a role.
   * This method is "1-based", i.e. getNameExtended(1) returns the name
   * of the role otherwise numbered zero. getNameExtended(0) returns an
   * empty String and all parameters greater than getNumber() return "*".
   * 
   * @param index the index of a role.
   * @return the name of the role.
   */
  public String getNameExtended(int index)
  {
    if (index <= 0)
      return "";
    else if (index > getNumber())
      return "*";
    
    return (String)names.getElementAt(index-1);
  }

  /**
   * Return the name of a role.
   * This method is "0-based", i.e. getName(0) returns the name
   * of the role numbered zero.
   *  
   * @param index the index of a role.
   * @return the name of the role.
   */
  public String getName(int index)
  {
    return (String)names.getElementAt(index);
  }

  /**
   * Set the name of a role.
   * 
   * @param index  the index of a role.
   * @param name   the new name.
   */
  public void setName(int index, String name)
  {
    names.setElementAt(name, index);
  }
  
  /**
   * Produce an XML representation of the roles.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<roles>");

    for (int r = 0;  r < getNumber();  ++r)
      Indenter.println(stream, level+1, "<role name=\"" + getName(r) + "\"/>");
    
    Indenter.println(stream, level, "</roles>");
  }
}