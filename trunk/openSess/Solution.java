package openSess;
import java.io.PrintWriter;
import java.util.Arrays;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     18.02.2005
 * Revision ID: $Id: Solution.java 10 2005-03-04 18:45:41Z awickner $
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
    int dimTopics = solver.getTopics().getNumber();
    int dimSessions = solver.getSessionNumber();
    int dimPersons  = solver.getPersons().getNumber(); 
    this.solver = solver;
    groupNumber = dimTopics / dimSessions;
    groupSize   = dimSessions;
    group       = new int[groupNumber][groupSize];
    role        = new int[dimPersons][dimTopics];
    personSum   = new int[dimPersons];
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
  
  public void clearRoles()
  {
    int dimPersons = solver.getPersons().getNumber();
    
    for (int person = 0;  person < dimPersons;  ++person)
      Arrays.fill(role[person], 0);  
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
	 * Calculates the target value of the current assignment.
	 * 
	 * @return the target value.
	 */
  protected int calculateTargetValue()
  {
    Persons persons    = solver.getPersons();
    Topics  topics     = solver.getTopics();
    int     dimPersons = persons.getNumber();
    int     dimTopics  = topics.getNumber();
    
		int pval[] = new int[dimPersons];
    int val = 0;

    for (int p = 0;  p < dimPersons;  ++p)
    {
      for (int t = 0;  t < dimTopics; ++t)
        if (getRole(p, t) > 0)
          pval[p] += persons.getPreferenceIndex(p, t);

      val += pval[p];
    }
    
    double mean = (val * 1.0 / dimPersons);
    double dev = 0;
    
    for (int p = 0;  p < dimPersons;  ++p)
      dev += (mean - pval[p]) * (mean - pval[p]);

    dev = Math.sqrt(dev / dimPersons);
    val += solver.getBalancingWeight() * dev;
    targetValue = val;
    
    return val;
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
   * Evaluate the solution and set the statistical values.
   */
  protected void evaluate()
  {
    Persons persons = solver.getPersons();
    int     dimPersons = persons.getNumber();
    int     dimTopics  = solver.getTopics().getNumber();
    int     dimSessions = solver.getSessionNumber();
    float   total = 0;
    int     sumMax = 0;
    int     pval[] = new int[dimPersons];
    int     val = 0;
    
    for (int p = 0; p < dimPersons; p++)
    {
      for (int t = 0; t < dimTopics; t++)
        if (getRole(p, t) > 0)
          pval[p] += persons.getPreferenceIndex(p, t);
      
      val += pval[p];
    }
    
    double mean = ((double)val) / dimPersons;
    double dev = 0;
    
    for (int p = 0;  p < dimPersons;  p++)
      dev += (mean - pval[p]) * (mean - pval[p]);
    
    dev = Math.sqrt(dev / dimPersons);
         
    for (int p = 0; p < dimPersons; p++)
    {
      int sum = -(dimTopics / dimSessions)
                * ((dimTopics / dimSessions) - 1) / 2;
      
      for (int t = 0; t < dimTopics; t++)
        if (getRole(p, t) > 0)
          sum += persons.getPreferenceIndex(p, t); 
      
      personSum[p] = sum;
      
      total += sum;
      
      if (sumMax < sum)
        sumMax = sum;
    }

    meanDeviation = total / dimPersons;
    maxDeviation  = sumMax;  
    stdDeviation  = dev;
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
  
  public String toString()
  {
    Persons persons = solver.getPersons();
    Topics  topics  = solver.getTopics();
    int     dimPersons = persons.getNumber();
    int     dimTopics  = topics.getNumber();
    
    // return the final allocation matrix
    StringBuffer s = new StringBuffer(persons.emptyName() + "   " + topics.toHeaderString(" ") + "\n");
         
    for (int p = 0; p < dimPersons; p++)
    {
      s.append(persons.getName(p) + "  ");

      for (int t = 0; t < dimTopics; t++)
        s.append(" " + solver.getRoles().getNameExtended(getRole(p, t)));

      String tmp = "       " + personSum[p];

      s.append(tmp.substring(tmp.length() - 5) + "\n");
    }
    
    s.append(persons.emptyName() + "   " + topics.toHeaderString(" ") + "\n");
    s.append("    mittlere Abweichung=" + getMeanDeviation()
         + "    maximale Abweichung=" + getMaximumDeviation()  
         + "    Standardabw.=" + getStandardDeviation()
         + "    target=" + calculateTargetValue()
         + "\n\n");
    
    return s.toString();
  }

}
