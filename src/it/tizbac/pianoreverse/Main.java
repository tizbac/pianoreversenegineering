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

import it.jargs.gnu.CmdLineParser;
import it.jargs.gnu.CmdLineParser.IllegalOptionValueException;
import it.jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.IOException;

public class Main {

	private static void printUsage()
	{
		System.err.println("Usage: PianoReverseEngineering -i [input filename] -o [output midi filename]\n {-n} {-s step_count} {-p peak_width}");
		System.err.println("-nf : Disable filtering");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CmdLineParser p = new CmdLineParser();
		CmdLineParser.Option ifname = p.addStringOption('i',"input");
		CmdLineParser.Option ofname = p.addStringOption('o',"output");
		CmdLineParser.Option ssize = p.addDoubleOption('s', "stepcount");
		CmdLineParser.Option nofilter = p.addDoubleOption('n', "nofilter");
		CmdLineParser.Option pw = p.addIntegerOption('p',"peakwidth");
		try {
			p.parse(args);
		} catch (IllegalOptionValueException e1) {
			printUsage();
			System.exit(2);
		} catch (UnknownOptionException e1) {
			printUsage();
			System.exit(2);
		}
		String in_fname = (String) p.getOptionValue(ifname);
		String out_fname = (String) p.getOptionValue(ofname);
		if ( in_fname == null || out_fname == null )
		{
			printUsage();
			MainWindow mw = new MainWindow();
			mw.frmPianoReverseEngineering.setVisible(true);
			/*GUI g = new GUI();
			g.run();
			return;*/
			return;
		}
		
		
		PianoReverseEngineer re = new PianoReverseEngineer(true);
		re.setInputfilename(in_fname);
		re.setMidifilename(out_fname);
		re.setPeakdetect_count((Integer) p.getOptionValue(pw, 12));
		re.setStep_count((Double) p.getOptionValue(ssize, 256.0));
		
		try {
			re.DoTimeFrequencyAnalysis();
			re.DetectNotes();
			if ( !(Boolean)p.getOptionValue(nofilter, false) == true )
				re.Filter();
			re.WriteMidi();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
