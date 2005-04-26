package openSess;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created: 27.02.2005 Revision ID: $Id: SolutionPanel.java 10 2005-03-04
 * 18:45:41Z awickner $
 * 
 * This file is part of OpenSess. OpenSess is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * OpenSess is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenSess; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * The SolutionPanel is a BorderedListPanel with additional JTextFields for the
 * calculation parameters and a button to start the calculation. If this button
 * is pressed, the calculation is started in a separate task and the progress is
 * monitored with ProgressMonitor.
 * 
 * @author andreas
 */
public class SolutionPanel
    extends BorderedListPanel
    implements ActionListener
{
  private JFrame              frame;
  private JFormattedTextField topicClustersField, personAssignmentsField, keepBestField,
      attemptsField;
  private JButton             solveButton;
  private JComboBox           printFormatList;
  private ProgressMonitor     monitor;
  private Timer               timer;
  private String              printDirPath = "../print";
  private XMLStateSaving      stateSaver;
  
  
  /**
   * Construct a new SolutionPanel that is a child of the specified JFrame and
   * associated with a Solver object and a ChangeMonitor.
   * 
   * @param frame
   *          the JFrame.
   * @param solver
   *          the Solver object.
   * @param monitor
   *          the ChangeMonitor.
   */
  public SolutionPanel(JFrame frame, Solver solver, ChangeMonitor monitor,
                       XMLStateSaving stateSaver)
  {
    super("Solutions", solver, monitor, new ShowSolutionWindow(frame),
          BoxLayout.LINE_AXIS);
    this.frame      = frame;
    this.stateSaver = stateSaver;
    
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
    JLabel topicClustersLabel = new JLabel("Topic Clustering Attempts:");
    JLabel personAssignmentsLabel = new JLabel("Person Assignment Attempts:");
    JLabel attemptsLabel = new JLabel("Maximum Assignment Attempts:");
    JLabel keepBestLabel = new JLabel("Keep Best Solutions:");
    topicClustersField = new JFormattedTextField();
    topicClustersField.setValue(new Integer(5));
    topicClustersField.setColumns(4);
    personAssignmentsField = new JFormattedTextField();
    personAssignmentsField.setValue(new Integer(5));
    personAssignmentsField.setColumns(4);
    attemptsField = new JFormattedTextField();
    attemptsField.setValue(new Integer(100000));
    attemptsField.setColumns(8);
    keepBestField = new JFormattedTextField();
    keepBestField.setValue(new Integer(10));
    keepBestField.setColumns(4);

    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.LINE_AXIS));
    valuePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    solparPanel.add(valuePanel);

    JPanel labelPanel = new JPanel(new GridLayout(0, 1));
    valuePanel.add(labelPanel);
    labelPanel.add(topicClustersLabel);
    labelPanel.add(personAssignmentsLabel);
    labelPanel.add(attemptsLabel);
    labelPanel.add(keepBestLabel);
    valuePanel.add(Box.createRigidArea(new Dimension(10, 0)));

    JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
    valuePanel.add(fieldPanel);
    fieldPanel.add(topicClustersField);
    fieldPanel.add(personAssignmentsField);
    fieldPanel.add(attemptsField);
    fieldPanel.add(keepBestField);

    solveButton = new JButton("Solve");
    solveButton.setActionCommand("solve");
    solveButton.addActionListener(this);
    solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    solparPanel.add(solveButton);
    solparPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JLabel printFormatLabel = new JLabel("Print format:");
    printFormatLabel.setMaximumSize(new Dimension(Short.MAX_VALUE,
                                      Short.MAX_VALUE));    
    printFormatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    solparPanel.add(printFormatLabel);
    
    printFormatList = new JComboBox();
    getPrintFormats();  // Read available print formats
    printFormatList.addActionListener(this);
    printFormatList.setAlignmentX(Component.CENTER_ALIGNMENT);
    solparPanel.add(printFormatList);
    solparPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JButton printButton = new JButton("Print");
    printButton.setActionCommand("print");
    printButton.addActionListener(this);
    printButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    solparPanel.add(printButton);
    
    Dimension minSize = new Dimension(0, 0);
    Dimension prefSize = new Dimension(0, Short.MAX_VALUE);
    solparPanel.add(new Box.Filler(minSize, prefSize, prefSize));
    add(solparPanel);
  }

  /**
   * Read the list of available print formats.
   *
   */
  protected void getPrintFormats()
  {
    try
    {
      // Locate the print directory and get the contained files
      File printDir = new File(getClass().getResource(printDirPath).toURI());
      String files[] = printDir.list();

      for (int n = 0; n < files.length; ++n)
        if (files[n].endsWith(".xsl"))
        {
          int p = files[n].lastIndexOf(".xsl");
          String format = files[n].substring(0, p);
          printFormatList.addItem(format);
        }
    }
    catch (URISyntaxException e)
    {
      System.out.println("Problem reading print formats: URL -> URI conversion.");
    }
  }
  
  /**
   * Sets the parameters for solution finding. The number of solutions will
   * usually be topicClusters*personAssignments, but might be slightly less if
   * some solution attempts were not successful.
   * 
   * @param topicClusters
   *          the number of topic clusterings to try.
   * @param personAssignments
   *          the number of person assignments to try.
   * @param keepBest
   *          the number of best solutions to keep in the list.
   */
  public void setSolutionParameters(int topicClusters, int personAssignments,
                                    int attempts, int keepBest)
  {
    topicClustersField.setValue(new Integer(topicClusters));
    personAssignmentsField.setValue(new Integer(personAssignments));
    attemptsField.setValue(new Integer(attempts));
    keepBestField.setValue(new Integer(keepBest));
  }

  /**
   * Return the number of topic clusters.
   * 
   * @return the number of topic clusters.
   */
  public int getTopicClusters()
  {
    return getIntFromField(topicClustersField);
  }

  /**
   * Return the number of person assignments.
   * 
   * @return the number of person assignments.
   */
  public int getPersonAssignments()
  {
    return getIntFromField(personAssignmentsField);
  }

  /**
   * Return the maximum number of person assignments.
   * 
   * @return the number of assignment attempts.
   */
  public int getAttempts()
  {
    return getIntFromField(attemptsField);
  }

  /**
   * Return the number of best solutions to keep in the list.
   * 
   * @return the number of assignment attempts.
   */
  public int getKeepBest()
  {
    return getIntFromField(keepBestField);
  }

  /**
   * Get the value of a JFormattedField as an int.
   * 
   * @param field
   *          the field.
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

    return ((Integer) field.getValue()).intValue();
  }

  /**
   * Process the command "solve" which start a calculation and forward all other
   * commands to the base class.
   */
  public void processCommand(String command)
  {
    if (command.equals("solve"))
    {
      if (checkConstraints())
      {
        int topicClusters = getTopicClusters();
        int personAssignments = getPersonAssignments();
        int attempts = getAttempts();
        int keepBest = getKeepBest();

        monitor = new ProgressMonitor(frame, "Calculating Solutions...", "", 0,
                                      topicClusters * personAssignments);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(0);
        solveButton.setEnabled(false);
        getSolver().startSolverTask(topicClusters, personAssignments, attempts, keepBest);
        timer.start();
        getChangeMonitor().signalChange();
      }
    }
    else if (command.equals("print"))
    {
      try
      {
        // get selected print format
        String printFormat = (String) printFormatList.getSelectedItem();
        String formatterPath = printDirPath + "/" + printFormat + ".xsl";
        File printFormatter = new File(getClass().getResource(formatterPath).toURI());
        
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer(new StreamSource(printFormatter));
        
        // turn our internal state into an XML string
        StringWriter xmlState = new StringWriter();
        PrintWriter  xmlWriter = new PrintWriter(xmlState);
        stateSaver.save(xmlWriter, 0);
        
        // Construct a Transformer source that contains the XML string
        StreamSource source = new StreamSource(new StringReader(xmlState.toString()));
        
        // Construct a Result that writes to a string
        StringWriter htmlData = new StringWriter();
        StreamResult result = new StreamResult(htmlData);
        
        // Create the HTML document
        transformer.transform(source, result);

        FileWriter testWriter = new FileWriter("test.html");
        PrintWriter testStream = new PrintWriter(testWriter);
        testStream.println(htmlData.toString());
        testStream.close();
        
        // Construct the printable object from the HTML string
        HTMLPrinter htmlPrinter = new HTMLPrinter(htmlData.toString());

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(htmlPrinter);
        
        if (job.printDialog()) /* Displays the standard system print dialog */
        {
          try
          {
            job.print();
          }
          catch (Exception ex)
          {
            System.out.println(ex);
          }
        }
      }
      catch (URISyntaxException e)
      {
        System.out.println("Problem using print format: " + e);
      }
      catch (IOException e)
      {
        System.out.println("I/O-Problem: " + e);
      }
      catch (TransformerConfigurationException e)
      {
        System.out.println("Cannot create transformer: " + e);
      }
      catch (TransformerException e)
      {
        System.out.println("TransformerException: " + e);
      }
    }
    else
      super.processCommand(command);
  }

  /**
   * Check any constraints that must be met on order for the calculation to work
   * as expected.
   * 
   * @return true if the constraints are met, false otherwise.
   */
  protected boolean checkConstraints()
  {
    boolean ok = true;
    StringBuffer msg = new StringBuffer("The following constraints are violated:\n");
    Solver solver = getSolver();
    Roles roles = solver.getRoles();
    int dimRoles = roles.getNumber();
    int dimPersons = solver.getPersons().getNumber();
    int dimSessions = solver.getSessionNumber();
    int minSum = 0;
    int maxSum = 0;
    int perSession = dimPersons / dimSessions;

    for (int r = 0; r < dimRoles; ++r)
    {
      minSum += roles.getMinimumPerSession(r);
      maxSum += roles.getMaximumPerSession(r);
    }

    if (maxSum < perSession)
    {
      ok = false;
      msg.append("  - The sum of the role maxima (" + maxSum
                 + ") is less than\n    the number of participants per session ("
                 + perSession + ")\n");
    }

    if (minSum > perSession)
    {
      ok = false;
      msg.append("  - The sum of the role minima (" + minSum
                 + ") exceeds\n    the number of participants per session (" + perSession
                 + ")\n");
    }

    if (!ok)
    {
      msg.append("Please correct these problems before trying again.");
      JOptionPane.showMessageDialog(this, msg, "Constraint Violation",
                                    JOptionPane.ERROR_MESSAGE);
    }

    return ok;
  }

  /**
   * This class is an ActionListener which is only triggered for timer events.
   * It is used to provide feedback on the progress of a solution calculation
   * and also detects when the calculation is finished.
   */
  private class TimerListener
      implements ActionListener
  {
    /**
     * When called, update the progress bar of the ProgressMonitor and display
     * any progress messages. If the calculation has been canceled or
     * terminated, do the neccessary cleanup work.
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
