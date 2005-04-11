package openSess;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataListener;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     17.02.2005
 * Revision ID: $Id: SharedDataComboBoxModel.java 10 2005-03-04 18:45:41Z awickner $
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
    return (index >= data.size()) ? null : data.getElementAt(index);
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
