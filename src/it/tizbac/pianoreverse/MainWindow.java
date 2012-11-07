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

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MainWindow {
	SpectrumViewer sv;
	JFrame frmPianoReverseEngineering;
	private JTextField txtWav;
	private JPanel panel;
	private JButton btnBrowse;
	private JLabel lblOutputFile;
	private JPanel panel_1;
	private JTextField txtMidi;
	private JButton btnBrowse_1;
	private JLabel lblSettings;
	private JPanel panel_2;
	private JCheckBox chckbxFilterNotes;
	private JLabel lblNoteDetectThershold;
	private JSlider sldTh;
	private JLabel lblStepssecond;
	private JSlider sldSteps;
	private JLabel lblProgress;
	private JPanel panel_3;
	private JProgressBar progressBar;
	private JButton btnAbort;
	private JButton btnStart;
	
	
	private PianoReverseEngineer pre = null;
	private Thread pianoreverse_thread = null;
	private Thread progress_thread = null;
	private boolean abort = false;
	private JLabel lblMinimumNoteWidthsteps;
	private JSlider sldDetSteps;
	private JLabel lblTHVal;
	private JLabel lblSTVal;
	private JLabel lblNWVal;
	private JCheckBox chckbxRangeCompression;
	private JButton btnSpec;
	private JMenuBar menuBar;
	private JMenu mnAbout;
	private JMenuItem mntmAbout;
	private JMenuItem mntmInfo;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmPianoReverseEngineering.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPianoReverseEngineering = new JFrame();
		frmPianoReverseEngineering.setTitle("Piano Reverse Engineering");
		frmPianoReverseEngineering.setBounds(100, 100, 592, 371);
		frmPianoReverseEngineering.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPianoReverseEngineering.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:min:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,}));
		
		JLabel lblInputFile = new JLabel("Input file ( WAV)");
		frmPianoReverseEngineering.getContentPane().add(lblInputFile, "2, 2, right, default");
		
		panel = new JPanel();
		frmPianoReverseEngineering.getContentPane().add(panel, "4, 2, fill, center");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		txtWav = new JTextField();
		panel.add(txtWav);
		txtWav.setText("Select file...");
		txtWav.setColumns(10);
		
		btnBrowse = new JButton("Browse...");
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return "Microsoft RIFF WAVE";
					}
					
					@Override
					public boolean accept(File f) {
						// TODO Auto-generated method stub
						return f.getName().toLowerCase().endsWith(".wav") || f.isDirectory();
					}
				});
				int r =  fc.showOpenDialog(frmPianoReverseEngineering);
				if ( r == JFileChooser.APPROVE_OPTION )
				{
					txtWav.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		panel.add(btnBrowse);
		
		lblOutputFile = new JLabel("Output file ( MIDI )");
		frmPianoReverseEngineering.getContentPane().add(lblOutputFile, "2, 4");
		
		panel_1 = new JPanel();
		frmPianoReverseEngineering.getContentPane().add(panel_1, "4, 4, fill, center");
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		txtMidi = new JTextField();
		txtMidi.setText("Select file...");
		panel_1.add(txtMidi);
		txtMidi.setColumns(10);
		
		btnBrowse_1 = new JButton("Browse...");
		btnBrowse_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return "MIDI";
					}
					
					@Override
					public boolean accept(File f) {
						// TODO Auto-generated method stub
						return f.getName().toLowerCase().endsWith(".mid") || f.isDirectory();
					}
				});
				int r =  fc.showOpenDialog(frmPianoReverseEngineering);
				if ( r == JFileChooser.APPROVE_OPTION )
				{
					String fp = fc.getSelectedFile().getAbsolutePath();
					if (!fp.toLowerCase().endsWith(".mid"))
						fp += ".mid";
					txtMidi.setText(fp);
				}
			}
		});
		panel_1.add(btnBrowse_1);
		
		lblSettings = new JLabel("Settings");
		frmPianoReverseEngineering.getContentPane().add(lblSettings, "2, 6");
		
		panel_2 = new JPanel();
		frmPianoReverseEngineering.getContentPane().add(panel_2, "4, 6, fill, fill");
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chckbxFilterNotes = new JCheckBox("Filter notes");
		chckbxFilterNotes.setSelected(true);
		panel_2.add(chckbxFilterNotes, "2, 2");
		
		lblNoteDetectThershold = new JLabel("Note detect thershold");
		panel_2.add(lblNoteDetectThershold, "2, 4");
		
		sldTh = new JSlider();
		
		lblTHVal = new JLabel("Val");
		panel_2.add(lblTHVal, "6, 4");
		sldTh.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lblTHVal.setText(Integer.toString(sldTh.getValue()));
			}
		});
		
		sldTh.setSnapToTicks(true);
		sldTh.setValue(15);
		sldTh.setPaintLabels(true);
		sldTh.setPaintTicks(true);
		sldTh.setMinimum(5);
		sldTh.setMaximum(150);
		panel_2.add(sldTh, "4, 4");
		
		lblSTVal = new JLabel("Val");
		panel_2.add(lblSTVal, "6, 6");
		
		lblStepssecond = new JLabel("Steps/second");
		panel_2.add(lblStepssecond, "2, 6");
		
		sldSteps = new JSlider();
		sldSteps.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lblSTVal.setText(Integer.toString(sldSteps.getValue()));
			}
		});
		sldSteps.setSnapToTicks(true);
		sldSteps.setPaintTicks(true);
		sldSteps.setPaintLabels(true);
		sldSteps.setMinimum(15);
		sldSteps.setMaximum(1024);
		sldSteps.setValue(80);
		panel_2.add(sldSteps, "4, 6");
		
		
		
		lblMinimumNoteWidthsteps = new JLabel("Minimum note width(steps)");
		panel_2.add(lblMinimumNoteWidthsteps, "2, 8");
		
		lblNWVal = new JLabel("Val");
		panel_2.add(lblNWVal, "6, 8");
		
		sldDetSteps = new JSlider();
		sldDetSteps.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lblNWVal.setText(Integer.toString(sldDetSteps.getValue()));
			}
		});
		sldDetSteps.setValue(2);
		sldDetSteps.setMinimum(2);
		sldDetSteps.setMaximum(50);
		sldDetSteps.setPaintLabels(true);
		sldDetSteps.setPaintTicks(true);
		panel_2.add(sldDetSteps, "4, 8");
		
		chckbxRangeCompression = new JCheckBox("Range compression");
		panel_2.add(chckbxRangeCompression, "2, 10");
		
		
		
		lblProgress = new JLabel("Progress");
		frmPianoReverseEngineering.getContentPane().add(lblProgress, "2, 8");
		
		panel_3 = new JPanel();
		frmPianoReverseEngineering.getContentPane().add(panel_3, "4, 8, fill, fill");
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStart.setEnabled(false);
				pre = new PianoReverseEngineer(true);
				pre.setInputfilename(txtWav.getText());
				pre.setMidifilename(txtMidi.getText());
				pre.setStep_count(sldSteps.getValue());
				pre.setMinvalue(sldTh.getValue());
				pre.setPeakdetect_count(sldDetSteps.getValue()/2);
				pre.setCompress(chckbxRangeCompression.isSelected());
				btnAbort.setEnabled(true);
				progress_thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while ( !abort )
						{
							final double prog = pre.getFreqanalysis_progress();
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									progressBar.setValue((int) (prog*100));
								}
							});
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						pre.abort();
					}
				});
				final boolean f = chckbxFilterNotes.isSelected();
				pianoreverse_thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							pre.DoTimeFrequencyAnalysis();
							pre.PrintAnalysis();
							pre.DetectNotes();
							if ( f)
								pre.Filter();
							pre.WriteMidi();
							
						} catch (final IOException e) {
							// TODO Auto-generated catch block
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									OnFinish(false, e.getMessage());
								}
							});
							return;
						} catch (final WavFileException e) {
							// TODO Auto-generated catch block
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									OnFinish(false, e.getMessage());
								}
							});
							return;
						}
						
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								OnFinish(true, "OK");
							}
						});
					}
				});
				pianoreverse_thread.start();
				progress_thread.start();
			}
		});
		panel_3.add(btnStart);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		panel_3.add(progressBar);
		
		btnAbort = new JButton("Abort");
		btnAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				abort = true;
			}
		});
		btnAbort.setEnabled(false);
		panel_3.add(btnAbort);
		
		btnSpec = new JButton("Spectrum");
		btnSpec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sv = new SpectrumViewer(pre);
				sv.setVisible(true);
			}
		});
		panel_3.add(btnSpec);
		
		menuBar = new JMenuBar();
		frmPianoReverseEngineering.setJMenuBar(menuBar);
		
		mnAbout = new JMenu("?");
		menuBar.add(mnAbout);
		
		mntmInfo = new JMenuItem("Info");
		mntmInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		mnAbout.add(mntmInfo);
		
		mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog dlg = new AboutDialog();
				dlg.setVisible(true);
			}
		});
		mnAbout.add(mntmAbout);
	}
	
	public void OnFinish(boolean success, String errormsg)
	{
		if (!success )
		{
			
			JOptionPane.showMessageDialog(frmPianoReverseEngineering, errormsg,"Error",JOptionPane.ERROR_MESSAGE);
			
		}else{
			JOptionPane.showMessageDialog(frmPianoReverseEngineering, "Successo","OK",JOptionPane.INFORMATION_MESSAGE);
		}
		abort = true;
		try {
			
			progress_thread.join();
			pianoreverse_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		abort = false;
		
		btnStart.setEnabled(true);
		btnAbort.setEnabled(false);
		
	}

}
