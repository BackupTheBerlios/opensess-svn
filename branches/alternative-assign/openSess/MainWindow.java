package openSess;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     2005-02-12
 * Revision ID: $Id: MainWindow.java 10 2005-03-04 18:45:41Z awickner $
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
 * The MainWindow allows simulations to be created and saved.
 * It shows the persons, topics and roles and allows them to be
 * edited. Finally, it allows the simulation to be run and the results
 * to be displayed.
 * 
 * @author awi
 */
public class MainWindow
  extends ChangeMonitor
  implements ActionListener, CommandProcessor, XMLStateSaving
{
  private final String       programName = "OpenSess";
  private final String       fileSuffix  = "ose";
  private JFrame             frame;
  private JLabel             configInfo;
  private ObjectPanel        topicPanel, personPanel, rolePanel;
  private SolutionPanel      solutionPanel;
  private JFileChooser       fileChooser;
  private GlobalNewWindow    globalNewWindow;
  private HelpWindow         helpWindow;
  private Solver             solver;
  private File               currentFile;
  private SAXParserFactory   parserFactory;

  /**
   * Constructs a new MainWindow.
   */
  public MainWindow()
  {
    // Initialise the XML parser factory
    parserFactory = SAXParserFactory.newInstance();
    
    // Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);

    // Create and set up the window.
    frame = new JFrame(programName);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    // Intercept outside attempts to close the window
    frame.addWindowListener(new CloseListener("exit", this));
    
    createMenuBar();
    createContents();

    // Set initial configuration
    reconfigure(12, 12, 3, 2);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
    
    // Set current file
    currentFile = null;

    // Create the file chooser
    fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileFilter()
                              {
                                public String getDescription()
                                {
                                  return "OpenSess files";
                                }
                                
                                public boolean accept(File f)
                                {
                                  String ext = getExtension(f);
                                  return ext != null && ext.equals(fileSuffix);
                                }
                              });
    
    currentFile = fileChooser.getSelectedFile();
    clearChanges();
  }
  
  /**
   * Returns the Solver associated with this MainWindow.
   * 
   * @return the Solver.
   */
  public Solver getSolver()
  {
    return solver;
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
    solutionPanel.setSolutionParameters(topicClusters, personAssignments, attempts);
  }
  
  /**
   * Get the file extension of a file name.
   * 
   * @param f  the file name.
   * @return   the extension of the file name.
   */
  protected String getExtension(File f)
  {
    String ext = null;
    String s   = f.getName();
    int    i   = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) 
      ext = s.substring(i+1).toLowerCase();
    
    return ext;
  }
  
  /**
   * Create the MenuBar of the MainWindow.
   */
  private void createMenuBar()
  {
    JMenuBar menuBar = new JMenuBar();
    frame.setJMenuBar(menuBar);

    // Build the file menu.
    {
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      menu.getAccessibleContext()
          .setAccessibleDescription("This menu contains file functions");
      menuBar.add(menu);

      addItem(menu, "new", "New...", KeyEvent.VK_N, ActionEvent.CTRL_MASK, KeyEvent.VK_N,
              "Create a new simulation");
      addItem(menu, "open", "Open...", KeyEvent.VK_O, ActionEvent.CTRL_MASK,
              KeyEvent.VK_O, "Open an existing simulation");
      menu.addSeparator();
      addItem(menu, "save", "Save", KeyEvent.VK_S, ActionEvent.CTRL_MASK, KeyEvent.VK_S,
              "Save the simulation");
      addItem(menu, "saveAs", "Save As...", KeyEvent.VK_A, ActionEvent.CTRL_MASK,
              KeyEvent.VK_A, "Save the simulation under a file name");
      menu.addSeparator();
      addItem(menu, "exit", "Exit", KeyEvent.VK_X, ActionEvent.CTRL_MASK, KeyEvent.VK_Q,
              "Exit the program");
    }

    // Build the help menu
    {
      JMenu menu = new JMenu("Help");
      menu.setMnemonic(KeyEvent.VK_H);
      menu.getAccessibleContext()
          .setAccessibleDescription("This menu contains all help functions");
      menuBar.add(menu);

      addItem(menu, "help", "Help Contents", KeyEvent.VK_H,
              0, KeyEvent.VK_F1, "Show help contents");
      addItem(menu, "about", "About " + programName + "...", KeyEvent.VK_A,
              ActionEvent.CTRL_MASK, KeyEvent.VK_H, "Show information about the program");
    }
  }
  
  /**
   * Add an item to a menu.
   * 
   * @param menu        the menu to add to.
   * @param command     the ActionCommand of the new item.
   * @param text        the label text of the new item.
   * @param mnemonic    the mnemonic of the new item. 
   * @param accMask     the accelerator mask of the new item.
   * @param accKey      the accelerator key of the new item.
   * @param description the description of the new item.
   */
  private void addItem(JMenu menu, String command, String text, int mnemonic,
                       int accMask, int accKey, String description)
  {
    JMenuItem menuItem = new JMenuItem(text, mnemonic);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(accKey, accMask));
    menuItem.getAccessibleContext().setAccessibleDescription(description);
    menu.add(menuItem);
    menuItem.setActionCommand(command);
    menuItem.addActionListener(this);
  }

  /**
   * Create the contents panel of the MainWindow.
   *
   */
  private void createContents()
  {
    JPanel rootPanel = new JPanel();
    rootPanel.setPreferredSize(new Dimension(600, 500));
    frame.getContentPane().add(rootPanel);
    rootPanel.setLayout(new GridBagLayout());
    rootPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    GridBagConstraints rc = new GridBagConstraints();
    rc.gridx = 0;
    rc.fill  = GridBagConstraints.BOTH;
    rc.weightx = 1.0;

    // Set up the info lines
    configInfo = new JLabel();
    configInfo.setHorizontalAlignment(JLabel.CENTER);
    rc.weighty = 0.0;
    rootPanel.add(configInfo, rc);

    JLabel usageInfo = new JLabel("(Use File/New to reconfigure)");
    usageInfo.setHorizontalAlignment(JLabel.CENTER);
    rootPanel.add(usageInfo, rc);
    
    // Set up the object panel
    JPanel objectPanel = new JPanel();
    objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.LINE_AXIS));
    objectPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
    rc.weighty = 1.0;
    rootPanel.add(objectPanel, rc);

    // Set up the topic panel
    topicPanel = new ObjectPanel("Topics", new EditTopicWindow(frame), solver, this);
    objectPanel.add(topicPanel);
    objectPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    
    // Set up the person panel
    personPanel = new ObjectPanel("Persons", new EditPersonWindow(frame), solver, this);
    objectPanel.add(personPanel);
    objectPanel.add(Box.createRigidArea(new Dimension(10, 0)));

    // Set up the role panel
    rolePanel = new ObjectPanel("Roles", new EditRoleWindow(frame), solver, this);
    objectPanel.add(rolePanel);

    // Set up the solution panel
    solutionPanel = new SolutionPanel(frame, solver, this);
    rc.weighty = 0.0;
    rootPanel.add(solutionPanel, rc);
    
    // Make sure that the solution panel gets informed about all name changes.
    NameChangeListener listener = solutionPanel.getNameChangeListener();
    personPanel.addNameChangeListener(listener);
    topicPanel.addNameChangeListener(listener);
    rolePanel.addNameChangeListener(listener);
  }
  
  /**
   * Process action events.
   * The method just determines the relevant ActionCommands and
   * delegates them to processCommand().
   */
  public void actionPerformed(ActionEvent e)
  {
    AbstractButton item = (AbstractButton)e.getSource();
    processCommand(item.getActionCommand());
  }
   
  /**
   * Process the ActionCommand specified.
   * 
   * @param command the ActionCommand.
   */
  public void processCommand(String command)
  {
    if (command.equals("exit"))
    {
      if (checkUnsavedChanges())
        System.exit(0);
    }
    else if (command.equals("new"))
    {
      getGlobalNewWindow().getConfig(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          GlobalNewWindow win = getGlobalNewWindow();
        
          if (checkUnsavedChanges())
            reconfigure(win.getNumberOfTopics(), win.getNumberOfPersons(),
                        win.getNumberOfRoles(), win.getNumberOfSessions());
        }
      });
    }
    else if (command.equals("open"))
      open();
    else if (command.equals("saveAs"))
    {
      int returnVal = fileChooser.showSaveDialog(frame);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
          currentFile = fileChooser.getSelectedFile();
          
          if (getExtension(currentFile) == null)
            currentFile = new File(currentFile.getParent(), 
                                   currentFile.getName() + "." + fileSuffix);
          
          save();
      }
    }
    else if (command.equals("save"))
      save();
    else if (command.equals("help"))
      getHelpWindow().setVisible(true);
    else if (command.equals("about"))
    {
      String date     = getSubversionString("$LastChangedDate: 2005-03-04 18:45:41Z $");
      String revision = getSubversionString("$LastChangedRevision: 10 $");
      
      JOptionPane.showMessageDialog(frame,
                                    programName + "\n\n"	
                                    + "Version: 0.5 revision " + revision + "\n" 
                                    + "Created: " + date + "\n" 
                                    + "Algorithms: Gero Scholz\n"
                                    + "User Interface: Andreas Wickner");
    }
  }
  
  /**
   * Return the value of a Subversion keyword string.
   * 
   * @param svnStr the Subversion keyword string.
   * @return the value of the keyword string.
   */
  protected String getSubversionString(String svnStr)
  {
    return svnStr.substring(svnStr.indexOf(": ")+2, svnStr.indexOf(" $"));
  }
  
  /**
   * Reconfigure the MainWindow with new numbers of topics, persons,
   * roles and sessions. It also closes all secondary windows, so that
   * the reconfiguration does not cause any trouble there.
   * 
   * @param topicNumber   the new number of topics.
   * @param personNumber  the new number of persons.
   * @param roleNumber    the new number of roles.
   * @param sessionNumber the new number of sessions.
   */
  protected void reconfigure(int topicNumber, int personNumber, int roleNumber,
                             int sessionNumber)
  {
    topicPanel.hideEditor();
    personPanel.hideEditor();
    rolePanel.hideEditor();
    solutionPanel.hideEditor();
    
    solver = new Solver(topicNumber, personNumber, roleNumber, sessionNumber);
    
    // reconfigure the panels
    topicPanel.reconfigure(solver, solver.getTopics().getNames());
    personPanel.reconfigure(solver, solver.getPersons().getNames());
    rolePanel.reconfigure(solver, solver.getRoles().getNames());
    solutionPanel.reconfigure(solver, solver.getSolutionNames());

    configInfo.setText("Configured for " + topicNumber + " topics, "
                       + personNumber + " persons, " + roleNumber + " roles and "
                       + sessionNumber + " sessions.");
  }
 
  /**
   * Lets the user select a new file and loads the file into the MainWindow.
   *
   */
  protected void open()
  {
    int returnVal = fileChooser.showOpenDialog(frame);

    if (returnVal == JFileChooser.APPROVE_OPTION
        && checkUnsavedChanges())
    {
      currentFile = fileChooser.getSelectedFile();
      
      try
      {
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(currentFile, new SolverConstructor(this));
      }
      catch (ParserConfigurationException e)
      {
        JOptionPane.showMessageDialog(frame, e.toString(), "Error configuring parser", 
                                      JOptionPane.ERROR_MESSAGE); 
      }
      catch (SAXException e)
      {
        JOptionPane.showMessageDialog(frame, e.toString(), "Error reading file", 
                                      JOptionPane.ERROR_MESSAGE); 
      }
      catch (IOException e)
      {
        JOptionPane.showMessageDialog(frame, e.toString(), "Error reading file", 
                                      JOptionPane.ERROR_MESSAGE); 
      }

      frame.setTitle(programName + " - " + currentFile.getAbsolutePath());
      solutionPanel.setSelectedIndex(0);
    }
  }
  
  /**
   * Saves the current state into the currently selected file.
   *
   */
  protected void save()
  {
    try
    {
      if (currentFile == null)
        currentFile = new File(fileChooser.getCurrentDirectory(), "untitled." + fileSuffix);
      
      System.out.println("Saving as: " + currentFile.getAbsolutePath());
      frame.setTitle(programName + " - " + currentFile.getAbsolutePath());
      
      FileWriter writer = new FileWriter(currentFile);
      PrintWriter stream = new PrintWriter(writer);
      
      stream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      save(stream, 0);
      stream.close();
      clearChanges();
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog(frame, e.toString(), "Error writing file", 
                                    JOptionPane.ERROR_MESSAGE); 
    }
  }
  
  /**
   * Implements XMLStateSaving.save() for saving the current state.
   * It also calls the save-methods of the various Solver-objects.  
   */
  public void save(PrintWriter stream, int level)
  {
    stream.println("<openconclave topics=\"" + solver.getTopics().getNumber()
                   + "\" persons=\"" + solver.getPersons().getNumber()
                   + "\" roles=\"" + solver.getRoles().getNumber()
                   + "\" sessions=\"" + solver.getSessionNumber()
                   + "\">");
    
    solver.getTopics().save(stream, level+1);
    solver.getPersons().save(stream, level+1);
    solver.getRoles().save(stream, level+1);

    Indenter.println(stream, level+1, "<solutionParameters topicClusters=\""
                     + solutionPanel.getTopicClusters() + "\" personAssignments=\""
                     + solutionPanel.getPersonAssignments() + "\" attempts=\""
                     + solutionPanel.getAttempts() + "\"/>");
    
    Indenter.println(stream, level+1, "<solutions>");
    
    Vector solutions = solver.getSolutions();
    
    for (int s = 0;  s < solutions.size();  ++s)
      ((Solution)solutions.elementAt(s)).save(stream, level+2);
    
    Indenter.println(stream, level+1, "</solutions>");
    stream.println("</openconclave>");
  }

  /**
   * Check whether there are unsaved changes and returns true if there are none.
   * Otherwise it returns false.
   * 
   * @return true if there are no unsaved changes, false otherwise.
   */
  public boolean checkUnsavedChanges()
  {
    if (!hasChanged())
      return true;
    
    return 	JOptionPane.YES_OPTION
    	== JOptionPane.showConfirmDialog(frame, 
    	                                 "There are changes that have not been saved.\nDo you really want to continue with this operation?",
    	                                 "Discard unsaved changes?", 
    	                                 JOptionPane.YES_NO_OPTION, 
    	                                 JOptionPane.QUESTION_MESSAGE);
  }
  
  /**
   * Returns the GlobalNewWindow. The first call creates it.
   * @return the GlobalNewWindow.
   */
  protected GlobalNewWindow getGlobalNewWindow()
  {
    if (globalNewWindow == null)
      globalNewWindow = new GlobalNewWindow(frame);
    
    return globalNewWindow;
  }
  
  /**
   * Returns the HelpWindow. The first call creates it.
   * @return the HelpWindow.
   */
  protected HelpWindow getHelpWindow()
  {
    if (helpWindow == null)
      helpWindow = new HelpWindow(frame);
    
    return helpWindow;
  }
}
