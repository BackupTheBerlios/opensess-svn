
package openSess;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.swing.DefaultListModel;

/*
 * Copyright 2005 Gero Scholz, Andreas Wickner
 * 
 * Created:     2005-02-11 
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
 * Topics maintains a list of all topic names and related data.
 * 
 * @author Gero Scholz
 */
public class Topics
  implements XMLStateSaving
{
  private Solver            solver;
  private DefaultListModel  names;
  private int               pref[];
  private int               rank[];
  private int               rankInx[];
  private int               dist[][];
  private int               dimPersons;

  /**
   * Creates a new Topics object with configuration data from a Solver object.
   * 
   * @param solver  a Solver object.
   */
  public Topics(Solver solver)
  {
    // create topic list
    this.solver = solver;
    names = new DefaultListModel();

    for (int t = 0; t < solver.dimTopics; t++)
      names.addElement("Topic " + (t+1));

    dist = new int[solver.dimTopics][solver.dimTopics];
  }

  /**
   * Return the number of topics.
   * 
   * @return the number of topics.
   */
  public int getNumber()
  {
    return names.getSize();
  }

  /**
   * Return the list of topic names as a DefaultList Model.
   * 
   * @return the list of topic names.
   */
  public DefaultListModel getNames()
  {
    return names;
  }
  
  /**
   * Return the name of a topic with a specified index.
   * 
   * @param topic the index of a topic
   * @return the name of the topic.
   */
  public String getName(int topic)
  {
    return (String) names.getElementAt(topic);
  }

  /**
   * Set the name of a topic with a specified index.
   * 
   * @param topic the index of a topic
   * @param name the new name of the topic.
   */
  public Topics setName(int topic, String name)
  {
    names.setElementAt(name, topic);
    return this;
  }

  /**
   * Summarize the preferences of all persons for each topic.
   */
  protected void calcPrefs()
  {
    boolean debug = false;
    pref = new int[solver.dimTopics];
    for (int t = 0; t < solver.dimTopics; t++)
    {
      pref[t] = 0;
      for (int p = 0; p < solver.dimPersons; p++)
      {
        pref[t] += solver.getPersons().pref[p][t];
        if (debug)
          System.out.println("Thema " + getName(t) + " pref+="
                             + solver.getPersons().pref[p][t]);
      }
    }

    // calculate the total rank of each topic
    // so that we can show topics ordered by rank if desired
    rank = new int[solver.dimTopics];
    rankInx = new int[solver.dimTopics];

    int prefH[] = (int[]) pref.clone();
    for (int t = 0; t < solver.dimTopics; t++)
    {
      int min = Integer.MAX_VALUE;
      int tmin = 0;
      for (int tt = 0; tt < solver.dimTopics; tt++)
      {
        if (prefH[tt] < min)
        {
          min = prefH[tt];
          tmin = tt;
          rank[tt] = t;
          if (debug)
            System.out.println("rank " + tt + ", value " + min);
        }
      }
      rankInx[t] = tmin;
      prefH[tmin] = Integer.MAX_VALUE;
      if (debug)
        System.out.println("RANK " + tmin + ", value " + rank[tmin] + "prefH="
                           + prefH[tmin]);
    }
  }

  /**
   * Calculate the distance for each pair of topics (symmetric matrix).
   * To emphasize differences we sum the square of the difference in ranking
   * for each person.
   * 
   * @param persons the Persons object.
   */
  protected void calcDist(Persons persons)
  {
    for (int t = 0; t < solver.dimTopics; t++)
    {
      for (int tt = 0; tt < solver.dimTopics; tt++)
      {
        if (t == tt)
          dist[t][tt] = 0;
        else if (t > tt)
          continue;
        else
        {
          int sum = 0, dif;
          for (int p = 0; p < solver.dimPersons; p++)
          {
            dif = persons.pref[p][t] - persons.pref[p][tt];
            sum += dif * dif;
          }
          dist[tt][t] = dist[t][tt] = sum;
        }
      }
    }
  }

  /**
   * Calculate a simple hash value for a clustering vector.
   * See createGroup(), where this is used to identify solutions which
   * have been investigated earlier.
   * 
   * @param vec a clustering vector.
   * @return the hash value.
   */
  protected int hashSum(int vec[])
  {
    int hashVal = 0;
    
    for (int n = 0; n < vec.length; n++)
      hashVal += (n + 1) * (solver.dimTopics) * (vec[n] + 1) * (vec[n] + 1);

    return hashVal;
  }

  /**
   * Create a topic cluster.
   * 
   * @param groupSize the size of the cluster to create.
   * @param forbidden a list of previously created clusters.
   * @param tries the maximum number of tries.
   * @param seed the seed for the random number generator.
   * @return a new topic cluster.
   */
  protected int[] createGroup(int groupSize, Vector forbidden, int tries,
                              long seed)
  {
    // clustering topics
    boolean debug = false;

    // we get a vector with solutions which have been investigated before
    // we do not want to reproduce these solutions and will identify
    // them by a hash value (and skip them) later during the search process
    int[] oldVecHashSum = new int[forbidden.size()];
    Iterator iter = forbidden.iterator();
    int oldVec[];
    int f = 0, hashVal;
    while (iter.hasNext())
    {
      oldVec = (int[]) iter.next();
      oldVecHashSum[f] = hashVal = hashSum(oldVec);
      // System.out.println("hashval("+f+")="+hashVal);
      f += 1;
    }

    // we work on an array which contains a "group" number for each topic
    // topics with identical group numbers run in parallel sessions.
    // only one topic of each group may be assigned to a person
    // it is useful to "normalize" the grouping, i.e. starting with group
    // 0,1,2, ..

    // we start with a simple initial grouping
    int n = solver.dimTopics;
    int vec[] = new int[n];
    int bestVec[] = new int[n];
    for (int i = 0; i < n; i++)
    {
      vec[i] = bestVec[i] = i / groupSize;
    }

    Random rand = new Random();
    if (seed != 0)
      rand = new Random(seed);

    // simulated annealing; we look for better solutions and
    // adopt them in most cases (80%) as a starting point for the next trial
    // sometimes we accept worse solutions and temporarily lower the
    // expectation level

    int sep, bestSepTotal = 0;
    int bestSep = 0; // this is the current expectation level ("flood level")

    for (int t = 0; t < tries; t++)
    {

      // we swap two topics by random, creating a new, normalized grouping
      int a = 0, b = 1, x;
      for (int z = 0; z < 10; z++)
      {
        a = rand.nextInt(n);
        b = rand.nextInt(n);
        if (vec[a] != vec[b])
          break;
      }
      x = vec[a];
      vec[a] = vec[b];
      vec[b] = x;
      normalize(vec);

      // we calculate the new separation value
      sep = separation(vec, groupSize);

      if (debug)
      {
        System.out.print("Vector= ");
        for (int i = 0; i < n; i++)
          System.out.print(vec[i] + " ");
        System.out.println("sep=" + sep + "   bestSep=" + bestSep);
      }

      // if it is better than all other values and if the solution
      // is a new one, we store the value and the solution
      if (sep > bestSepTotal)
      {
        int thisHashSum = hashSum(vec);
        boolean known = false;
        for (int i = 0; i < oldVecHashSum.length; i++)
        {
          if (known = (oldVecHashSum[i] == thisHashSum))
            break;
        }
        if (!known)
        {
          bestSepTotal = sep;
          bestVec = (int[]) vec.clone();
        }
      }

      // now we compare with the current threshold ("bestSep")
      if (sep > bestSep)
      {
        if (rand.nextInt(100) >= 90)
        {
          // disregard improved result
          x = vec[b];
          vec[b] = vec[a];
          vec[a] = x;
          if (debug)
            System.out.println("better, but staying.");
        }
        else
        {
          // accept improved result
          bestSep = sep;
          if (debug)
            System.out.println("better, moving.");
        }
      }
      else
      {
        // disregard result as it is worse
        if (rand.nextInt(100) >= 10)
        {
          x = vec[b];
          vec[b] = vec[a];
          vec[a] = x;
          if (debug)
            System.out.println("worse, staying");
        }
        else
        {
          // accept result although it is worse
          bestSep = sep;
          if (debug)
            System.out.println("worse, but moving.");
        }
      }
    }

    if (debug)
    {
      System.out.print("bestVecTotal= ");
      for (int i = 0; i < n; i++)
        System.out.print(bestVec[i] + " ");
      System.out.println("bestSepTotal=" + bestSepTotal);
    }
    return bestVec;
  }

  /**
   * Normalize the target sequence (i.e. lowest group numbers first).
   * 
   * @param vec the target sequence.
   */
  protected void normalize(int vec[])
  {
    int gr = 0;
    for (int t = 0; t < solver.dimTopics; t++)
    {
      if (vec[t] >= 100)
        continue;
      int vOld = vec[t];
      vec[t] = 100 + gr;
      for (int tt = t + 1; tt < solver.dimTopics; tt++)
      {
        if (vec[tt] == vOld)
          vec[tt] = 100 + gr;
      }
      gr += 1;
    }
    for (int t = 0; t < solver.dimTopics; t++)
      vec[t] -= 100;
  }

  /**
   * The separation value is the sum of the squared preference differences
   * for all persons if they had to choose between all topics of a group.
   * This value is calculated for each group and summed up.
   * The greater the average distance between the members of a group
   * the better the separation value will be.
   * one could restrict this to a difference between the top preference
   * within each group and the remaining members - but this would assume
   * that it will always be possible to assign a person to its
   * top-preference-topic - which clearly cannot be guaranteed for all persons.
   * We build a reference to the index of the group members.
   * Example: from 00123123 we would produce 01 25 36 47
   * 
   * @param vec TODO
   * @param gSize  the size of each group.
   * @return the separation value.
   */
  protected int separation(int[] vec, int gSize)
  {
    int gCount = vec.length / gSize;
    int grinx[] = new int[vec.length];
    int n = 0;
    for (int gr = 0; gr < gCount; gr++)
    {
      for (int i = 0; i < vec.length; i++)
      {
        if (vec[i] == gr)
        {
          grinx[n] = i;
          n += 1;
        }
      }
    }

    // System.out.println("VecInx= ");
    // for (int g=0;g<grinx.length;g++) System.out.print(grinx[g]+" ");
    // System.out.println();

    int sep = 0;
    for (int gr = 0; gr < gCount; gr++)
    {
      int start = gr * gSize;
      int gsep = 0;
      for (int g = 0; g < gSize - 1; g++)
      {
        for (int gg = g + 1; gg < gSize; gg++)
        {
          // System.out.println("group "+gr+":
          // "+dist[grinx[start+g]][grinx[start+gg]]);
          gsep += dist[grinx[start + g]][grinx[start + gg]];
        }
      }
      sep += (gsep * gsep);
    }
    return sep;
  }

  /**
   * Return the matrix of topic distances.
   * 
   * @return the matrix of topic distances.
   */
  protected String distToString()
  {
    String s = new String();
    String field;
    s += "      " + toHeaderString("    ") + "\n";
    for (int t = 0; t < solver.dimTopics; t++)
    {
      s += " " + getName(t);
      for (int tt = 0; tt < solver.dimTopics; tt++)
      {
        field = "     " + dist[t][tt];
        s += field.substring(field.length() - 5);
      }
      s += "\n";
    }
    return s;
  }

  /**
   * Return a headline with topic names.
   * 
   * @param space a String used to separate the topic names.
   * @return a headline with topic names.
   */
  public String toHeaderString(String space)
  {
    StringBuffer s = new StringBuffer();
    
    for (int t = 0; t < solver.dimTopics; t++)
      s.append(getName(t) + space);
    
    return s.toString();
  }

  /**
   * Return a textual list of topic groupings.
   * 
   * @param groups  a vector giving the group index for each topic.
   * @param groupsCount the number of groups.
   * @return a textual list of topic groupings.
   */
  public String groupsToString(int groups[], int groupsCount)
  {
    String s = new String();
    for (int gr = 0; gr < groupsCount; gr++)
    {
      for (int t = 0; t < solver.dimTopics; t++)
      {
        if (groups[t] != gr)
          continue;
        s += getName(t) + " ";
      }
      s += "\n";
    }
    s += "============================ "
         + separation(groups, groups.length / groupsCount) + "\n";
    return s;
  }

  /**
   * Return a textual list of all topics, ordered by rank.
   */
  public String toString()
  {
    String s = new String();
    for (int t = 0; t < solver.dimTopics; t++)
    {
      int tt = rankInx[t];
      s += "      " + getName(tt) + " " + (pref[tt] + dimPersons) + "\n";
    }
    return s;
  }
  
  /**
   * Produce an XML representation of the Topics object.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<topics>");

    for (int t = 0;  t < getNumber();  ++t)
      Indenter.println(stream, level+1, "<topic name=\"" + getName(t) + "\"/>");
    
    Indenter.println(stream, level, "</topics>");
  }
}