package openSess;
/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     2005-02-12
 * Revision ID: $Id: OpenSess.java 43 2005-02-25 09:01:08Z andreas $
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
 * The main class of the GUI.
 * 
 * @author awi
 */
public class OpenSess
{
  /**
   * Create the GUI by instantiating the MainWindow.
   */
  private static void createAndShowGUI()
  {
    MainWindow main = new MainWindow();
  }

  /**
   * Move the GUI creation to a dedicated thread.
   * 
   * @param args  the program arguments.
   */
  public static void main(String[] args)
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        createAndShowGUI();
      }
    });
  }
}