package openSess;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
 * EnterListener is a specialized KeyAdapter
 * that determines whether the ENTER key has been pressed
 * on a component.
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class EnterListener
extends KeyAdapter
{
  private String           command;
  private CommandProcessor processor;
  /**
   * Constructs a new EnterListener.
   * 
   * @param command the command to process when ENTER is pressed.
   * @param processor the CommandProcessor to process the command.
   */
  public EnterListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }

  /**
   * Check the event and process the ActionCommand on ENTER.
   */
  public void keyPressed(KeyEvent e) 
  {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
      processor.processCommand(command);
  }
}
