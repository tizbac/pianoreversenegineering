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

import java.awt.Color;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Hashtable;


import javax.swing.JPanel;


public class SpectrumViewPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8154382088258146261L;
	int startStep = 0;
	PianoReverseEngineer pre;

	public SpectrumViewPanel(PianoReverseEngineer pre) {
		super();
		this.pre = pre;
		setBackground(Color.BLACK);
	}

	int px_step = 4;

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);

		int w = getSize().width;
		int h = getSize().height;
		int viewable_steps = w / px_step;
		int whitekey_height = h / 52;
		int blackkey_height = whitekey_height / 2;
		int nota = 0;

		Hashtable<Integer, ArrayList<NoteHit>> nh = new Hashtable<Integer, ArrayList<NoteHit>>();
		ArrayList<Integer> times = new ArrayList<Integer>();
		if (pre != null) {
			nh = pre.getNotes_hit();
			times = pre.getTimes();
		}
		Hashtable<Integer,Integer> y_for_notes = new Hashtable<Integer, Integer>();
		for (int i = 0; i < 52; i++) {
			g.setColor(Color.BLACK);
			g.drawRect(0, whitekey_height * i, 50, whitekey_height);
			g.setColor(Color.WHITE);
			g.fillRect(1, whitekey_height * i + 1, 50 - 1, whitekey_height - 1);
			g.setColor(Color.BLACK);
			g.drawString(NoteHit.notes_names[nota], 1, whitekey_height
					* (i + 1));
			int spectrumbar_y = whitekey_height * i + whitekey_height / 2
					- blackkey_height / 2;
			y_for_notes.put(nota, spectrumbar_y);
			for (int s = 0; s < viewable_steps; s++) {

				int level = 0;
				if (pre != null
						&& startStep + s < pre.getNotes_timedomain().length && (int) (startStep + s + pre.step_count*0.5) > 0) {
					try{
						level = (int) pre.getNotes_timedomain()[(int) (startStep + s + pre.step_count*0.5)][nota];
					}catch (ArrayIndexOutOfBoundsException e)
					{
						level = -1;
					}
				}
				int red = 255, green = 255, blue = 255;
				if (level > 511) {
					red = 255;
					green = 255;
					blue = Math.min(255, level - 511);
				} else if (level > 255) {
					red = 255;
					green = level - 256;
					blue = 0;
				} else {
					if ( level >= 0)
					{
						red = level;
						green = 0;
						blue = 0;
					}else{
						blue = 255;
						red = 0;
						green = 0;
					}
				}
				g.setColor(new Color(red, green, blue));
				g.fillRect(50 + s * px_step, spectrumbar_y, px_step,
						blackkey_height);
			}

			int z = i;// Shift verso l'alto
			if (i != 0 && z % 7 != 4 && z % 7 != 0) {
				nota++;
				if (nota < NoteHit.notes_freqs.length) {

					spectrumbar_y = whitekey_height * i + whitekey_height / 2
							- blackkey_height / 2 + blackkey_height;
					y_for_notes.put(nota, spectrumbar_y);
					for (int s = 0; s < viewable_steps; s++) {

						int level = 0;
						if (pre != null
								&& startStep + s < pre.getNotes_timedomain().length && (int) (startStep + s + pre.step_count*0.5) > 0 ) {
							try{
							level = (int) pre.getNotes_timedomain()[(int) (startStep
									+ s + pre.step_count*0.5)][nota];
							}catch (ArrayIndexOutOfBoundsException e)
							{
								level = -1;
							}
						}

						int red = 255, green = 255, blue = 255;
						if (level > 511) {
							red = 255;
							green = 255;
							blue = Math.min(255, level - 511);
						} else if (level > 255) {
							red = 255;
							green = level - 256;
							blue = 0;
						} else {
							if ( level >= 0)
							{
								red = level;
								green = 0;
								blue = 0;
							}else{
								blue = 255;
								red = 0;
								green = 0;
							}
						}
						g.setColor(new Color(red, green, blue));
						g.fillRect(50 + s * px_step, spectrumbar_y, px_step,
								blackkey_height);

					}
				}
			} else {
				spectrumbar_y = whitekey_height * i + whitekey_height / 2
						- blackkey_height / 2 + blackkey_height;
				for (int s = 0; s < viewable_steps; s++) {
					// Spettro tasti neri

					int level = 0;
					g.setColor(new Color(level, level, level));
					g.fillRect(50 + s * px_step, spectrumbar_y, px_step,
							blackkey_height);

				}

			}
			nota++;
		}
		g.setColor(Color.GREEN);
		for (int t = 0; t < times.size(); t++) {
			int tt = times.get(t);
			if (tt < getTimeMSForStep(startStep)
					|| tt > getTimeMSForStep(startStep + viewable_steps)) {
				continue;
			}
			ArrayList<NoteHit> hits = nh.get(tt);
			for (int b = 0; b < hits.size(); b++) {
				NoteHit H = hits.get(b);

				g.fillRect(getXforTime(H.getMstime()),
						(y_for_notes.get(H.getNoteid()))
								+ blackkey_height / 2- blackkey_height/4,
						getWidthforTime(H.getDuration()), blackkey_height / 2);
			}

		}
		for (int i = 0; i < 52; i++)// Intermedi
		{
			if (i == 0)
				continue;
			int z = i - 1;// Shift verso l'alto
			if (z % 7 != 4 && z % 7 != 0) {
				g.setColor(Color.BLACK);
				g.fillRect(0, whitekey_height * i - blackkey_height / 2, 30,
						blackkey_height);
			}
		}

	}

	public void setStartStep(int step) {
		startStep = step;

	}

	public int getStartStep() {
		return startStep;
	}

	@SuppressWarnings("unused")
	private boolean isInBounds(int timems) {
		if (getXforTime(timems) < 50)
			return false;
		if (getXforTime(timems) > getSize().width)
			return false;

		return true;
	}

	private int getXforTime(int timems) {
		double timesecs = (double) timems / 1000.0;
		double steps = timesecs * pre.step_count - startStep;
		return (int) (50 + (steps * px_step));
	}
	private int getWidthforTime(int timems) {
		double timesecs = (double) timems / 1000.0;
		double steps = timesecs * pre.step_count;
		return (int) ((steps * px_step));
	}
	private int getTimeMSForStep(int step) {
		return (int) ((step * 1000) / pre.step_count);

	}
}
