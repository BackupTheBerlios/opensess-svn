package openSess;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
 * DoubleClickListener is a specialized MouseAdapter
 * that determines whether a component has been double-clicked.
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class DoubleClickListener
extends MouseAdapter
{
  private String           command;
  private CommandProcessor processor;
  /**
   * Constructs a new DoubleClickListener.
   * 
   * @param command the command to process when an item is double-clicked.
   * @param processor the CommandProcessor to process the command.
   */
  public DoubleClickListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }
  
  /**
   * Check the event and process the ActionCommand on double-click.
   */
  public void mouseClicked(MouseEvent e) 
  {
    if (e.getClickCount() == 2) 
      processor.processCommand(command);
  }
}
