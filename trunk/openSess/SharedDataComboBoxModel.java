package openSess;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataListener;

/*
 * Author:      andreas
 * Created:     17.02.2005
 * Revision ID: $Id: SharedDataComboBoxModel.java 48 2005-03-01 11:12:27Z awi $
 */

/**
 * This class implements a ComboBoxModel that has its own
 * selection state but shares its data with other SharedDataComboBoxModels.
 * 
 * @author andreas
 *
 */
public class SharedDataComboBoxModel
  implements ComboBoxModel
{
  private DefaultListModel data;
	private int selection = -1;
  
	/**
	 * Construct a SharedDataComboBoxModel that referes to data in the
	 * specified DefaultListModel.
	 * 
	 * @param data the DefaultListModel.
	 */
  public SharedDataComboBoxModel(DefaultListModel data)
  {
    this.data = data;
  }
  
  /**
   * Add a ListDataListener.
   */
  public void addListDataListener(ListDataListener l)
  {
    data.addListDataListener(l);
  }
  
  /**
   * Retrieve the element at the specified index.
   */
  public Object getElementAt(int index)
  {
    return data.getElementAt(index);
  }
  
  /**
   * Return the number of elements in the data list.
   */
  public int getSize()
  {
    return data.getSize();
  }
  
  /**
   * Remove a ListDataListener.
   */
  public void removeListDataListener(ListDataListener l)
  {
    data.removeListDataListener(l);
  }
  
  /**
   * Set the element at the specified index.
   * 
   * @param item  the new item.
   * @param index the index.
   */
  public void setElementAt(Object item, int index)
  {
    data.setElementAt(item, index);
  }
  
  /**
   * Return the currently selected item.
   */
  public Object getSelectedItem()
  {
    return selection >= 0 && data.getSize() > selection ? data.getElementAt(selection) : null;
  }

  /**
   * Set the currently selected item.
   */
  public void setSelectedItem(Object item)
  {
		selection = data.indexOf(item);
  }
}
