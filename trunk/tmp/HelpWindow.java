package openSess;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/*
 * Copyright 2005 Andreas Wickner
 * 
 * Created:     28.02.2005
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

/**
 * A help window that acts as a simple browser on the HTML
 * help files.
 * 
 * @author andreas
 */
public class HelpWindow
  extends JDialog
  implements ActionListener, HyperlinkListener
{
  JEditorPane helpPanel;
  JButton     backButton, forwardButton;
  Vector      history;
  int         currentURL;
  
  /**
   * Constructs a HelpWindow for the specified JFrame.
   * 
   * @param frame the parent JFrame.
   */
  public HelpWindow(JFrame frame)
  {
    super(frame, "Help Information", false);
    history = new Vector();

    JPanel rootPanel = new JPanel();
    rootPanel.setPreferredSize(new Dimension(600, 500));
    getContentPane().add(rootPanel);
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
    rootPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
    rootPanel.add(buttonPanel);
    
    backButton = new JButton("Back");
    backButton.setActionCommand("back");
    backButton.addActionListener(this);
    buttonPanel.add(backButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); 
    
    forwardButton = new JButton("Forward");
    forwardButton.setActionCommand("forward");
    forwardButton.addActionListener(this);
    buttonPanel.add(forwardButton);
    buttonPanel.add(Box.createHorizontalGlue());
    
    JButton closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);
    buttonPanel.add(closeButton);
    
    rootPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
    URL url = getClass().getResource("../help/index.html");
    
    try
    {
      helpPanel = new JEditorPane(url);
      helpPanel.setEditable(false);
      helpPanel.addHyperlinkListener(this);
      JScrollPane scroller = new JScrollPane(helpPanel);
      scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      rootPanel.add(scroller);
    }
    catch (IOException e)
    {
      System.out.println("Help file not found.");
    }

    history.add(url);
    currentURL = 0;
    setButtonStates();
    pack();
  }
  
  /**
   * Enable or disable the back/forward buttons according
   * to the current state of the URL history.
   */
  protected void setButtonStates()
  {
    backButton.setEnabled(currentURL > 0);
    forwardButton.setEnabled(currentURL+1 < history.size());
  }

  /**
   * Process the events for the buttons.
   */
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();
    
    if (source instanceof AbstractButton)
    {
      String command = ((AbstractButton) source).getActionCommand();
      
      try
      {
        if (command.equals("back"))
        {
          if (currentURL > 0)
          {
            --currentURL;
            helpPanel.setPage((URL)history.elementAt(currentURL));
          }
        }
        else if (command.equals("forward"))
        {
          if (currentURL+1 < history.size())
          {
            ++currentURL;
            helpPanel.setPage((URL)history.elementAt(currentURL));
          }
        }
        else if (command.equals("close"))
          setVisible(false);
        
        setButtonStates();
      }
      catch (IOException ex)
      {
        System.out.println("Error displaying: " + ((URL)history.elementAt(currentURL)));
      }
    }
  }
  
  /**
   * Process hyperlink events for the HTML document.
   */
  public void hyperlinkUpdate(HyperlinkEvent e) 
  {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
    {
      // delete everything behind the current place in history
      if (currentURL+1 < history.size())
      {
        // This should actually be the following JDK 1.5 call:
        // history.removeRange(currentURL+1, history.size());
        int rest = history.size() - currentURL - 1;
        
        for (int i = 0;  i < rest;  ++i)
          history.remove(currentURL+1);
      }
      
      URL url = null;
      
      if (e instanceof HTMLFrameHyperlinkEvent) 
      {
        HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
        HTMLDocument doc = (HTMLDocument)helpPanel.getDocument();
        doc.processHTMLFrameHyperlinkEvent(evt);
        url = evt.getURL();
      } 
      else 
      {
        try 
        {
          url = e.getURL();
          helpPanel.setPage(url);
        } catch (Throwable t) 
        {
          t.printStackTrace();
        }
      }
      
      history.add(url);
      ++currentURL;
      setButtonStates();
    }
  }
}
