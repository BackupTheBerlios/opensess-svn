package openSess;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     17.02.2005
 * Revision ID: $Id: EditRoleWindow.java 10 2005-03-04 18:45:41Z awickner $
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
 * The window for editing role names is simply a ListEditWindow.
 * 
 * @author andreas
 */
public class EditRoleWindow
  extends ListEditWindow
{
  private JFormattedTextField minField, maxField;
  private Roles               roles;
  private int                 selected;
  
  /**
   * Constructs an EditRoleWindow.
   * 
   * @param frame
   */
  public EditRoleWindow(JFrame frame)
  {
    super(frame, "Edit Role");
  }
  
  /**
   * Adds fields for editing the minimum and maximum occurences of roles.
   */
  protected void addAdditionalComponents(JPanel panel)
  {
    JPanel occursPanel = new JPanel();
    occursPanel.setLayout(new GridBagLayout());
    Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    TitledBorder titled = BorderFactory.createTitledBorder(border, "Occurences per session");
    titled.setTitleJustification(TitledBorder.LEFT);
    occursPanel.setBorder(titled);
    panel.add(occursPanel);

    GridBagConstraints c = new GridBagConstraints();
    c.gridx   = 0;
    c.gridy   = 0;
    c.insets  = new Insets(5, 5, 5, 5);
    c.anchor  = GridBagConstraints.LINE_START;
    occursPanel.add(new JLabel("Minimum per session:"), c);
    c.gridy   = 1;
    occursPanel.add(new JLabel("Maximum per session:"), c);
    c.gridx   = 1;
    c.gridy   = 0;
    c.fill    = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    minField = new JFormattedTextField();
    minField.setValue(new Integer(3));
    minField.setColumns(5);
    minField.addActionListener(this); // react to VK_ENTER
    minField.addFocusListener(this);  // react to FocusLost
    occursPanel.add(minField, c);
    c.gridy = 1;
    maxField = new JFormattedTextField();
    maxField.setValue(new Integer(3));
    maxField.setColumns(5);
    maxField.addActionListener(this); // react to VK_ENTER
    maxField.addFocusListener(this);  // react to FocusLost
    occursPanel.add(maxField, c);
  }
  
  /**
   * Displays data before the window is displayed.
   */
  protected void additionalEditSetup(ComboBoxModel model, Solver solver, int selected,
                                     ChangeMonitor changeMonitor)
  {
    updateOccurences(solver, selected);
  }
  
  /**
   * Redisplays data when the role selection changes.
   */
  protected void additionalChangesOnSelection(Solver solver, int selected)
  {
    updateOccurences(solver, selected);
  }

  /**
   * Display the current occurence data
   * 
   * @param solver   the Solver object containing configuration data.
   * @param role     the index of the currently selected object.
   */
  protected void updateOccurences(Solver solver, int role)
  {
    roles    = solver.getRoles();
    selected = role;
    int min  = roles.getMinimumPerSession(role);
    int max  = roles.getMaximumPerSession(role);
    
    minField.setValue(new Integer(min));
    maxField.setValue(new Integer(max));
  }

  /**
   * Read the occurences fields when VK_ENTER was pressed.
   */
  protected void additionalActionPerformed(ActionEvent e)
  {
    Object source = e.getSource();
    
    if (source == minField || source == maxField)
      setRoleOccurences();
  }

  /**
   * Read the occurences fields when the focus changed.
   */
  protected void additionalFocusLost(FocusEvent e)
  {
    Object source = e.getSource();
    
    if (source == minField || source == maxField)
      setRoleOccurences();
  }
  
  /**
   * Read the text fields and set the role values accordingly.
   */
  protected void setRoleOccurences()
  {
    roles.setMinimumPerSession(selected, getIntFromField(minField));
    roles.setMaximumPerSession(selected, getIntFromField(maxField));
  }
}
