package openSess;
import javax.swing.JFrame;

/*
 * Author:      andreas
 * Created:     17.02.2005
 * Revision ID: $Id: EditRoleWindow.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * The window for editing role names is simply a ListEditWindow.
 * 
 * @author andreas
 */
public class EditRoleWindow
  extends ListEditWindow
{
  /**
   * Constructs an EditRoleWindow.
   * 
   * @param frame
   */
  public EditRoleWindow(JFrame frame)
  {
    super(frame, "Edit Role");
  }
}
