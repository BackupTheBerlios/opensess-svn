package openSess;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
 * Author:      andreas
 * Created:     27.02.2005
 * Revision ID: $Id: DoubleClickListener.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * DoubleClickListener is a specialized MouseAdapter
 * that determines whether a component has been double-clicked.
 * When that happens, a CommandProcessor is called with the 
 * command provided in the constructor.
 *  
 * @author andreas
 */
public class DoubleClickListener
extends MouseAdapter
{
  private String           command;
  private CommandProcessor processor;
  /**
   * Constructs a new DoubleClickListener.
   * 
   * @param command the command to process when an item is double-clicked.
   * @param processor the CommandProcessor to process the command.
   */
  public DoubleClickListener(String command, CommandProcessor processor)
  {
    this.command   = command;
    this.processor = processor;
  }
  
  /**
   * Check the event and process the ActionCommand on double-click.
   */
  public void mouseClicked(MouseEvent e) 
  {
    if (e.getClickCount() == 2) 
      processor.processCommand(command);
  }
}
