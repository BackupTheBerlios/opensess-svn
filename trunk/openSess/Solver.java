/*
 * Copyright 2005 Gero Scholz, Andreas Wickner
 * 
 * Created:     2005-02-11 
 * Revision ID: $Id: Solver.java 10 2005-03-04 18:45:41Z awickner $
 * 
 * 2005-02-14/AW: Changes to decrease excessive memory allocation/deallocation
 * 2005-02-22/GS: Algorithm bug fixes
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

package openSess;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import javax.swing.DefaultListModel;

/**
 * This is the central class of the solution algorithm.
 * It maintains the dimensions of the various object lists
 * as well as the object lists themselves. It provides
 * the solution algorithm and analysis functions.
 * The algorithm must be executed in a different thread, therefore
 * it is implemented as the doTask() method of a TaskMonitor.
 *  
 * @author Gero Scholz
 */
public class Solver
extends TaskMonitor
{
  private int              dimSessions;
  private int              dimShuffle   = 100;  // the greater the more randomly
                                                // preferences will
                                                // be distributed.
                                                // should be 3 * dimTopics or more, values < 3 will lead
                                                // to pathologic / trivial distributions
  private int              dimBalancing = 20;   // weight of a component in the
                                                // target function which
                                                // favors evenly distributed solutions; try values between
                                                // 0 and 100 (or greater); large values will usually tend
                                                // to make the overall result worse
  private Topics           topics;
  private Persons          persons;
  private Roles            roles;
  private Vector           solutions;
  private DefaultListModel solutionNames;
  private int              dimTryTopicClustering;
  private int              dimTryPersonAssignment;
  private int              dimTryAlloc;
  private boolean          debug;
  private Solution         solution;
  private int              bestAssignment[][];
  private int              topicRole[][];
  private int              personRole[][];
  private int              seqPersons[][];
  private NumberFormat     compactFormat;
  private boolean          solved;
  
  /*
   * currently there is a tendency to find an ideal solution for some persons
   * whereas others are far less well treated. Maybe a change in the target
   * function of the allocation process could change this.
   */

  /**
   * Create a new Solver with the specified dimensions.
   * 
   * @param dimTopics   the number of topics.
   * @param dimPersons  the number of persons.
   * @param dimRoles    the number of roles.
   * @param dimSessions the number of sessions.
   */
  public Solver(int dimTopics, int dimPersons, int dimRoles, int dimSessions)
  {
    this.dimSessions = dimSessions;

    topics        = new Topics(this, dimTopics);
    persons       = new Persons(this, dimPersons, dimTopics);
    roles         = new Roles(this, dimRoles);
    solutions     = new Vector();
    solutionNames = new DefaultListModel();
  }

  /**
   * Return the weight of a component in the target function which
   * favors evenly distributed solutions.; try values between
   * 
   * @return the balancing weight.
   */
  public int getBalancingWeight()
  {
    return dimBalancing;
  }
  
  /**
   * Return the number of sessions.
   * 
   * @return the number of sessions.
   */
  public int getSessionNumber()
  {
    return dimSessions;
  }

  /**
   * Return the number of topics.
   * 
   * @return the number of topics.
   */
  public Topics getTopics()
  {
    return topics;
  }
  
  /**
   * Return the number of persons.
   * 
   * @return the number of persons.
   */
  public Persons getPersons()
  {
    return persons;
  }
  
  /**
   * Return the number of roles.
   * 
   * @return the number of roles.
   */
  public Roles getRoles()
  {
    return roles;
  }
  
  /**
   * Return the list of solutions.
   * 
   * @return the list of solutions.
   */
  public Vector getSolutions()
  {
    return solutions;
  }
  
  /**
   * Return the list of solution names.
   * 
   * @return the list of solution names.
   */
  public DefaultListModel getSolutionNames()
  {
    return solutionNames;    
  }
  
  /**
   * Add a solution to the list of solutions.
   * 
   * @param solution
   */
  public void addSolution(Solution solution)
  {
    solutions.addElement(solution);
    solutionNames.addElement(solution.getName());
  }
  
  /**
   * Start a solution calculation as a separate task.
   * This is neccessary in order not to block the GUI thread.
   * 
   * @param dimTryTopicClustering  the number of topic clusterings to try.
   * @param dimTryPersonAssignment the number of topic/person assignments to try.
   * @param dimTryAlloc            the maximum number of assignments to try.
   */
  public void startSolverTask(int dimTryTopicClustering, 
                             int dimTryPersonAssignment,
                             int dimTryAlloc)
  {
    final Solver theSolverItself = this;
    this.dimTryTopicClustering = dimTryTopicClustering;
    this.dimTryPersonAssignment = dimTryPersonAssignment;
    this.dimTryAlloc = dimTryAlloc;
    startTask();
  }
  

  /**
   * Perform the calculation of solutions.
   * This should not be called in a GUI thread, use startSolverTask()
   * instead.
   */
  protected void doTask()
  {
  	// added update of preference index -- GS - 2005-02-22
    persons.createPrefInx();
    
/*    
    System.out.println("\n" + "Die Personen und ihre Präferenzen:");
    System.out.print("\n" + persons);

    System.out.println("\n" + "Die Personen und ihre Ranglisten je Thema:");
    System.out.println("\n" + Persons.emptyName() + "   "
                       + topics.toHeaderString("  "));
    System.out.print(persons.ranks());
    System.out.println(Persons.emptyName() + "   "
                       + topics.toHeaderString("  "));
*/
    
    // calculate attractiveness of topics
    topics.calcPrefs();
    //System.out.println("\nDie Themen, geordnet nach summierter Präferenz");
    //System.out.println("\n" + topics);

    // calculate distance matrix for each topic pair
    topics.calcDist(persons);
    //System.out.print("Die Abstandsmatrix zwischen den Themen:\n"
    //                 + topics.distToString());

    // create topic groups using a heuristic approach to minimize
    // preference conflicts for all persons;
    // make several tries
    solutionNames.removeAllElements();
    solutions.removeAllElements();
    allocate();

    Vector  done = new Vector();
    int     dimTopics = topics.getNumber();
    
    for (int tryT = 0; tryT < dimTryTopicClustering; tryT++)
    {
      setMessage("Topic Clustering Attempt " + tryT);
      
      int topicGroup[] = topics.createGroup(dimSessions, done, dimTopics
                                                               * dimTopics
                                                               * 100,
                                            tryT * 12345 + 678);
/*
       System.out.print("\nGruppierung der Themen (Versuch "
                       + tryT
                       + "):\n"
                       + topics.groupsToString(topicGroup, dimTopics
                                                           / dimSessions));
*/
      done.add(topicGroup);

      // for each clustering we try several assigments
      // of persons to topics (and roles)
      for (int tryP = 0; tryP < dimTryPersonAssignment; tryP++)
      {
        setCurrent(tryT*dimTryPersonAssignment + tryP);
        
        if (taskWasCanceled())
          return;
        
        // first the assignment is done without a specific role
        assignPersonsToSessions(topicGroup, dimTryAlloc, tryP * 4711 + 8812);
        
        // thereafter the roles are assigned
        assignRoles();

        // System.out.print(pt);
        
        if (isValidSolution())
          addSolution(createSolution(solutions.size()));
      }
    }
  }
  
  /**
   * Return true if the PTAlloc currently holds a valid solution.
   * 
   * @return true if the solution is valid, false otherwise.
   */
	public boolean isValidSolution()
	{
	  return solved;
	}

  /**
   * Allocates global data structures for the algorithm.
   * It creates the necessary arrays which are used in subsequent
   * calculations. Each calculation must be set up by a call to 
   * assignPersonsToSessions().
   */
  public void allocate()
  {
    debug            = false;
    this.solved      = false;
    
    int     dimPersons     = persons.getNumber();
    int     dimTopics      = topics.getNumber();
    int     dimRoles       = roles.getNumber();
    int     nAssignments = dimPersons * dimTopics / dimSessions;
    
    bestAssignment   = new int[dimPersons][dimTopics];
    topicRole        = new int[dimTopics][dimRoles + 1];
    personRole       = new int[dimPersons][dimRoles  + 1];
    seqPersons       = new int[nAssignments + 1][2];
    
    // Prepare a number formatter
    compactFormat = NumberFormat.getInstance();
    compactFormat.setMinimumFractionDigits(2);
    compactFormat.setMaximumFractionDigits(2);
    
    if (debug)
    {
      System.out
          .println("\nPersonen und Präferenzen bei gegebener Themengruppierung:");
      System.out.println("\n" + persons.emptyName() + ":   "
                         + topics.toHeaderString("   "));
      for (int p = 0;  p < dimPersons;  p++)
      {
        System.out.print(persons.getName(p) + ":");
        for (int t = 0;  t < dimTopics; t++)
        {
          String tmp = "    " + persons.getPreferenceIndex(p, t);
          System.out.print(tmp.substring(tmp.length() - 4));
        }
        
        System.out.println();
      }
    }
  }

  /**
   * Initialize a calculation by performing a first assignment 
   * without specific roles.
   * 
   * @param groups  the group of topics to work on.
   * @param tries   the maximum number of tries.
   * @param seed    the seed for the random number generator.
   */
  public void assignPersonsToSessions(int[] groups, int tries, long seed)
  {
    int     dimPersons     = persons.getNumber();
    int     dimTopics      = topics.getNumber();
    int     dimSessions    = getSessionNumber();
    int     unassignedRole = getRoles().getNumber() + 1;  // Marker for an unassigned role

    // Start a new solution
    solution = new Solution(this);
    
    // Remember the topic grouping in the solution
    for (int gr = 0;  gr < groups.length;  ++gr)
    {
      int groupIndex = 0;
      
      for (int t = 0;  t < dimTopics;  ++t)
        if (groups[t] == gr)
          solution.setGroupElement(gr, groupIndex++, t);
    }

    // Erase bestAssignment
    for (int person = 0;  person < dimPersons;  ++person)
      Arrays.fill(bestAssignment[person], 0);
    
    // we generate a legal assignment as a starting point;
    for (int gr = 0; gr < groups.length / dimSessions; gr++)
      for (int p = 0; p < dimPersons; p++)
      {
        int seq = 0;
        
        for (int t = 0; t < dimTopics; t++)
          if (groups[t] == gr)
          {
            if (p / (dimPersons / dimSessions) == seq)
              solution.setRole(p, t, unassignedRole);

            ++seq;
          }
      }

    int target = solution.calculateTargetValue();

    if (debug)
    {
      System.out.println("\n" + persons.emptyName() + ":  "
                         + topics.toHeaderString(" "));
      System.out.println(this + "target=" + target);
    }

    // again we apply simulated annealing; we try to swap people
    // between sessions of the same group

    // simulated annealing; we look for better solutions and
    // adopt them in most cases (80%) as a starting point for the next trial
    // sometimes we accept worse solutions and temporarily lower the
    // expectation level

    int bestTargetTotal = Integer.MAX_VALUE;
    int bestTarget = Integer.MAX_VALUE;
    int lastTarget = target;
    Random rand = new Random();
    
    if (seed != 0)
      rand = new Random(seed);

    for (int y = 0; y < tries; y++)
    {
      // modify constellation, later restore possible
      int p1 = 0, t1 = 0, p2 = 0, t2 = 0;
      p1 = rand.nextInt(dimPersons);
      t1 = rand.nextInt(dimTopics / dimSessions);
      // look for a topic the selected person is assigned to
      for (int t = 0; t < dimTopics; t++)
        if (solution.getRole(p1, t) > 0)
          if (t1-- <= 0)
          {
            t1 = t;
            break;
          }

        // pick a member of another topic of the same session
      int n = rand.nextInt(dimPersons - dimPersons / dimSessions);
      
      for (int t = 0;  t < dimTopics;  t++)
        if (groups[t] == groups[t1] && t1 != t)
          for (int p = 0;  p < dimPersons;  p++)
            if (solution.getRole(p, t) > 0 && n-- <= 0)
            {
              p2 = p;
              t2 = t;
              break;
            }
      
      solution.setRole(p1, t1, 0);
      solution.setRole(p1, t2, unassignedRole);
      solution.setRole(p2, t1, unassignedRole);
      solution.setRole(p2, t2, 0);

      // calculate target value
      lastTarget = target;
      target = solution.calculateTargetValue();

      if (debug)
      {
        System.out.println("\np1=" + p1 + " t1=" + topics.getName(t1)
                           + "  /  p2=" + p2 + " t2=" + topics.getName(t2));
        System.out.println(this + "target=" + target + "   (" + bestTarget
                           + ")  (" + bestTargetTotal + ")");
      }

      if (target < bestTargetTotal)
      {
        // check if solution is known already
        boolean known = false;
        
        if (!known)
        {
          bestTargetTotal = target;
          // store best result
          for (int p = 0; p < dimPersons; p++)
            for (int t = 0; t < dimTopics; t++)
              bestAssignment[p][t] = solution.getRole(p, t);

          if (debug)
            System.out.println("BEST ASSIGNMENT");
        }
      }
      
      if (target < bestTarget)
      {
        if (rand.nextInt(100) >= 100)
        {
          // take back
          solution.setRole(p1, t1, unassignedRole);
          solution.setRole(p1, t2, 0);
          solution.setRole(p2, t1, 0);
          solution.setRole(p2, t2, unassignedRole);
          target = lastTarget;
          
          //if (debug)
            System.out.println("better, but staying.");
        }
        else
        {
          // accept
          bestTarget = target;
          
          if (debug)
            System.out.println("better, moving.");
        }
      }
      else
      {
        if (rand.nextInt(100) >= 0)
        {
          // take back
          solution.setRole(p1, t1, unassignedRole);
          solution.setRole(p1, t2, 0);
          solution.setRole(p2, t1, 0);
          solution.setRole(p2, t2, unassignedRole);
          target = lastTarget;
          if (debug)
            System.out.println("worse, staying");
        }
        else
        {
          // accept
          bestTarget = target;
          //if (debug)
            System.out.println("worse, but moving.");
        }
      }
    }

    for (int p = 0; p < dimPersons; p++)
      for (int t = 0; t < dimTopics; t++)
        solution.setRole(p, t, bestAssignment[p][t]);

    if (debug)
    {
      target = solution.calculateTargetValue();
      System.out.println(this + "target=" + target + "  bestTargetTotal="
                         + bestTargetTotal);
    }
  }

  /**
   * Assign the roles to produce a solution.
   * After calling this method, isValidSoution() should be used
   * to check whether a valid solution has been reached.
   */
  void assignRoles()
  {
    int dimPersons     = persons.getNumber();
    int dimTopics      = topics.getNumber();
    int dimSessions    = getSessionNumber();
    int dimRoles       = getRoles().getNumber();
    int nAssignments   = dimPersons * dimTopics / dimSessions;
    int unassignedRole = dimRoles + 1; // Marker for an unassigned role

    for (int topic = 0;  topic < dimTopics;  ++topic)
      Arrays.fill(topicRole[topic], 0);

    for (int person = 0;  person < dimPersons;  ++person)
      Arrays.fill(personRole[person], 0);

    for (int as = 0;  as < nAssignments + 1;  ++as)
      Arrays.fill(seqPersons[as], 0);

    int topicLimit  = dimPersons / dimSessions / dimRoles;
    int personLimit = dimTopics / dimSessions / dimRoles;

    // for every role we repeat the same procedure:
    // we try to allocate the role according to the preference of the person
    // we perform backtracking to find a valid solution
    boolean debug = false;

    if (debug)
      System.out.println(this);

    // we define the sequence in which we want to assign roles to the persons
    // we change the sequence of persons for each step to prevent a bias
		int noStep   = 0;
		int maxSteps = 100000; // Integer.MAX_VALUE;
		solved = true;
		
    for (int n = 0; n < nAssignments; n++)
    {
      int p = n % dimPersons;
      int cycle = n / dimPersons;
      seqPersons[n][0] = (cycle % 2 == 0) ? p : dimPersons - 1 - p;
    }

    for (int n = 0; n < nAssignments; n++)
    {
			if (++noStep > maxSteps)
			{
			  solved = false;
			  break;
			}
			
      int r = (n / (nAssignments / dimRoles)) + 1;
      int p = seqPersons[n][0];
      boolean ok = false;
      int t = 0, prio;
      
      for (prio = seqPersons[n][1]; prio < dimTopics; prio++)
      {
        t = persons.getPreferenceIndex(p, prio);
        
        if (solution.getRole(p, t) == unassignedRole)
        {
          if (debug)
            System.out.print("n=" + n + " r=" + r + " p=" + p + " t=" + t);
          
          // try to assign the role
          if (topicRole[t][r] < topicLimit && personRole[p][r] < personLimit)
          {
            solution.setRole(p, t, r);
            topicRole[t][r] += 1;
            personRole[p][r] += 1;
            
            if (debug)
              System.out.println("  assigned.");
            
            ok = true;
            break;
          }
          else
          {
            ok = false;
            
            if (debug)
              System.out.println("  not possible.");
          }
        }
      }
      
      if (ok)
      { // going forward, reset minPrio for next n
        seqPersons[n][1] = prio + 1;
        seqPersons[n + 1][1] = 0;
      }
      else
      { // back tracking
        n -= 1;

        p = seqPersons[n][0];
        t = persons.getPreferenceIndex(p, seqPersons[n][1] - 1);
        solution.setRole(p, t, unassignedRole);
        topicRole[t][r] -= 1;
        personRole[p][r] -= 1;
        n -= 1;
      }
    }
    
		// System.out.println (noStep + " steps performed.");
  }

 
  /**
   * Create a Solution object from the current solution.
   * 
   * @param index this is the current number of the solution.
   *              Will be used to give the solution an initial name
   *              ("Solution &lt;index&gt;").
   * @return the new Solution object.
   */
  public Solution createSolution(int index)
  {
    if (!solved)
      return null;

    solution.evaluate();
    
    StringBuffer name = new StringBuffer("Solution " + (index+1));
    name.append(": ");
    name.append(compactFormat.format(solution.getMeanDeviation()));
    name.append(" - ");
    name.append(compactFormat.format(solution.getMaximumDeviation()));
    name.append(" - ");
    name.append(compactFormat.format(solution.getStandardDeviation()));
    solution.setName(name.toString());
    
    return solution;
  }
  
}