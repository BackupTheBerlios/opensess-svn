

package openSess;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     16.02.2005
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
 * EditPersonWindow extends ListEditWindow for the editing of person objects.
 * 
 * @author andreas
 */
public class EditPersonWindow
  extends ListEditWindow
{
  private JPanel prefList;
  private Solver solver;
  private int    person;
  
  /**
   * Constructs a new EditPersonWindow.
   * 
   * @param frame the parent JFrame.
   */
  public EditPersonWindow(JFrame frame)
  {
    super(frame, "Edit Person");
  }
  
  /**
   * Adds a panel for the topic preferences to the window.
   */
  protected void addAdditionalComponents(JPanel panel)
  {
    JPanel prefPanel = new JPanel();
    prefPanel.setLayout(new BoxLayout(prefPanel, BoxLayout.PAGE_AXIS));
    Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    TitledBorder titled = BorderFactory.createTitledBorder(border, "Topic Preferences");
    titled.setTitleJustification(TitledBorder.LEFT);
    prefPanel.setBorder(titled);
    panel.add(prefPanel);
    
    prefList = new JPanel();
    prefList.setLayout(new BoxLayout(prefList, BoxLayout.PAGE_AXIS));
    JScrollPane prefScroller = new JScrollPane(prefList);
    prefPanel.add(prefScroller);
  }
  
  /**
   * Adds the topic preference ComboBoxes before the window is displayed.
   */
  protected void additionalEditSetup(ComboBoxModel model, Solver solver, int selected,
                                     ChangeMonitor changeMonitor)
  {
    updatePreferenceList(solver, selected);
  }
  
  /**
   * Recreates the preference ComboBoxes when the person selection changes.
   */
  protected void additionalChangesOnSelection(Solver solver, int selected)
  {
    updatePreferenceList(solver, selected);
  }

  /**
   * Removes the existing preference ComboBoxes and creates new ones according
   * to the current selection.
   * 
   * @param solver   the Solver object containing configuration data.
   * @param selected the index of the currently selected object.
   */
  protected void updatePreferenceList(Solver solver, int selected)
  {
    this.solver = solver;
    person      = selected;
    // remove old listeners
    for (int topic = 0; topic < prefList.getComponentCount();  ++topic)
      ((JComboBox)prefList.getComponent(topic)).removeActionListener(this);
    
    prefList.removeAll();
    
    // Build a model of the topics
    DefaultListModel names = solver.getTopics().getNames();
    
    // Create comboboxes with this model and set them in the preference order
    // of the current person
    Persons persons = solver.getPersons();
    
    for (int topicIndex = 0;  topicIndex < solver.getTopics().getNumber();  ++topicIndex)
    {
      JComboBox box = new JComboBox(new SharedDataComboBoxModel(names));
      prefList.add(box);
      box.setSelectedIndex(persons.getPreference(selected, topicIndex));
      box.addActionListener(this);
    }
    
    pack();
  }
  
  /**
   * If the value of a ComboBox was changed, swap the values of this
   * ComboBox with the other ComboBox which currently has the selected value.
   */
  public void additionalActionPerformed(ActionEvent e, ChangeMonitor changeMonitor)
  {
    JComboBox changedBox = (JComboBox) e.getSource();
    
    // determine the index of this box in the preference list
    for (int changed = 0; changed < prefList.getComponentCount();  ++changed)
      if (prefList.getComponent(changed) == changedBox)
      {
        Object value = changedBox.getSelectedItem();
        
        // Find the other ComboBox that has the same value as the newly changed one.
        for (int other = 0; other < prefList.getComponentCount();  ++other)
        {
          JComboBox otherBox = (JComboBox) prefList.getComponent(other);
        
          if (other != changed && value == otherBox.getSelectedItem())
          {
            // Swap the preferences in the real preference list
            solver.getPersons().swapPreferences(person, changed, other);
            // Set the other box to its new value
            otherBox.setSelectedIndex(solver.getPersons().getPreference(person, other));
            // I am not sure why this is necessary, but otherwise the other box
            // is not always repainted:
            otherBox.update(otherBox.getGraphics());
            changeMonitor.signalChange();
            break;
          }
        }
        
        return;
      }
    
    // We only get here if the function was called for something outside our list
    // which should not happen
    System.out.println("WARNING: Spurious event " + e);
  }
}
