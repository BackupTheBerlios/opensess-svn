package openSess;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     16.02.2005
 * Revision ID: $Id: ListEditWindow.java 48 2005-03-01 11:12:27Z awi $
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
 * @author andreas
 * This is a base class for several kinds of dialog that allow editing
 * several objects that are kept in a list. At the top of the dialog,
 * a ComboBox is displayed which allows selecting the object to be edited.
 * Below this, a TextField allows to change the name of the object.
 * At the bottom of the dialog, buttons allow to close the window and to
 * navigate to the preceeding and following objects in the list.
 * The area in between can be configured by implementing 
 * additionalComponents() in the deriving class.
 * The constructor creates the dialog in the invisible state independently
 * of the configuration data maintained by a Solver object.
 * A call to edit() must be used to display the dialog and this also
 * constructs everything that depends on Solver(). Deriving classes
 * should implement additionalEditSetup() if they need Solver data for
 * configuration.
 * Derived classes can also implement additionalChangesOnSelection()
 * and additionalActionOnNameChange() if they need to contribute to the
 * event processing.
 */
public class ListEditWindow
  extends JDialog
  implements ActionListener, FocusListener
{
  private JComboBox     list;
  private JTextField    nameField;
  private Solver        solver;
  private ChangeMonitor changeMonitor;

  /**
   * Construct a ListEditWindow that is a child of the given JFrame and
   * has the given title.
   * Calls additionalComponents() which derived classes can use to add
   * additional components to the window.
   * 
   * @param frame  the parent frame.
   * @param title  the title of the new window.
   */
  public ListEditWindow(JFrame frame, String title)
  {
    super(frame, title, false);

    JPanel rootPanel = new JPanel();
    getContentPane().add(rootPanel);
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
    rootPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
  
    list = new JComboBox();
    list.addActionListener(this);
    rootPanel.add(list);
    
    JPanel editPanel = new JPanel();
    editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.LINE_AXIS));
    editPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
    rootPanel.add(editPanel);
    
    editPanel.add(new JLabel("Name:"));
    editPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    nameField = new JTextField();
    nameField.setActionCommand("set");
    nameField.addActionListener(this); // react to VK_ENTER
    nameField.addFocusListener(this);  // react to FocusLost
    editPanel.add(nameField);
    
    addAdditionalComponents(rootPanel);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    rootPanel.add(buttonPanel);
    
    JButton prevButton = new JButton("Previous");
    prevButton.setActionCommand("previous");
    prevButton.addActionListener(this);
    buttonPanel.add(prevButton);

    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        
    JButton closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);
    buttonPanel.add(closeButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPanel.add(Box.createHorizontalGlue());
    
    JButton nextButton = new JButton("Next");
    nextButton.setActionCommand("next");
    nextButton.addActionListener(this);
    buttonPanel.add(nextButton);
    
    pack();
  }
  
  /**
   * Derived classes can implement this to add additional components
   * to the window. At the time of call, The top elements have already
   * been added to the JPanel and after the call, the buttons at the bottom
   * will be added. The JPanel has a BoxLayout with PAGE orientation.
   * 
   * @param panel  the contents panel of the ListEditWindow.
   */
  protected void addAdditionalComponents(JPanel panel)
  {
  }
  
  /**
   * This methods makes the final preparations to the ListEditWindow that
   * depend on a Solver object and displays the window for the user.
   * The ListEditWindow also signals changes to a ChangeMonitor so that
   * the parent window knows that there are unsaved changes.
   * 
   * @param model     the ComboBoxModel containing the list of objects.
   * @param solver    the Solver object containing configuration data.
   * @param selected  the index of the object to be displayed initially.
   * @param changeMonitor  the ChangeMonitor to inform about any changes.
   */
  public void edit(ComboBoxModel model, Solver solver, int selected, ChangeMonitor changeMonitor)
  {
    if (model.getSize() < 1)
      return;
    
    additionalEditSetup(model, solver, selected, changeMonitor);

    this.solver = solver;
    this.changeMonitor = changeMonitor;
    list.setModel(model);
    list.setSelectedIndex(selected);
    setVisible(true);
    nameField.requestFocusInWindow();
    redisplay();
  }
  
  /**
   * Derived classes can implement this method if they need to perform any
   * initialisations depending on a Solver object before the window is
   * displayed.
   * 
   * @param model     the ComboBoxModel containing the list of objects.
   * @param solver    the Solver object containing configuration data.
   * @param selected  the index of the currently selected object.
   * @param changeMonitor  the ChangeMonitor to inform about any changes.
   */
  protected void additionalEditSetup(ComboBoxModel model, Solver solver, int selected,
                                     ChangeMonitor changeMonitor)
  {
  }
  
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();
    
    if (source == list)
    {
      // A new selection has been made, change the nameField
      redisplay();
      additionalChangesOnSelection(solver, list.getSelectedIndex());
    }
    else if (source == nameField)
    {
      setObjectName(true);
    }
    else if (AbstractButton.class.isAssignableFrom(source.getClass()))
    {
      AbstractButton item = (AbstractButton) source;
      String command = item.getActionCommand();

      if (command.equals("previous"))
      {
        int index = list.getSelectedIndex();
        
        if (index > 0)
        {
          list.setSelectedIndex(index-1);
          list.update(list.getGraphics());
          redisplay();
        }
      }
      else if (command.equals("next"))
      {
        int index = list.getSelectedIndex();
        
        if (index+1 < list.getItemCount())
        {
          list.setSelectedIndex(index+1);
          list.update(list.getGraphics());
          redisplay();
        }
      }
      else if (command.equals("set"))
      {
      }
      else if (command.equals("close"))
      {
        setVisible(false);
      }
      else
        additionalActionPerformed(e, changeMonitor);
    }
    else
      additionalActionPerformed(e, changeMonitor);
  }

  /**
   * Ignore FocusGained events.
   */
  public void focusGained(FocusEvent e)
  {
  }
  
  /**
   * When the text field looses focus, set the selected object's name. 
   */
  public void focusLost(FocusEvent e)
  {
    setObjectName(false);
  }

  /**
   * Set the name of the currently selected object to the value of
   * the text field. If the parameter is true, try to advance the
   * current selection to the next object.
   * 
   * @param advance if true, advance to next object.
   */
  protected void setObjectName(boolean advance)
  {
    String name = nameField.getText();
    int index = list.getSelectedIndex();
    int nameInList = getListIndex(name);

    if (nameInList != -1 && nameInList != index)
      JOptionPane
          .showMessageDialog(
                             this,
                             "This name was already used elsewhere.\nPlease choose another.",
                             "Duplicate Name", JOptionPane.ERROR_MESSAGE);
    else
    {
      SharedDataComboBoxModel model = (SharedDataComboBoxModel) list.getModel();
      model.setElementAt(nameField.getText(), index);
      list.setSelectedIndex(index);
      additionalActionOnNameChange(solver, list.getSelectedIndex(), 
                                   nameField.getText());
      changeMonitor.signalChange();
      
      if (advance)
      {
        // for convenience, select the next element
        list.setSelectedIndex(index + 1 < list.getItemCount() ? index + 1 : index);
        redisplay();
      }
    }
  }
  
  /**
   * Derived classes can implement this method if they need to
   * participate in the general event processing. This is called by
   * ListEditWindow.actionPerformed() if there is an event that
   * actionPerformed can not process itself (for example additionalActionPerformed()
   * will not be called when the close button is pressed).
   * 
   * @param e  the Event.
   * @param changeMonitor  the ChangeMonitor to inform about any changes.
   */
  protected void additionalActionPerformed(ActionEvent e, ChangeMonitor changeMonitor)
  {
  }

  /**
   * Derived classes can implement this method if they need to do anything
   * when a new object is selected.
   * 
   * @param solver    the Solver object containing configuration data.
   * @param selected  the index of the newly selected object.
   */
  protected void additionalChangesOnSelection(Solver solver, int selected)
  {
  }


  /**
   * Derived classes can implement this method if they need to do anything
   * when the name of the current object changes.
   * 
   * @param solver    the Solver object containing configuration data.
   * @param selected  the index of the currently selected object.
   * @param name      the new name.
   */
  protected void additionalActionOnNameChange(Solver solver, int selected, String name)
  {
  }
  
  /**
   * Redisplay the name field and select all of it.
   */
  void redisplay()
  {
    nameField.setText((String) list.getSelectedItem());
    nameField.select(0, nameField.getText().length());
  }

  /**
   * Return the index in the list of the object with the given name
   * or -1 if no such object exists.
   *  
   * @param name  the name of the object of interest.
   * @return      the index of the object in the list or -1 if it does not exist.
   */
  protected int getListIndex(String name)
  {
    ComboBoxModel model = list.getModel();
    int size = model.getSize();
    
    for (int index = 0;  index < size;  ++index)
      if (name.equals((String) model.getElementAt(index)))
        return index;
      
    return -1;
  }
}
