package openSess;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     19.02.2005
 * Revision ID: $Id: ShowSolutionWindow.java 10 2005-03-04 18:45:41Z awickner $
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
 * ShowSolutionWindow extends ListEditWindow to present solutions
 * to the user.
 * 
 * @author andreas
 */
public class ShowSolutionWindow
  extends ListEditWindow
{
  private JLabel meanDevValue, maxDevValue, stdDevValue, targetValue;
  private JPanel topicsPanel, rolesPanel;
  private Color  gColor[];
  private Solver solver;
  private int    selected;
  
  /**
   * Construct a new ShowSolutionWindow.
   * 
   * @param frame  the parent JFrame.
   */
  ShowSolutionWindow(JFrame frame)
  {
    super(frame, "Show Solution");
    selected = -1;
  }
  
  /**
   * Returns a NameChangeListener that will redisplay the window
   * when any name change is signalled.
   * 
   * @return a NameChangeListener.
   */
  public NameChangeListener getNameChangeListener()
  {
    return new RedisplayOnNameChange(this);
  }
  
  /**
   * Add a JTabbedPane to the JPanel.
   */
  protected void addAdditionalComponents(JPanel panel)
  {
    JTabbedPane tabPanel = new JTabbedPane();
    tabPanel.setPreferredSize(new Dimension(500, 500));
    panel.add(tabPanel);
    
    // Add the panels to the tabbed panel
    JPanel statPanel = new JPanel();
    statPanel.setLayout(new GridBagLayout());
    statPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    tabPanel.add("Statistics", statPanel);
    
    topicsPanel = new JPanel();
    topicsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    JScrollPane topicsScrollPanel = new JScrollPane(topicsPanel);
    tabPanel.add("Topic Clustering", topicsScrollPanel);
    
    rolesPanel = new JPanel();
    rolesPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    JScrollPane rolesScrollPanel = new JScrollPane(rolesPanel);
    tabPanel.add("Role Assignment", rolesScrollPanel);
    
    // Set up Statistics
    meanDevValue        = new JLabel();
    maxDevValue         = new JLabel();
    stdDevValue         = new JLabel();
    targetValue         = new JLabel();
   
    GridBagConstraints lc = new GridBagConstraints();
    lc.gridx  = 0;
    lc.gridy  = GridBagConstraints.RELATIVE;
    lc.anchor = GridBagConstraints.LINE_START;
    lc.insets = new Insets(0, 0, 5, 10);
    GridBagConstraints vc = new GridBagConstraints();
    vc.gridx = GridBagConstraints.RELATIVE;
    vc.fill  = GridBagConstraints.HORIZONTAL;
    vc.anchor = GridBagConstraints.LINE_START;
    vc.weightx = 1.0;
    vc.weighty = 0.0;
    
    statPanel.add(new JLabel("Mean Deviation:"), lc);
    vc.gridy = 0;
    statPanel.add(meanDevValue, vc);
    statPanel.add(new JLabel("Maximum Deviation:"), lc);
    vc.gridy = 1;
    statPanel.add(maxDevValue, vc);
    statPanel.add(new JLabel("Standard Deviation:"), lc);
    vc.gridy = 2;
    statPanel.add(stdDevValue, vc);
    statPanel.add(new JLabel("Target Value:"), lc);
    vc.gridy = 3;
    statPanel.add(targetValue, vc);

    lc.weighty = 1.0;
    statPanel.add(Box.createVerticalGlue(), lc);
  }

  /**
   * Create the neccessary elements for solutions as configured in
   * the Solver object.
   */
  protected void additionalEditSetup(ComboBoxModel model, Solver solver, int selected,
                                     ChangeMonitor changeMonitor)
  {
    this.solver = solver;
    this.selected = selected;
    
    Topics topics = solver.getTopics();
    Persons persons = solver.getPersons();
    int tNumber = topics.getNumber();   // Number of topics
    int pNumber = persons.getNumber();  // Number of persons
    int sNumber = solver.getSessionNumber();   // Number of sessions
    int gNumber = tNumber / sNumber;    // Number of topic groups
    gColor = new Color[gNumber]; // Colors for topic groups
    
    //System.out.println("addEdSetup: t" + tNumber + ", p" + pNumber
    //                   + ", s" + sNumber + ", g" + gNumber);
    
    // Create Layout of Topic Clustering Panel
    topicsPanel.removeAll();
    topicsPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(0, 0, 5, 10);
    c.weightx = 0.0;
    c.weighty = 0.0;
    
    // Create the group colors
    for (int g = 0;  g < gNumber;  ++g)
      gColor[g] = new Color(Color.HSBtoRGB(g / (float)gNumber, 0.9f, 0.7f));
    
    for (int g = 0;  g < gNumber+1;  ++g)
      for (int s = 0;  s < sNumber+1;  ++s)
      {
        c.gridx = s;
        c.gridy = g;
        
        JLabel label = new JLabel();
        topicsPanel.add(label, c);

        if (g > 0)
          label.setForeground(gColor[g-1]);  // set color of sessions
        
        if (g == 0)  // Column headers
        {
          if (s > 0)
            label.setText("Session " + s);
        }
        else if (s == 0)  // Row headers
        {
          if (g > 0)
            label.setText("Group " + g);
        }
      }
     
    // add cells to the layout that push it to the upper left
    c.gridx = sNumber+1;
    c.gridy = 0;
    c.weightx = 1.0;
    topicsPanel.add(Box.createHorizontalGlue(), c);
    c.gridx = 0;
    c.gridy = gNumber+1;
    c.weightx = 0.0;
    c.weighty = 1.0;
    topicsPanel.add(Box.createVerticalGlue(), c);
    
    // Create Layout of Role Assignment Panel
    rolesPanel.removeAll();
    rolesPanel.setLayout(new GridBagLayout());
    c.weightx = 0.0;
    c.weighty = 0.0;
    
    for (int p = 0;  p < pNumber+1;  ++p)
      for (int t = 0; t < tNumber+2;  ++t)
      {
        c.gridx = t;
        c.gridy = p;
        JLabel label = new JLabel();
        rolesPanel.add(label, c);
      }

    // add cells to the layout that push it to the upper left
    c.gridx = tNumber+2;
    c.gridy = 0;
    c.weightx = 1.0;
    rolesPanel.add(Box.createHorizontalGlue(), c);
    c.gridx = 0;
    c.gridy = pNumber+1;
    c.weightx = 0.0;
    c.weighty = 1.0;
    rolesPanel.add(Box.createVerticalGlue(), c);

    update();
  }
  
  /**
   * Update all solution panels with the currently selected solution.
   */
  protected void additionalChangesOnSelection(Solver solver, int selected)
  {
    this.solver = solver;
    this.selected = selected;
    update();
  }

  /**
   * Update all solution panels with the currently selected solution.
   */
  protected void update()
  {
    if (solver == null || selected < 0)
      return;
    
    Solution solution = (Solution) solver.getSolutions().elementAt(selected);
    Topics   topics   = solver.getTopics();
    Persons  persons  = solver.getPersons();
    Roles    roles    = solver.getRoles();
    int      tNumber  = topics.getNumber();
    int      pNumber  = persons.getNumber();
    int      gNumber  = solution.getGroupNumber();
    int      sNumber  = solution.getGroupSize();
    
    //System.out.println("update: t" + tNumber + ", p" + pNumber
    //                   + ", s" + sNumber + ", g" + gNumber);
        
    // Update statistics
    meanDevValue.setText("" + solution.getMeanDeviation());
    maxDevValue.setText("" + solution.getMaximumDeviation());
    stdDevValue.setText("" + solution.getStandardDeviation());
    targetValue.setText("" + solution.getTargetValue());

    // Update topic clustering
    for (int g = 1;  g < gNumber+1;  ++g)
      for (int s = 1;  s < sNumber+1;  ++s)
      {
        JLabel label = (JLabel)topicsPanel.getComponent(g*(sNumber+1) + s);
        label.setText(topics.getName(solution.getGroupElement(g-1, s-1)));
      }

    // Re-color the column headers
    for (int t = 0;  t < tNumber;  ++t)
    {
      JLabel label = (JLabel)rolesPanel.getComponent(t+1);
      label.setForeground(gColor[solution.topicToGroup(t)]);
    }
    
    // Update role assignments
    for (int p = 0;  p < pNumber+1;  ++p)
      for (int t = 0;  t < tNumber+2;  ++t)
      {
        JLabel label = (JLabel)rolesPanel.getComponent(p*(tNumber+2) + t);

        if (p == 0) // Column headers
        {
          if (t > 0)
            if (t == tNumber+1)
              label.setText("Dev.");
            else
              label.setText(topics.getName(t-1));
        }
        else if (t == 0) // Row headers
        {
          if (p > 0)
            label.setText(persons.getName(p-1));
        }
        else if (t < tNumber+1)  // Role fields
        {
          label.setText(roles.getNameExtended(solution.getRole(p-1, t-1)));
          label.setForeground(gColor[solution.topicToGroup(t-1)]);
        }
        else  // deviation sums
          label.setText("" + solution.getPersonSum(p-1));
      }
  }
  
  
  private class RedisplayOnNameChange
    implements NameChangeListener
  {
    private ShowSolutionWindow window;
    
    RedisplayOnNameChange(ShowSolutionWindow window)
    {
      this.window = window;
    }
    
    public void nameChanged(int index, String oldName, String newName)
    {
      System.out.println("Solution update on name change " + oldName + " -> " + newName);
      window.update();
    }
  }

}
