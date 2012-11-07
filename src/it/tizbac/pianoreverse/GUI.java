/*  Piano Reverse Engineering
Copyright (C) 2012  Tiziano Bacocco

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
package it.tizbac.pianoreverse;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUI extends JFrame{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6896719638831855847L;
	@SuppressWarnings("deprecation")
	public GUI() {
		super("Piano reverse engineering");
		Container c = getContentPane();
		setSize(600,200);
		c.setLayout( new BorderLayout( 30, 30 ) );
		show();
		addWindowListener(
				 new WindowAdapter() {
				 public void windowClosing( WindowEvent e )
				 {
				 System.exit( 0 );
				 }
				 });
	}
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
