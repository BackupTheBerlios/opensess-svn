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
  private double personSat[];
  private double meanSatisfaction, minSatisfaction, stdDeviation;
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
    personSat   = new double[dimPersons];
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
   * Return the satisfaction value for a specified person.
   * 
   * @param person the person.
   * @return the satisfaction value.
   */
  public double getPersonSatisfaction(int person)
  {
    return personSat[person];
  }
	
  public boolean greaterThan(Solution other)
  {
    if (this == other)
      return false;  // identical
    
    int thisMean = (int)(this.getMeanSatisfaction() * 1000);
    int otherMean = (int)(other.getMeanSatisfaction() * 1000);
    
    if (thisMean > otherMean)
      return true;
    else if (thisMean < otherMean)
      return false;
    
    // same mean deviation, compare max deviation
    return this.getMinimumSatisfaction() > other.getMinimumSatisfaction();
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
   * Return the mean satisfaction value of this solution.
   * 
   * @return the mean satisfaction.
   */
  public double getMeanSatisfaction()
  {
    return meanSatisfaction;
  }
  
  /**
   * Return the minimum satisfaction value of this solution.
   * 
   * @return the minimum satisfaction.
   */
  public double getMinimumSatisfaction()
  {
    return minSatisfaction;
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
  public void setPersonSum(int person, double sum)
  {
    personSat[person] = sum;
  }
  
  /**
   * Evaluate the solution and set the statistical values.
   */
  protected void evaluate()
  {
    calculateTargetValue();
    
    Persons persons = solver.getPersons();
    Roles   roles   = solver.getRoles();
    int     dimPersons = persons.getNumber();
    int     dimTopics  = solver.getTopics().getNumber();
    int     dimRoles   = solver.getRoles().getNumber();
    int     dimSessions = solver.getSessionNumber();
    int     personsPerSession = dimPersons / dimSessions;
    int     sumMax = 0;
    boolean debug = false;
    
    // Create a vector that holds the maximum achievable roles
    // in a session, which includes a special role for
    // non-participation (dimRoles+1).
    int optRoles[] = new int[dimPersons];
    int currentRole = 0;
    int rolesLeft = 0;
    
    if (debug)
      System.out.println("\noptRoles:");
    
    for (int p = 0;  p < dimPersons;  ++p)
    {
      if (p >= personsPerSession)
        optRoles[p] = dimRoles + 1;
      else
      {	
        if (rolesLeft <= 0)
          // next role: see how many we can use at maximum
          rolesLeft = roles.getMaximumPerSession(currentRole++);
      
        if (rolesLeft-- > 0)
          optRoles[p] = currentRole;
      }
    
      if (debug)
        System.out.println(p + ": " + optRoles[p]);
    }
    
    // Record and sum up the satisfaction values for all participants
    double sat[][] = new double[dimPersons][dimTopics];
    double total   = 0;
    double minSat  = 1.0;
    
    for (int p = 0;  p < dimPersons;  ++p)
    {
      personSat[p] = 0;
      
      for (int t = 0;  t < dimTopics;  ++t)
      {
        int actualRole = getRole(p, t);
        
        if (actualRole == 0)
          actualRole = dimRoles+1;
        
        int optimalRole = optRoles[(persons.getPreferenceIndex(p, t) * dimPersons) 
                                   / dimTopics];
        double s = 1.0 - Math.abs(optimalRole - actualRole) / (double)dimRoles;
        
        if (debug)
          System.out.println(p + "," + t + ": act " + actualRole
                             + "  opt " + optimalRole + "  sat " 
                             + s);
        
        sat[p][t]     = s;
        personSat[p] += s;
        total        += s;
      }
    
      personSat[p] /= dimTopics;
      
      if (personSat[p] < minSat)
        minSat = personSat[p];
      
      if (debug)
        System.out.println("Total satisfaction for P" + p + ": " + personSat[p]);
    }

    double mean = total / (dimPersons * dimTopics);
    double dev = 0;
    
    for (int p = 0;  p < dimPersons;  ++p)
      for (int t = 0;  t < dimTopics;  ++t)
      {
        double diff = mean - sat[p][t];
        dev += diff * diff;
      }
    
    meanSatisfaction = mean;
    minSatisfaction  = minSat;  
    stdDeviation     = Math.sqrt(dev / (dimPersons * dimTopics));
  }

  /**
   * Produce an XML representation of the Solution.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<solution name=\""
                     + getName() + "\">");

    Indenter.println(stream, level+1, "<statistics meandev=\""
                     + getMeanSatisfaction() + "\" maxdev=\""
                     + getMinimumSatisfaction() + "\" stddev=\""
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
                       + getPersonSatisfaction(p) + "\"/>");
    
    Indenter.println(stream, level+1, "</personSums>");
    
    Indenter.println(stream, level, "</solution>");
  }

  public String debugString()
  {
    int     dimPersons = solver.getPersons().getNumber();
    int     dimTopics  = solver.getTopics().getNumber();
    StringBuffer s = new StringBuffer();
   
    s.append("\n    ");
    
    for (int t = 0; t < dimTopics; t++)
      s.append(" T" + t);
    
    s.append("\n");
    
    for (int p = 0; p < dimPersons; p++)
    {
      s.append("P" + p + ": ");

      for (int t = 0; t < dimTopics; t++)
        s.append("  " + getRole(p, t));
      
      s.append("\n");
    }
    
    return s.toString();
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

      String tmp = "       " + personSat[p];

      s.append(tmp.substring(tmp.length() - 5) + "\n");
    }
    
    s.append(persons.emptyName() + "   " + topics.toHeaderString(" ") + "\n");
    s.append("    mittlere Abweichung=" + getMeanSatisfaction()
         + "    maximale Abweichung=" + getMinimumSatisfaction()  
         + "    Standardabw.=" + getStandardDeviation()
         + "    target=" + calculateTargetValue()
         + "\n\n");
    
    return s.toString();
  }

}
