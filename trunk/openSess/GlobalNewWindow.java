package openSess;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     13.02.2005
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
 * Displays a modal dialog used to input the global configuration
 * for a new simulation.
 * 
 * @author awi
 */
public class GlobalNewWindow
  extends JDialog
  implements ActionListener
{
  private ActionListener createListener;
  private JFormattedTextField topicsField, personsField, rolesField, sessionsField;
  
  /**
   * Create a new GlobalNewWindow.
   * It is not displayed immediately, use getConfig() to show
   * the dialog and retrieve the results.
   * 
   * @param frame the JFrame that this dialog belongs to.
   */
  public GlobalNewWindow(JFrame frame)
  {
    super(frame, "New Configuration", true);

    JPanel rootPanel = new JPanel();
    getContentPane().add(rootPanel);
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
    rootPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    
    JLabel configInfo = new JLabel("Create a new configuration with:");
    configInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
    rootPanel.add(configInfo);

    NumberFormat intFormat = NumberFormat.getIntegerInstance();
    JLabel topicsLabel   = new JLabel("Number of Topics:");
    JLabel personsLabel  = new JLabel("Number of Persons:");
    JLabel rolesLabel    = new JLabel("Number of Roles:");
    JLabel sessionsLabel = new JLabel("Number of Sessions:");
    topicsField = new JFormattedTextField();
    topicsField.setValue(new Integer(12));
    topicsField.setColumns(4);
    personsField = new JFormattedTextField();
    personsField.setValue(new Integer(12));
    personsField.setColumns(4);
    rolesField = new JFormattedTextField();
    rolesField.setValue(new Integer(3));
    rolesField.setColumns(4);
    sessionsField = new JFormattedTextField();
    sessionsField.setValue(new Integer(2));
    sessionsField.setColumns(3);
    
    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.LINE_AXIS));
    valuePanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
    rootPanel.add(valuePanel);
    
    JPanel labelPanel = new JPanel(new GridLayout(0,1));
    valuePanel.add(labelPanel);
    labelPanel.add(topicsLabel);
    labelPanel.add(personsLabel);
    labelPanel.add(rolesLabel);
    labelPanel.add(sessionsLabel);
    
    JPanel fieldPanel = new JPanel(new GridLayout(0,1));
    valuePanel.add(fieldPanel);
    fieldPanel.add(topicsField);
    fieldPanel.add(personsField);
    fieldPanel.add(rolesField);
    fieldPanel.add(sessionsField);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    rootPanel.add(buttonPanel);
    
    JButton createButton = new JButton("Create");
    createButton.setActionCommand("create");
    createButton.addActionListener(this);
    buttonPanel.add(createButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setActionCommand("cancel");
    cancelButton.addActionListener(this);
    buttonPanel.add(cancelButton);
    
    pack();
  }
  
  /**
   * Display the dialog (which is modal). When the user presses
   * "Create" or "Cancel", the dialog is hidden again.
   * When "Create" was pressed, the actionPerformed-method of the
   * ActionListener passed as a parameter is called (the caller does 
   * not need to investigate the event, but it is forwarded for whatever 
   * reason). The caller can use the getNumberOf...-functions to retrieve
   * the new values in the ActionListener.
   * 
   * @param listener the ActionListener to be used when "Create" is pressed.
   */
  public void getConfig(ActionListener listener)
  {
    createListener = listener;
    setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e)
  {
    JButton item = (JButton)e.getSource();
    String command = item.getActionCommand();
    
    if (command.equals("cancel"))
    {
      setVisible(false);
    }
    else if (command.equals("create"))
    {
      int topics   = getNumberOfTopics();
      int persons  = getNumberOfPersons();
      int roles    = getNumberOfRoles();
      int sessions = getNumberOfSessions();
      
      boolean topicsOk = (topics % roles) == 0 && (topics % sessions) == 0;
      boolean personsOk = (persons % roles) == 0 && (persons % sessions) == 0;
      
      if (topicsOk && personsOk)
      {
        createListener.actionPerformed(e);
        setVisible(false);
      }
      else
      {
        StringBuffer msg = new StringBuffer("The following constraints were violated:\n");
        
        if (!topicsOk)
          msg.append("- The number of topics must be divisible by the\n"
                     +	"  number of roles and by the number of sessions\n");
        
        if (!personsOk)
          msg.append("- The number of persons must be divisible by the\n"
                     +	"  number of roles and by the number of sessions\n");
        
        JOptionPane.showMessageDialog(this, msg, "Constraint Violation", JOptionPane.ERROR_MESSAGE); 
      }
    }
  }

  /**
   * Return the new number of topics.
   * 
   * @return the new number of topics.
   */
  public int getNumberOfTopics()
  {
    try
    {
      topicsField.commitEdit();
    }
    catch (ParseException ex)
    {
    }
    
    return ((Integer)topicsField.getValue()).intValue();
  }
  
  /**
   * Return the new number of persons.
   * 
   * @return the new number of persons.
   */
  public int getNumberOfPersons()
  {
    try
    {
      personsField.commitEdit();
    }
    catch (ParseException ex)
    {
    }
    
    return ((Integer)personsField.getValue()).intValue();
  }
  
  /**
   * Return the new number of roles.
   * 
   * @return the new number of roles.
   */
  public int getNumberOfRoles()
  {
    try
    {
      rolesField.commitEdit();
    }
    catch (ParseException ex)
    {
    }
    
    return ((Integer)rolesField.getValue()).intValue();
  }
  
  /**
   * Return the new number of sessions.
   * 
   * @return the new number of sessions.
   */
  public int getNumberOfSessions()
  {
    try
    {
      sessionsField.commitEdit();
    }
    catch (ParseException ex)
    {
    }
    
    return ((Integer)sessionsField.getValue()).intValue();
  }
}
