package openSess;
/*
 * Author:      andreas
 * Created:     27.02.2005
 * Revision ID: $Id: CommandProcessor.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * A simple interface for objects that accept string commands.
 * @author andreas
 */
public interface CommandProcessor
{
  /**
   * Execute the command specified as the parameter.
   * 
   * @param command the command.
   */
  public void processCommand(String command);
}
