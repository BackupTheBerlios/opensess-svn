package openSess;
/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     22.02.2005
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
 *  A very simple base class to keep track of changes to the state
 *  of an object or a network of objects.
 * 
 * @author andreas
 */
public class ChangeMonitor
{
  private boolean unsavedChanges = false;
  
  /**
   * Tells the ChangeMonitor that the state is "clear", i.e. that there
   * are no unsaved changes. Directly after a call to clearChanges(),
   * hasChanged() returns false.
   */
  public void clearChanges()
  {
    unsavedChanges = false;
  }

  /**
   * Tells the ChangeMonitor, that a change to the state has occured.
   * Subsequent calls to hasChanged() return true.
   */
  public void signalChange()
  {
    unsavedChanges = true;
  }

  /**
   * Returns true if there are unsaved changes.
   * 
   * @return true if there are unsaved changes.
   */
  public boolean hasChanged()
  {
    return unsavedChanges;    
  }
}
