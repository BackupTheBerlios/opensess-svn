package openSess;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     27.02.2005
 * Revision ID: $Id: SolutionPanel.java 48 2005-03-01 11:12:27Z awi $
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
 * The SolutionPanel is a BorderedListPanel with additional
 * JTextFields for the calculation parameters and a button
 * to start the calculation. If this button is pressed,
 * the calculation is started in a separate task and the progress
 * is monitored with ProgressMonitor.
 * 
 * @author andreas
 */
public class SolutionPanel
  extends BorderedListPanel
  implements ActionListener
{
  private JFrame              frame;
  private JFormattedTextField topicClustersField, personAssignmentsField, attemptsField;
  private JButton             solveButton;
  private ProgressMonitor     monitor;
  private Timer               timer;

  /**
   * Construct a new SolutionPanel that is a child of the specified
   * JFrame and associated with a Solver object and a ChangeMonitor.
   * 
   * @param frame   the JFrame.
   * @param solver  the Solver object.
   * @param monitor the ChangeMonitor.
   */
  public SolutionPanel(JFrame frame, Solver solver, ChangeMonitor monitor)
  {
    super("Solutions", solver, monitor, new ShowSolutionWindow(frame),
          BoxLayout.LINE_AXIS);
    this.frame = frame;

    // Create a timer for monitoring calculations
    timer = new Timer(500, new TimerListener());
    
    // Set up the solution list
    JPanel solListPanel = new JPanel();
    solListPanel.setLayout(new BoxLayout(solListPanel, BoxLayout.PAGE_AXIS));
    JScrollPane scroller = new JScrollPane(getList());
    scroller.setMinimumSize(new Dimension(200, 200));
    solListPanel.add(scroller);

    JButton showButton = new JButton("Show");
    showButton.setActionCommand(getEditCommand());
    showButton.addActionListener(this);
    showButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    solListPanel.add(showButton);
    
    add(solListPanel);
    add(Box.createRigidArea(new Dimension(10, 0)));

    // Set up the solution parameter panel
    JPanel solparPanel = new JPanel();
    solparPanel.setLayout(new BoxLayout(solparPanel, BoxLayout.PAGE_AXIS));
    
    NumberFormat intFormat = NumberFormat.getIntegerInstance();
    JLabel topicClustersLabel     = new JLabel("Topic Clustering Attempts:");
    JLabel personAssignmentsLabel = new JLabel("Person Assignment Attempts:");
    JLabel attemptsLabel          = new JLabel("Assignments Attempts:");
    topicClustersField = new JFormattedTextField();
    topicClustersField.setValue(new Integer(5));
    topicClustersField.setColumns(4);
    personAssignmentsField = new JFormattedTextField();
    personAssignmentsField.setValue(new Integer(5));
    personAssignmentsField.setColumns(4);
    attemptsField = new JFormattedTextField();
    attemptsField.setValue(new Integer(100000));
    attemptsField.setColumns(8);
    
    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.LINE_AXIS));
    valuePanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
    solparPanel.add(valuePanel);
    
    JPanel labelPanel = new JPanel(new GridLayout(0,1));
    valuePanel.add(labelPanel);
    labelPanel.add(topicClustersLabel);
    labelPanel.add(personAssignmentsLabel);
    labelPanel.add(attemptsLabel);
    valuePanel.add(Box.createRigidArea(new Dimension(10, 0)));
    
    JPanel fieldPanel = new JPanel(new GridLayout(0,1));
    valuePanel.add(fieldPanel);
    fieldPanel.add(topicClustersField);
    fieldPanel.add(personAssignmentsField);
    fieldPanel.add(attemptsField);
    
    solveButton = new JButton("Solve");
    solveButton.setActionCommand("solve");
    solveButton.addActionListener(this);
    solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    solparPanel.add(solveButton);
    
    Dimension minSize = new Dimension(0, 0);
    Dimension prefSize = new Dimension(0, Short.MAX_VALUE);
    solparPanel.add(new Box.Filler(minSize, prefSize, prefSize));
    // solparPanel.add(Box.createVerticalGlue());
    add(solparPanel);
  }
  
  /**
   * Sets the parameters for solution finding.
   * The number of solutions will usually be topicClusters*personAssignments,
   * but might be slightly less if some solution attempts were not successful.
   * 
   * @param topicClusters     the number of topic clusterings to try.
   * @param personAssignments the number of person assignments to try.
   * @param attempts          the maximum number of assignment attempts.
   */
  public void setSolutionParameters(int topicClusters, int personAssignments,
                                    int attempts)
  {
    topicClustersField.setValue(new Integer(topicClusters));
    personAssignmentsField.setValue(new Integer(personAssignments));
    attemptsField.setValue(new Integer(attempts));
  }

  /**
   * Return the number of topic clusters.
   * @return the number of topic clusters.
   */
  public int getTopicClusters()
  {
    return getIntFromField(topicClustersField);
  }

  /**
   * Return the number of person assignments.
   * @return the number of person assignments.
   */
  public int getPersonAssignments()
  {
    return getIntFromField(personAssignmentsField);
  }

  /**
   * Return the number of assignment attempts.
   * @return the number of assignment attempts.
   */
  public int getAttempts()
  {
    return getIntFromField(attemptsField);
  }
  
  /**
   * Get the value of a JFormattedField as an int.
   * 
   * @param field the field.
   * @return the value, converted to int.
   */
  protected int getIntFromField(JFormattedTextField field)
  {
    try
    {
      field.commitEdit();
    }
    catch (ParseException ex)
    {
    }
    
    return ((Integer)field.getValue()).intValue();
  }
  
  /**
   * Process the command "solve" which start a calculation and
   * forward all other commands to the base class.
   */
  public void processCommand(String command)
  {
    if (command.equals("solve"))
    {
      int topicClusters     = getTopicClusters();
      int personAssignments = getPersonAssignments();
      int attempts          = getAttempts();
      
      monitor = new ProgressMonitor(frame, "Calculating Solutions...", "", 0,
                                    topicClusters * personAssignments);
      monitor.setProgress(0);
      monitor.setMillisToDecideToPopup(0);
      solveButton.setEnabled(false);
      getSolver().startSolverTask(topicClusters, personAssignments, attempts);
      timer.start();
      getChangeMonitor().signalChange();
    }
    else
      super.processCommand(command);
  }
  

  /**
   * This class is an ActionListener which is only triggered for timer
   * events. It is used to provide feedback on the progress of a 
   * solution calculation and also detects when the calculation is
   * finished.
   */
  private class TimerListener
    implements ActionListener
  {
    /**
     * When called, update the progress bar of the ProgressMonitor
     * and display any progress messages. If the calculation has
     * been canceled or terminated, do the neccessary cleanup work.
     */
    public void actionPerformed(ActionEvent e)
    {
      Solver solver = getSolver();
      monitor.setProgress(solver.getCurrent());
      String s = solver.getMessage();
      
      if (s != null) 
        monitor.setNote(s);

      if (monitor.isCanceled() || solver.isDone()) 
      {
        monitor.close();
        solver.stop();
        timer.stop();

        solveButton.setEnabled(true);
        setSelectedIndex(0);
      }
    }
  }
}
