
package openSess;
import java.io.PrintWriter;
import java.util.Random;

import javax.swing.DefaultListModel;

/*
 * Copyright 2005 Gero Scholz, Andreas Wickner
 * 
 * Created:     2005-02-11 
 * Revision ID: $Id: Persons.java 48 2005-03-01 11:12:27Z awi $
 * 
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

/**
 * Persons maintains a list of person names and their data.
 * 
 * @author Gero Scholz
 */
public class Persons
  implements XMLStateSaving
{
  private Solver           solver;
  private DefaultListModel names;
  private static int       nameLen = 30;
  
  public int              pref[][];    /** the topics ordered by preference */
  public int              prefInx[][]; /** the rank of the preference for each topic */

  /**
   * Constructs a new Persons object. Uses the configuration data in a
   * Solver object.
   * 
   * @param solver the Solver object.
   */
  public Persons(Solver solver)
  {
    this.solver = solver;

    // store the names
    names = new DefaultListModel();
    
    for (int p = 0; p < solver.dimPersons; p++)
      names.addElement("Person " + (p+1));

    // create an initial preference structure
    pref = new int[solver.dimPersons][solver.dimTopics];
    prefInx = new int[solver.dimPersons][solver.dimTopics];
    for (int p = 0; p < solver.dimPersons; p++)
    {
      for (int t = 0; t < solver.dimTopics; t++)
      {
        prefInx[p][t] = pref[p][t] = t;
      }
    }
  }

  /**
   * Returns the number of persons.
   * 
   * @return the number of persons.
   */
  public int getNumber()
  {
    return names.getSize();
  }
  
  /**
   * Returns all person names as a DefaultListModel.
   * 
   * @return a list of all person names.
   */
  public DefaultListModel getNames()
  {
    return names;
  }
  
  /**
   * Returns the name of the person with a given index.
   * 
   * @param index the index of the person
   * @return the name of the person.
   */
  public String getName(int index)
  {
    return (String)names.getElementAt(index);
  }
  
  /**
   * For the indicated person, get the topic number at the specified index
   * in the preferences list.
   *  
   * @param person the person index.
   * @param index  the index into the preferences list.
   * @return the topic number at the specified index.
   */
  public int getPreference(int person, int index)
  {
    return pref[person][index];
  }
  
  /**
   * For the indicated person, set the topic number at the specified index
   * in the preferences list.
   *  
   * @param person the person index.
   * @param index  the index into the preferences list.
   * @param topic the new topic number.
   */
  public void setPreference(int person, int index, int topic)
  {
    pref[person][index] = topic;  
  }
  
  /**
   * For the indicated person, swap the topics at indeces "first" and "second"
   * in the preferences list.
   * 
   * @param person the person number.
   * @param first  the first index to swap.
   * @param second the second index to swap.
   */
  public void swapPreferences(int person, int first, int second)
  {
    int tmp = getPreference(person, first);
    setPreference(person, first, getPreference(person, second));
    setPreference(person, second, tmp);
  }
  
  /**
   * Set the name of a person.
   * 
   * @param person the person number.
   * @param name   the new name.
   */
  protected void setName(int person, String name)
  {
    // set a user´s name
    names.setElementAt(name, person);
  }

  /**
   * Shuffle the preferences list by swapping randomly selected
   * topics in the list. 
   * 
   * @param rand    a random number generator.
   * @param shuffle the number of swaps to be performed.
   */
  public void setRandomPrefs(Random rand, int shuffle)
  {
    // assign random preferences by randomly swapping pairs of
    // the initial preference structure
    for (int p = 0;  p < solver.dimPersons;  p++)
    {
      for (int i = 0;  i < shuffle;  i++)	
      {
        int j = rand.nextInt(solver.dimTopics);
        int k = rand.nextInt(solver.dimTopics);
        int tmp = pref[p][j];
        pref[p][j] = pref[p][k];
        pref[p][k] = tmp;
      }
    }
    createPrefInx();
  }

  /**
   * Create an inverted preference list containing the the rank of each topic.
   */
  protected void createPrefInx()
  {
    for (int p = 0; p < solver.dimPersons; p++)
      for (int t = 0; t < solver.dimTopics; t++)
        prefInx[p][pref[p][t]] = t;
  }

  /**
   * Return persons and the ranks of their topic preferences.
   * 
   * @return persons and the ranks of their topic preferences.
   */
  public String ranks()
  {
    String s = new String();
    for (int p = 0; p < solver.dimPersons; p++)
    {
      s += getName(p) + ":";
      for (int t = 0; t < solver.dimTopics; t++)
      {
        String tmp = "   " + (pref[p][t] + 1);
        s += tmp.substring(tmp.length() - 3);
      }
      s += "\n";
    }
    return s;
  }

  /**
   * Return a String consisting of spaces. The length of the String
   * is equal to the length of the longest person name.
   * 
   * @return a String of spaces as long as the longest person name.
   */
  public String emptyName()
  {
    int maxLength = 0;
    
    for (int p = 0;  p < getNumber();  ++p)
      if (getName(p).length() > maxLength)
        maxLength = getName(p).length();
      
    StringBuffer s = new StringBuffer();
    
    for (int i = 0;  i < maxLength;  ++i)
      s.append(' ');
    
    return s.toString();
  }
  
  /**
   * Returns persons and their preference order.
   */
  public String toString()
  {
    String s = new String();
    for (int p = 0; p < solver.dimPersons; p++)
    {
      s += getName(p) + ":  ";
      for (int t = 0; t < solver.dimTopics; t++)
      {
      	// changed prefInx to pref -- GS - 2005-02-22
        s += solver.getTopics().getName(pref[p][t]);
        if (t < solver.dimTopics - 1)
          s += "__";
      }
      s += "\n";
    }
    return s;
  }

  /**
   * Save the Persons data in XML form.
   */
  public void save(PrintWriter stream, int level)
  {
    Indenter.println(stream, level, "<persons>");

    for (int p = 0;  p < getNumber();  ++p)
    {
      Indenter.println(stream, level+1, "<person name=\"" + getName(p) + "\">");
      
      for (int t = 0;  t < solver.getTopics().getNumber();  ++t)
        Indenter.println(stream, level+2, "<preferredTopic index=\"" + pref[p][t] + "\"/>");
      
      Indenter.println(stream, level+1, "</person>");
    }
    
    Indenter.println(stream, level, "</persons>");
  }
}