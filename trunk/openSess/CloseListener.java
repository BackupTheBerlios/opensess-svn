package openSess;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * Author:      andreas
 * Created:     27.02.2005
 * Revision ID: $Id: CloseListener.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * CloseListener is a specialized WindowAdapter
 * that recognizes attempts from the outside
 * to close the window (e.g. via the system menu).
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class CloseListener
extends WindowAdapter
{
  private String           command;
  private CommandProcessor processor;
  
  /**
   * Constructs a new CloseListener.
   * 
   * @param command the command to process when the window is closed.
   * @param processor the CommandProcessor to process the command.
   */
  public CloseListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }
  
  /**
   * Process the ActionCommand when the window is closed.
   */
  public void windowClosing(WindowEvent e)
  {
    processor.processCommand(command);
  }
}
