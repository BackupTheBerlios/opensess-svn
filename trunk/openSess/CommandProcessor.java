package openSess;
/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     27.02.2005
 * Revision ID: $Id: CommandProcessor.java 48 2005-03-01 11:12:27Z awi $
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
 * A simple interface for objects that accept string commands.
 * @author andreas
 */
public interface CommandProcessor
{
  /**
   * Execute the command specified as the parameter.
   * 
   * @param command the command.
   */
  public void processCommand(String command);
}
