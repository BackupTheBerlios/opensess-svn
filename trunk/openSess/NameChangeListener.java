package openSess;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     11.03.2005
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

/**
 * ListEditWindows can call implementers of this interface
 * when the name of the selected element changes.
 * @author andreas
 */
public interface NameChangeListener
{
  /**
   * The name at the indicated index has changed from oldName to newName.
   * 
   * @param index   the index of the changed object.
   * @param oldName the old name.
   * @param newName the new name.
   */
  public void nameChanged(int index, String oldName, String newName);
}
