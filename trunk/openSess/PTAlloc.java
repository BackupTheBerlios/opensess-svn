
package openSess;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;

/*
 * Author:      Gero Scholz
 * Created:     2005-02-11 
 * Revision ID: $Id: PTAlloc.java 49 2005-03-01 13:29:32Z awi $
 * 
 * 2005-02-14/AW: Changes to decrease excessive memory allocation/deallocation.
 * 2005-02-22/GS: Algorithm bug fixes 
 */

/**
 * PTAlloc performs the assignment of roles to persons and topics.
 * A PTAlloc instance can be used to perform the calculation of several
 * solutions. The method init() is used to set up the PTAlloc for
 * a particular solution which is then calculated by calling
 * assignRoles(). If isValidSolution() returns true afterwards,
 * the solution can be retrieved via createSolution().
 * 
 * @author Gero Scholz
 */
public class PTAlloc
{
  private Solver       solver;
  private Persons      persons;
  private Topics       topics;

  private boolean      debug;
  private int          dimSessions;
  private int          groups[];
  private int          assigned[][];
  private int          bestAssignment[][];
  private int          pref[][];
  private int          topicRole[][];
  private int          personRole[][];
  private int          nAssignments;
  private int          seqPersons[][];
  private int          personSum[];
  private double       meanDeviation, maxDeviation, stdDeviation;
  private NumberFormat compactFormat;
  private boolean      solved;

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
	 * Calculates the target value of the current assignment.
	 * 
	 * @return the target value.
	 */
  protected int targetValue()
  {
		int pval[] = new int[solver.dimPersons];
    int val = 0;

    for (int p = 0; p < solver.dimPersons; p++)
    {
      for (int t = 0; t < solver.dimTopics; t++)
        if (assigned[p][t] > 0)
          pval[p] += pref[p][t];

      val += pval[p];
    }
    
    double mean = (val * 1.0 / solver.dimPersons);
    double dev = 0;
    
    for (int p = 0; p < solver.dimPersons; p++)
      dev += (mean - pval[p]) * (mean - pval[p]);

    dev = Math.sqrt(dev / solver.dimPersons);
    val += (solver.dimBalancing * dev);

    return val;
  }

