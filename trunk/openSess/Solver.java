/*
 * Copyright 2005 Gero Scholz, Andreas Wickner
 * 
 * Created:     2005-02-11 
 * Revision ID: $Id: Solver.java 48 2005-03-01 11:12:27Z awi $
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
import java.util.Vector;

import javax.swing.DefaultListModel;

/**
 * This is the central class of the solution algorithm.
 * It maintains the dimensions of the various object lists
 * as well as the object lists themselves. It provides
 * the solution algorithm and analysis functions.
 * 
 * TODO: Maybe the class is a bit too central and should be
 *       restructured. Also, the separation of concerns between
 *       Solver and PTAlloc is not quite clear (andreas).
 *  
 * @author Gero Scholz
 */
public class Solver
{
  int                      dimPersons;
  int                      dimRoles;
  int                      dimTopics;
  private int              dimSessions;
  private int              dimShuffle   = 100;  // the greater the more randomly
                                                // preferences will
                                                // be distributed.
                                                // should be 3 * dimTopics or more, values < 3 will lead
                                                // to pathologic / trivial distributions
  int                      dimBalancing = 20;   // weight of a component in the
                                                // target function which
                                                // favors evenly distributed solutions; try values between
                                                // 0 and 100 (or greater); large values will usually tend
                                                // to make the overall result worse

  private Topics           topics;
  private Persons          persons;
  private Roles            roles;
  private int              current      = 0;
  private boolean          taskDone     = false;
  private boolean          taskCanceled = false;
  private String           statMessage;
  private Vector           solutions;
  private DefaultListModel solutionNames;
  
  /*
   * currently there is a tendency to find an ideal solution for some persons
   * whereas others are far less well treated. Maybe a change in the target
   * function of the allocation process could change this.
   */

  /*
  public static void main(String[] args)
  {
    System.out.println("\nOpenSess allocation solver 0.1 by Gero Scholz");
    new Solver().solve(2, 3, 100000);
  }
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
    this.dimTopics   = dimTopics;
    this.dimPersons  = dimPersons;
    this.dimRoles    = dimRoles;
    this.dimSessions = dimSessions;

    topics        = new Topics(this);
    persons       = new Persons(this);
    roles         = new Roles(this);
    solutions     = new Vector();
    solutionNames = new DefaultListModel();
    
    //persons.setRandomPrefs(new Random(4711),dimShuffle);
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
   * This is neccessary in order not to block the GUI thread,
   * however I do not know why this has to be so complicated.
   * All this impressive handwaving was copied from Javasoft 
   * example code...
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
    final int clusterings = dimTryTopicClustering;
    final int assignments = dimTryPersonAssignment;
    final int allocs = dimTryAlloc;
    
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        current = 0;
        taskDone = false;
        taskCanceled = false;
        statMessage = null;
        return new SolverTask(theSolverItself, clusterings, assignments, allocs); 
      }
    };
    
    worker.start();
  }
  
  /**
   * SolverTask wraps the actual work to do in a way that is
   * compatible with the SwingWorker.
   * 
   * @author andreas
   */
  private class SolverTask
  {
    /**
     * The constructor contains the actual work to do
     * (which is our solution calculation).
     * 
     * @param solver the Solver object.
     * @param dimTryTopicClustering  the number of topic clusterings to try.
     * @param dimTryPersonAssignment the number of topic/person assignments to try.
     * @param dimTryAlloc            the maximum number of assignments to try.
     */
    public SolverTask(Solver solver, int dimTryTopicClustering, 
                      int dimTryPersonAssignment,
                      int dimTryAlloc)
    {
      solver.solve(dimTryTopicClustering, dimTryPersonAssignment, dimTryAlloc);
    }
  };
  
  /**
   * Return the current progress (which is in the range
   * 0 to (dimTryTopicClustering*dimTryPersonAssignment)).
   * 
   * @return the current progress.
   */
  public int getCurrent()
  {
    return current;
  }

  /**
   * Can be used to cancel the task from the outside.
   */
  public void stop()
  {
    taskCanceled = true;
    statMessage = null;
  }

  /**
   * Returns whether the task has completed.
   * 
   * @return true if completed, false otherwise.
   */
  public boolean isDone()
  {
    return taskDone;
  }

  /**
   * Returns the most recent status message, or null
   * if there is no current status message.
   * 
   * @return a status message.
   */
  public String getMessage()
  {
    return statMessage;
  }

  /**
   * Perform the calculation of solutions.
   * This should not be called in a GUI thread, use startSolverTask()
   * instead.
   * 
   * @param dimTryTopicClustering  the number of topic clusterings to try.
   * @param dimTryPersonAssignment the number of topic/person assignments to try.
   * @param dimTryAlloc            the maximum number of assignments to try.
   */
  public void solve(int dimTryTopicClustering, int dimTryPersonAssignment,
                    int dimTryAlloc)
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
    Vector  done = new Vector();
    PTAlloc pt   = new PTAlloc(this);

    for (int tryT = 0; tryT < dimTryTopicClustering; tryT++)
    {
      statMessage = "Topic Clustering Attempt " + tryT;
      
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
        current = tryT*dimTryPersonAssignment + tryP;
        
        if (taskCanceled)
          return;
        
        // first the assignment is done without a specific role
        pt.init(topicGroup, dimTryAlloc, tryP * 4711 + 8812);
        
        // thereafter the roles are assigned
        pt.assignRoles();

        // System.out.print(pt);
        
        if (pt.isValidSolution())
          addSolution(pt.createSolution(solutions.size()));
      }
    }
    
    taskDone = true;
  }
}