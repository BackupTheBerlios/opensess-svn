package openSess;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     27.02.2005
 * Revision ID: $Id: ObjectPanel.java 48 2005-03-01 11:12:27Z awi $
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
 * Since the JPanels for editing topics, persons and roles
 * are so similar, we implement them with a common class. 
 * All the main work is done by a BorderedListPanel, we just
 * have to add an Edit button.
 * 
 * @author andreas
 */
public class ObjectPanel
  extends BorderedListPanel
{
  /**
   * Construct a new ObjectPanel.
   *  
   * @param title       the title to display in the border.
   * @param editWindow  the associated ListEditWindow.
   * @param solver      the Solver object.
   * @param monitor     the ChangeMonitor.
   */
  public ObjectPanel(String title, ListEditWindow editWindow, Solver solver,
                     ChangeMonitor monitor)
  {
    super(title, solver, monitor, editWindow, BoxLayout.PAGE_AXIS);
    JScrollPane topicScroller = new JScrollPane(getList());
    add(topicScroller);
    JButton topicEditButton = new JButton("Edit");
    topicEditButton.setActionCommand(getEditCommand());
    topicEditButton.addActionListener(this);
    topicEditButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(topicEditButton);
  }
 }
