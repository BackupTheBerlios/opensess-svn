
package openSess;
import javax.swing.JFrame;

/*
 * Author:      andreas
 * Created:     15.02.2005
 * Revision ID: $Id: EditTopicWindow.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * An EditTopicWindow is simply a ListEditWindow.
 * 
 * @author andreas
 */
public class EditTopicWindow
  extends ListEditWindow
{
  /**
   * Construct a new EditTopicWindow.
   * 
   * @param frame
   */
  public EditTopicWindow(JFrame frame)
  {
    super(frame, "Edit Topic");
  }
}

