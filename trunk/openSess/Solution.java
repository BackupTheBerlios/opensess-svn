package openSess;
import java.io.PrintWriter;
import java.util.Arrays;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     18.02.2005
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
 * Solution encapsulates the data of a... solution.
 * 
 * @author andreas
 */
public class Solution
  implements XMLStateSaving
{
  private Solver solver;
  private String name;
  private int    group[][];
  private int    role[][];
  private int    personSum[];
  private double meanDeviation, maxDeviation, stdDeviation;
  private int    targetValue;
  private int    groupNumber;
  private int    groupSize;

  /**
   * Constructs a new Solution with the dimensions specified in a Solver object.
   * 
   * @param solver a Solver object.
   */
  Solution(Solver solver)
  {
    this.solver = solver;
    groupNumber = solver.getTopics().getNumber() / solver.getSessionNumber();
    groupSize   = solver.getSessionNumber();
    group = new int[groupNumber][groupSize];
    role = new int[solver.getPersons().getNumber()][solver.getTopics().getNumber()];
    personSum = new int[solver.getPersons().getNumber()];
  }

  /**
   * Return the name of the solution.
   * @return the name.
   */
  String getName()
  {
    return name;
  }
  
  /**
   * Set the name of the solution.
   * 
   * @param name the name.
   */
  void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Return the number of topic groups.
   * 
   * @return the number of topic groups.
   */
  public int getGroupNumber()
  {
    return groupNumber;
  }
  
  /**
   * Return the number of topics in each group.
   * 
   * @return the number of topics in each group.
   */
  public int getGroupSize()
  {
    return groupSize;
  }
  
  /**
   * Return the topic of the specified group and index.
   * 
   * @param group  the group.
   * @param index  the index within the group.
   * @return the topic index.
   */
  public int getGroupElement(int group, int index)
  {
    return this.group[group][index];   
  }
  
  /**
   * Return the group index for a given topic.
   * 
   * @param topic a topic index.
   * @return the group index.
   */
  public int topicToGroup(int topic)
  {
    for (int g = 0;  g < getGroupNumber();  ++g)
      for (int s = 0;  s < getGroupSize();  ++s)
        if (getGroupElement(g, s) == topic)
          return g;
        
    return -1;
  }
  
  /**
   * Return the role of the specified person with regard to the specified topic.
   * @param person the person.
   * @param topic the topic.
   * @return the role.
   */
  public int getRole(int person, int topic)
  {
    return role[person][topic];
  }
  
  /**
   * Return the sum of deviations for a specified person.
   * 
   * @param person the person.
   * @return the sum of deviations.
   */
  public int getPersonSum(int person)
  {
    return personSum[person];
  }
  
  /**
   * Return the mean deviation of this solution.
   * 
   * @return the mean deviation.
   */
  public double getMeanDeviation()
  {
    return meanDeviation;
  }
  
  /**
   * Return the maximum deviation of this solution.
   * 
   * @return the maximum deviation.
   */
  public double getMaximumDeviation()
  {
    return maxDeviation;
  }
  
  /**
   * Return the standard deviation of this solution.
   * 
   * @return the standard deviation.
   */
  public double getStandardDeviation()
  {
    return stdDeviation;
  }
  
  /**
   * Return the target value of this solution.
   * 
   * @return the target value.
   */
  public int getTargetValue()
  {
    return targetValue;
  }
  
  /**
   * Set the topic of the specified group and index to the specified topic.
   * 
   * @param group  the group.
   * @param index  the index within the group.
   * @param topic  the new topic index.
   */
  public void setGroupElement(int group, int index, int topic)
  {
    this.group[group][index] = topic;
  }
  
  /**
   * Clear all role assignments of this solution.
   */
  public void clearRoleAssignments()
  {
    for (int person = 0;  person < solver.getPersons().getNumber();  ++person)
      Arrays.fill(role[person], 0);
  }

  /**
   * Set the role of the specified person with regard to the specified topic.
   * @param person the person.
   * @param topic the topic.
   * @param role the new role.
   */
  public void setRole(int person, int topic, int role)
  {
    this.role[person][topic] = role;
  }
  
  /**
   * Set the sum of deviations for a specified person.
   * 
   * @param person the person.
   * @param sum the sum of deviations.
   */
  public void setPersonSum(int person, int sum)
  {
    personSum[person] = sum;
  }

  /**
   * Set the statistical values of this solution.
   * 
   * @param meanDeviation the mean deviation.
   * @param maxDeviation  the maximum deviation.
   * @param stdDeviation  the standard deviation.
   * @param targetValue   the target value.
   */
  public void setStatistics(double meanDeviation, double maxDeviation, 
                            double stdDeviation, int targetValue)
  {
    this.meanDeviation = meanDeviation;
    this.maxDeviation  = maxDeviation;
    this.stdDeviation  = stdDeviation;
    this.targetValue   = targetValue;
  }

  /**
   * Produce an XML representation of the Solution.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<solution name=\""
                     + getName() + "\">");

    Indenter.println(stream, level+1, "<statistics meandev=\""
                     + getMeanDeviation() + "\" maxdev=\""
                     + getMaximumDeviation() + "\" stddev=\""
                     + getStandardDeviation() + "\" target=\""
                     + getTargetValue() + "\"/>");
    
    Indenter.println(stream, level+1, "<topicGroups>");
    
    for (int g = 0;  g < getGroupNumber();  ++g)
    {
      Indenter.println(stream, level+2, "<topicGroup>");
      
      for (int s = 0;  s < getGroupSize();  ++s)
        Indenter.println(stream, level+3, "<groupTopic index=\""
                         + getGroupElement(g, s) + "\"/>");
      
      Indenter.println(stream, level+2, "</topicGroup>");
    }
    
    Indenter.println(stream, level+1, "</topicGroups>");

    Indenter.println(stream, level+1, "<roleAssignments>");
    
    for (int p = 0;  p < solver.getPersons().getNumber();  ++p)
      for (int t = 0;  t < solver.getTopics().getNumber();  ++t)
        {
        	int role = getRole(p, t);
        	
        	if (role > 0 && role <= solver.getRoles().getNumber())
        	  Indenter.println(stream, level+2, "<roleAssignment person=\""
        	                   + p + "\" topic=\""
        	                   + t + "\" role=\""
        	                   + role + "\"/>");
        }
    
    Indenter.println(stream, level+1, "</roleAssignments>");

    Indenter.println(stream, level+1, "<personSums>");
    
    for (int p = 0;  p < solver.getPersons().getNumber();  ++p)
      Indenter.println(stream, level+2, "<personSum sum=\"" 
                       + getPersonSum(p) + "\"/>");
    
    Indenter.println(stream, level+1, "</personSums>");
    
    Indenter.println(stream, level, "</solution>");
  }
}
