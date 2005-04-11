package openSess;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     21.02.2005
 * Revision ID: $Id: SolverConstructor.java 10 2005-03-04 18:45:41Z awickner $
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
 * This is an implementation of the SAX DefaultHandler which reconstructs
 * the state of the MainWindow/Solver from an XML file.
 * 
 * @author andreas
 */
public class SolverConstructor
  extends DefaultHandler
{
  private MainWindow main;
  private Solver     solver;
  private int        topic      = 0;
  private int        person     = 0;
  private int        role       = 0;
  private int        solution   = 0;
  private int        group      = 0;
  private int        groupIndex = 0;
  private Solution   currentSolution;
  
  /**
   * Constructs the contents of the given MainWindow.
   * 
   * @param main the MainWindow.
   */
  public SolverConstructor(MainWindow main)
  {
    this.main = main;
  }
  
  /**
   * Gets called by the SAX implementation when the start of an XML tag
   * is recognized. 
   */
  public void startElement(String uri, String localName, String qName, 
                           Attributes attributes) 
  {
    if (qName.equals("openconclave"))
    {
      int topicNumber   = getInt(attributes, "topics", 12);
      int personNumber  = getInt(attributes, "persons", 12);
      int roleNumber    = getInt(attributes, "roles", 3);
      int sessionNumber = getInt(attributes, "sessions", 2);
      
      main.reconfigure(topicNumber, personNumber, roleNumber, sessionNumber);
      solver = main.getSolver();
    }
    else if (qName.equals("topics"))
      topic = -1;
    else if (qName.equals("topic"))
      solver.getTopics().setName(++topic, getString(attributes, "name", topic));
    else if (qName.equals("persons"))
      person = -1;
    else if (qName.equals("person"))
    {
      solver.getPersons().setName(++person, getString(attributes, "name", person));
      topic = -1;
    }
    else if (qName.equals("roles"))
      role = -1;
    else if (qName.equals("role"))
    {
      Roles roles = solver.getRoles();
      roles.setName(++role, getString(attributes, "name", role));
      int minmax = solver.getPersons().getNumber()
      				/	solver.getSessionNumber()
      				/ solver.getRoles().getNumber();
      roles.setMinimumPerSession(role, getInt(attributes, "min", minmax));
      roles.setMaximumPerSession(role, getInt(attributes, "max", minmax));
    }
    else if (qName.equals("preferredTopic"))
      solver.getPersons().setPreference(person, ++topic, getInt(attributes, "index", 0));
    else if (qName.equals("solutionParameters"))
    {
      int keepBest = getInt(attributes, "keepBest", 10);
      solver.setKeepBest(keepBest);
      main.setSolutionParameters(getInt(attributes, "topicClusters",     5),
                                 getInt(attributes, "personAssignments", 5),
                                 getInt(attributes, "attempts",          100000),
                                 keepBest);
    }
    else if (qName.equals("solutions"))
    {
      solution = 0;
      solver.getPersons().createPreferenceIndex();
    }
    else if (qName.equals("solution"))
    {
      currentSolution = new Solution(solver);
      currentSolution.setName(getString(attributes, "name", solution));
      main.getSolver().addSolution(currentSolution);
    }
/*
    else if (qName.equals("statistics"))
      currentSolution.setStatistics(getDouble(attributes, "meandev", 0.0),
                                    getDouble(attributes, "maxdev",  0.0),
                                    getDouble(attributes, "stddev",  0.0),
                                    getInt(attributes, "target",     0));
*/                                    
    else if (qName.equals("topicGroups"))
      group = -1;
    else if (qName.equals("topicGroup"))
    {
      ++group;
      groupIndex = -1;
    }
    else if (qName.equals("groupTopic"))
      currentSolution.setGroupElement(group, ++groupIndex, 
                                      getInt(attributes, "index", 0));
    else if (qName.equals("roleAssignments"))
      currentSolution.clearRoleAssignments();
    else if (qName.equals("roleAssignment"))
      currentSolution.setRole(getInt(attributes, "person", 0),
                              getInt(attributes, "topic", 0),
                              getInt(attributes, "role", 0));   
    else if (qName.equals("personSums"))
      person = -1;
    else if (qName.equals("personSum"))
      currentSolution.setPersonSum(++person, getDouble(attributes, "sum", 0));
  }
  /**
   * Gets called by the SAX implementation when the end of an XML tag
   * is recognized. 
   */
  public void endElement(String uri, String localName, String qName) 
  {
    if (qName.equals("solution"))
      currentSolution.evaluate();
  }
  
  /**
   * Looks for an attribute with a specified name and returns its value 
   * as an int.
   * 
   * @param attributes  the attribute list to search.
   * @param name        the name of the required attribute.
   * @param def         the default value to use when the attribute is not found.
   * @return            the value of the attribute or the default value if not found.
   */
  int getInt(Attributes attributes, String name, int def)
  {
    String value = attributes.getValue(name);
    
    return value == null ? def : (new Integer(value)).intValue();
  }
  
  /**
   * Looks for an attribute with a specified name and returns its value 
   * as a double.
   * 
   * @param attributes  the attribute list to search.
   * @param name        the name of the required attribute.
   * @param def         the default value to use when the attribute is not found.
   * @return            the value of the attribute or the default value if not found.
   */
  double getDouble(Attributes attributes, String name, double def)
  {
    String value = attributes.getValue(name);
    
    return value == null ? def : (new Double(value)).doubleValue();
  }
  
  /**
   * Looks for an attribute with a specified name and returns its value 
   * as a String. If the attribute is not found, return the String "Unknown n",
   * where "n" is the value of the def-parameter.  
   * 
   * @param attributes  the attribute list to search.
   * @param name        the name of the required attribute.
   * @param def         the number to use in the default value when the attribute is not found.
   * @return            the value of the attribute or the default value if not found.
   */
  String getString(Attributes attributes, String name, int defNumber)
  {
    String value = attributes.getValue(name);
    
    return value == null ? "Unknown " + defNumber : value;
  }
}
