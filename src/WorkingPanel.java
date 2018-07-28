/*
 *    FireSteg
 *    Copyright (C) 2009  Zachary Oakes
 *
 *	  Digital Invisible Ink Toolkit
 *    Copyright (C) 2005  K. Hempstalk	
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package invisibleinktoolkit.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JDialog;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A non-blocking window to state "Working, please wait".
 *
 * @author Kathryn Hempstalk.
 */
public class WorkingPanel{
	
	//VARIABLES
	
	/**
	 * The dialog being hidden/shown.
	 */
	private JDialog mDialog;
	
	/**
	 * Button to cancel the current operation.
	 * 
	 */
	private JButton mCancelButton;
	
	/**
	 * The text that is displayed.
	 * 
	 */
	private JLabel mLabel;
	
	/**
	 * The progress bar.
	 * 
	 */
	private JProgressBar mMovingThing;
	
		
	//CONSTRUCTORS	
	
	
	/**
	 * Sets up a working panel that displays "Working, please wait".
	 *
	 * @param wt The worker thread that pressing the cancel button will stop.
	 */
	public WorkingPanel(){		
		Frame parent = null;
		mDialog = new JDialog(parent);
		JPanel contentPanel = new JPanel();
		contentPanel.setPreferredSize(new Dimension(400, 85));
		contentPanel.setLayout(new BorderLayout());
		
		mLabel = new JLabel("");
		mLabel.setPreferredSize(new Dimension(400,40));
				
		mCancelButton = new JButton("Cancel");
		mCancelButton.setPreferredSize(new Dimension(100, 30));
		
		contentPanel.add(mLabel, BorderLayout.NORTH);
		
		mMovingThing = new JProgressBar();
		mMovingThing.setValue(0);
		mMovingThing.setPreferredSize(new Dimension(400,15));
		contentPanel.add(mMovingThing, BorderLayout.CENTER);
		
		contentPanel.add(mCancelButton, BorderLayout.SOUTH);
		
		mCancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				System.exit(1);
				
			}
		});
				
		mDialog.getContentPane().add(contentPanel);
		mDialog.pack();
		mDialog.repaint();
		mDialog.setLocationRelativeTo(parent);
	}
	
	//FUNCTIONS
	
	/**
	 * Shows this dialog centered on the parent on screen.
	 */
	public void show(){
		mDialog.setVisible(true);
	}
	
	/**
	 * Hides this dialog.
	 */
	public void hide(){
		mDialog.setVisible(false);
	}
	
	public void setLabel(String labelName){
		mLabel.setText(labelName);
	}
	
	public void setMax(int number){
		mMovingThing.setMaximum(number);
	}
	
	public void addValue(int number){
		mMovingThing.setValue(mMovingThing.getValue() + number);
	}
	
}
//end of class.
