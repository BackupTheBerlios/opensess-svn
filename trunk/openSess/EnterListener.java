package openSess;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/*
 * Author:      andreas
 * Created:     27.02.2005
 * Revision ID: $Id: EnterListener.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * EnterListener is a specialized KeyAdapter
 * that determines whether the ENTER key has been pressed
 * on a component.
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class EnterListener
extends KeyAdapter
{
  private String           command;
  private CommandProcessor processor;
  /**
   * Constructs a new EnterListener.
   * 
   * @param command the command to process when ENTER is pressed.
   * @param processor the CommandProcessor to process the command.
   */
  public EnterListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }

  /**
   * Check the event and process the ActionCommand on ENTER.
   */
  public void keyPressed(KeyEvent e) 
  {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) 
      processor.processCommand(command);
  }
}