  /**
   * Constructs a new PTAlloc.
   * It creates the necessary arrays which are used in subsequent
   * calculations. Each calculation must be set up by a call to init().
   *  
   * @param solver      the Solver object.
   */
  public PTAlloc(Solver solver)
  {
    debug            = false;
    this.solver      = solver;
    this.persons     = solver.getPersons();
    this.topics      = solver.getTopics();
    this.dimSessions = solver.getSessionNumber();
    this.solved      = false;
    assigned         = new int[solver.dimPersons][solver.dimTopics];
    bestAssignment   = new int[solver.dimPersons][solver.dimTopics];
    topicRole        = new int[solver.dimTopics][solver.getRoles().getNumber() + 1];
    personRole       = new int[solver.dimPersons][solver.getRoles().getNumber()  + 1];
    // seqPersons[][0] = person, seqPerson[][1]=minPrio
    nAssignments     = solver.dimPersons * solver.dimTopics / dimSessions;
    seqPersons       = new int[nAssignments + 1][2];
    personSum        = new int[solver.dimPersons];
    // Prepare a number formatter
    compactFormat = NumberFormat.getInstance();
    compactFormat.setMinimumFractionDigits(2);
    compactFormat.setMaximumFractionDigits(2);
    
    // we calculate the preference for each person and topic under the
    // assumption of the given grouping

    pref = new int[solver.dimPersons][solver.dimTopics];

    for (int p = 0; p < solver.dimPersons; p++)
      for (int t = 0; t < solver.dimTopics; t++)
      	// changed pref to prefInx -- GS - 2005-02-22
        pref[p][t] = solver.getPersons().prefInx[p][t];
    
    if (debug)
    {
      System.out
          .println("\nPersonen und Präferenzen bei gegebener Themengruppierung:");
      System.out.println("\n" + persons.emptyName() + ":   "
                         + topics.toHeaderString("   "));
      for (int p = 0; p < solver.dimPersons; p++)
      {
        System.out.print(persons.getName(p) + ":");
        for (int t = 0; t < solver.dimTopics; t++)
        {
          String tmp = "    " + pref[p][t];
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
  public void init(int[] groups, int tries, long seed)
  {
    for (int person = 0;  person < solver.dimPersons;  ++person)
    {
      Arrays.fill(assigned[person], 0);  
      Arrays.fill(bestAssignment[person], 0);
    }
    
    // we generate a legal assignment as a starting point;
    this.groups = groups;
    
    for (int gr = 0; gr < groups.length / dimSessions; gr++)
      for (int p = 0; p < solver.dimPersons; p++)
      {
        int seq = 0;
        
        for (int t = 0; t < solver.dimTopics; t++)
          if (groups[t] == gr)
          {
            if (p / (solver.dimPersons / dimSessions) == seq)
              assigned[p][t] = 9;

            ++seq;
          }
      }

    int target = targetValue();

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

    int bestTargetTotal = Integer.MAX_VALUE, bestTarget = Integer.MAX_VALUE, lastTarget = target;
    Random rand = new Random();
    
    if (seed != 0)
      rand = new Random(seed);

    for (int y = 0; y < tries; y++)
    {
      // modify constellation, later restore possible
      int p1 = 0, t1 = 0, p2 = 0, t2 = 0;
      p1 = rand.nextInt(solver.dimPersons);
      t1 = rand.nextInt(solver.dimTopics / dimSessions);
      // look for a topic the selected person is assigned to
      for (int t = 0; t < solver.dimTopics; t++)
        if (assigned[p1][t] > 0)
        {
          if (t1 <= 0)
          {
            t1 = t;
            break;
          }
          
          --t1;
        }

        // pick a member of another topic of the same session
      int n = rand.nextInt(solver.dimPersons - solver.dimPersons / dimSessions);
      
      for (int t = 0; t < solver.dimTopics; t++)
        if (groups[t] == groups[t1] && t1 != t)
          for (int p = 0; p < solver.dimPersons; p++)
          {
            if (assigned[p][t] > 0 && n <= 0)
            {
              p2 = p;
              t2 = t;
              break;
            }
            
            --n;
          }
      
      assigned[p1][t1] = 0;
      assigned[p1][t2] = 9;
      assigned[p2][t1] = 9;
      assigned[p2][t2] = 0;

      // calculate target value
      lastTarget = target;
      target = targetValue();
      // pref[p1][t1]-pref[p1][t2]+pref[p2][t2]-pref[p2][t1]);

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
          for (int p = 0; p < solver.dimPersons; p++)
            for (int t = 0; t < solver.dimTopics; t++)
              bestAssignment[p][t] = assigned[p][t];

          if (debug)
            System.out.println("BEST ASSIGNMENT");
        }
      }
      
      if (target < bestTarget)
      {
        if (rand.nextInt(100) >= 100)
        {
          // take back
          assigned[p1][t1] = 9;
          assigned[p1][t2] = 0;
          assigned[p2][t1] = 0;
          assigned[p2][t2] = 9;
          target = lastTarget;
          
          if (debug)
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
          assigned[p1][t1] = 9;
          assigned[p1][t2] = 0;
          assigned[p2][t1] = 0;
          assigned[p2][t2] = 9;
          target = lastTarget;
          if (debug)
            System.out.println("worse, staying");
        }
        else
        {
          // accept
          bestTarget = target;
          if (debug)
            System.out.println("worse, but moving.");
        }
      }
    }

    for (int p = 0; p < solver.dimPersons; p++)
      for (int t = 0; t < solver.dimTopics; t++)
        assigned[p][t] = bestAssignment[p][t];

    if (debug)
    {
      target = targetValue();
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
    int dimRoles = solver.getRoles().getNumber();

    for (int topic = 0;  topic < solver.dimTopics;  ++topic)
      Arrays.fill(topicRole[topic], 0);

    for (int person = 0;  person < solver.dimPersons;  ++person)
      Arrays.fill(personRole[person], 0);

    for (int as = 0;  as < nAssignments + 1;  ++as)
      Arrays.fill(seqPersons[as], 0);

    int topicLimit = solver.dimPersons / dimSessions / dimRoles;
    int personLimit = solver.dimTopics / dimSessions / dimRoles;

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
      int p = n % solver.dimPersons;
      int cycle = n / solver.dimPersons;
      
      if (cycle % 2 == 0)
        seqPersons[n][0] = p;
      else
        seqPersons[n][0] = solver.dimPersons - 1 - p;
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
      
      for (prio = seqPersons[n][1]; prio < solver.dimTopics; prio++)
      {
        t = persons.prefInx[p][prio];
        
        if (assigned[p][t] == 9)
        {
          if (debug)
            System.out.print("n=" + n + " r=" + r + " p=" + p + " t=" + t);
          
          // try to assign the role
          if (topicRole[t][r] < topicLimit && personRole[p][r] < personLimit)
          {
            assigned[p][t] = r;
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
        t = persons.prefInx[p][seqPersons[n][1] - 1];
        assigned[p][t] = 9;
        topicRole[t][r] -= 1;
        personRole[p][r] -= 1;
        n -= 1;
      }
    }
    
		// System.out.println (noStep + " steps performed.");
		doStatistics();
  }

  /**
   * Evaluate the solution and set the statistical values.
   */
  protected void doStatistics()
  {
    float  total = 0;
    int    sumMax = 0;
    int    pval[] = new int[solver.dimPersons];
    int    val = 0;
    
    for (int p = 0; p < solver.dimPersons; p++)
    {
      for (int t = 0; t < solver.dimTopics; t++)
        if (assigned[p][t] > 0)
          pval[p] += pref[p][t];
      
      val += pval[p];
    }
    
    double mean = (val * 1.0 / solver.dimPersons);
    double dev = 0;
    
    for (int p = 0;  p < solver.dimPersons;  p++)
      dev += (mean - pval[p]) * (mean - pval[p]);
    
    dev = Math.sqrt(dev / solver.dimPersons);
         
    for (int p = 0; p < solver.dimPersons; p++)
    {
      int sum = -(solver.dimTopics / dimSessions)
                * ((solver.dimTopics / dimSessions) - 1) / 2;
      
      for (int t = 0; t < solver.dimTopics; t++)
        if (assigned[p][t] > 0)
          sum += pref[p][t]; // *pref[p][t])
      
      personSum[p] = sum;
      
      total += sum;
      
      if (sumMax < sum)
        sumMax = sum;
    }

    meanDeviation = total / solver.dimPersons;
    maxDeviation = sumMax;  
    stdDeviation = Math.sqrt(dev);
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
    
    Solution s = new Solution(solver);

    for (int gr = 0;  gr < groups.length;  ++gr)
    {
      int groupIndex = 0;
      
      for (int t = 0;  t < solver.dimTopics;  ++t)
        if (groups[t] == gr)
          s.setGroupElement(gr, groupIndex++, t);
    }
    
    for (int p = 0; p < solver.dimPersons; p++)
    {
      for (int t = 0; t < solver.dimTopics; t++)
        s.setRole(p, t, assigned[p][t]);
      
      s.setPersonSum(p, personSum[p]);
    }

    s.setStatistics(meanDeviation, maxDeviation, stdDeviation, targetValue());

    StringBuffer name = new StringBuffer("Solution " + (index+1));
    name.append(": ");
    name.append(compactFormat.format(s.getMeanDeviation()));
    name.append(" - ");
    name.append(compactFormat.format(s.getMaximumDeviation()));
    name.append(" - ");
    name.append(compactFormat.format(s.getStandardDeviation()));
    s.setName(name.toString());
    
    return s;
  }
  
  public String toString()
  {
		if (!solved) 
		  return "suppressing output of unsolved matrix ...\n";
    
    // return the final allocation matrix
    StringBuffer s = new StringBuffer(persons.emptyName() + "   " + topics.toHeaderString(" ") + "\n");
         
    for (int p = 0; p < solver.dimPersons; p++)
    {
      s.append(persons.getName(p) + "  ");

      for (int t = 0; t < solver.dimTopics; t++)
        s.append(" " + solver.getRoles().getNameExtended(assigned[p][t]));

      String tmp = "       " + personSum[p];

      s.append(tmp.substring(tmp.length() - 5) + "\n");
    }
    
    s.append(persons.emptyName() + "   " + topics.toHeaderString(" ") + "\n");
    s.append("    mittlere Abweichung=" + meanDeviation
         + "    maximale Abweichung=" + maxDeviation  
         + "    Standardabw.=" + stdDeviation
         + "    target=" + targetValue()
         + "\n\n");
    
    return s.toString();
  }
}