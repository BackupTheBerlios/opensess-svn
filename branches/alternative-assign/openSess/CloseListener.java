package openSess;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     27.02.2005
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
 * CloseListener is a specialized WindowAdapter
 * that recognizes attempts from the outside
 * to close the window (e.g. via the system menu).
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class CloseListener
extends WindowAdapter
{
  private String           command;
  private CommandProcessor processor;
  
  /**
   * Constructs a new CloseListener.
   * 
   * @param command the command to process when the window is closed.
   * @param processor the CommandProcessor to process the command.
   */
  public CloseListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }
  
  /**
   * Process the ActionCommand when the window is closed.
   */
  public void windowClosing(WindowEvent e)
  {
    processor.processCommand(command);
  }
}
