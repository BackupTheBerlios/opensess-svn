package openSess;
/*
 * Author:      awi
 * Created:     2005-02-12
 * Revision ID: $Id: OpenSess.java 43 2005-02-25 09:01:08Z andreas $
 */

/**
 * The main class of the GUI.
 * 
 * @author awi
 */
public class OpenSess
{
  /**
   * Create the GUI by instantiating the MainWindow.
   */
  private static void createAndShowGUI()
  {
    MainWindow main = new MainWindow();
  }

  /**
   * Move the GUI creation to a dedicated thread.
   * 
   * @param args  the program arguments.
   */
  public static void main(String[] args)
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        createAndShowGUI();
      }
    });
  }
}