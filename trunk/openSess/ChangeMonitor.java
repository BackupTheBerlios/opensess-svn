package openSess;
/*
 * Author:      andreas
 * Created:     22.02.2005
 * Revision ID: $Id: ChangeMonitor.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 *  A very simple base class to keep track of changes to the state
 *  of an object or a network of objects.
 * 
 * @author andreas
 */
public class ChangeMonitor
{
  private boolean unsavedChanges = false;
  
  /**
   * Tells the ChangeMonitor that the state is "clear", i.e. that there
   * are no unsaved changes. Directly after a call to clearChanges(),
   * hasChanged() returns false.
   */
  public void clearChanges()
  {
    unsavedChanges = false;
  }

  /**
   * Tells the ChangeMonitor, that a change to the state has occured.
   * Subsequent calls to hasChanged() return true.
   */
  public void signalChange()
  {
    unsavedChanges = true;
  }

  /**
   * Returns true if there are unsaved changes.
   * 
   * @return true if there are unsaved changes.
   */
  public boolean hasChanged()
  {
    return unsavedChanges;    
  }
}
