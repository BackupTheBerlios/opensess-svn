/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     2005-03-19 
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
package openSess;

/**
 * TaskMonitor is a base class for classes that must perform
 * a task in a different (non-GUI) thread. The task progress can
 * be visualized with a ProgressMonitor.
 * 
 * Classes that extend TaskMonitor should:
 * 
 * - Redefine doTask() to contain the work to be done in the
 *   new thread.
 * - Check taskWasCanceled() frequently to see whether the task
 *   was canceled from the outside (e.g. by the GUI user). If this
 *   is the case, doTask() should terminate as soon as possible.
 * - Call setCurrent() whenever there is progress to report. The value
 *   passed to setCurrent() should be appropriate for the ProgressMonitor.
 * - Call setMessage() with progress messages if appropriate.
 * 
 * @author andreas
 */
public class TaskMonitor
{
  private int              current      = 0;
  private boolean          taskDone     = false;
  private boolean          taskCanceled = false;
  private String           statMessage;
  
  /**
   * Return the current progress.
   * 
   * @return the current progress.
   */
  public int getCurrent()
  {
    return current;
  }

  /**
   * Set the current progress (must be in the valid range of the
   * ProgressMonitor used).
   *  
   * @param current the current progress.
   */
  public void setCurrent(int current)
  {
    this.current = current;
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
   * Returns whether the task was canceled from outside.
   * 
   * @return true if the task has been canceled.
   */
  public boolean taskWasCanceled()
  {
    return taskCanceled;
  }

  /**
   * Returns whether the task has completed (by itself).
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
   * Sets the current status message.
   * 
   * @param message
   */
  public void setMessage(String message)
  {
    statMessage = message;
  }
  
  /**
   * Start the task: Call the method doTask() in its own thread.
   */
  public void startTask()
  {
    final TaskMonitor theMonitorItself = this;
    
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        current = 0;
        taskDone = false;
        taskCanceled = false;
        statMessage = null;
        return new SolverTask(theMonitorItself); 
      }
    };
    
    worker.start();
  }

  /**
   * Derived classes should redefine this method with the work to
   * be done.
   */
  protected void doTask()
  {
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
    public SolverTask(TaskMonitor monitor)
    {
      monitor.doTask();
      monitor.taskDone = true;
    }
  };
}
