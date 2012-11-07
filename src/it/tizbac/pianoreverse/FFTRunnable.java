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

import java.util.concurrent.ConcurrentHashMap;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;


public class FFTRunnable implements Runnable {
	float fftbuf[];
	ConcurrentHashMap<Integer, double[]> frequency_timedomain;
	int s;
	int tdoffset;
	int div;
	PianoReverseEngineer pianoReverseEngineer ;
	public FFTRunnable(float fb[],int step,ConcurrentHashMap<Integer, double[]> ftd,int tdoffset, PianoReverseEngineer pianoReverseEngineer, int div) {
		fftbuf = fb.clone();
		s = step;
		frequency_timedomain = ftd;
		this.tdoffset = tdoffset;
		this.pianoReverseEngineer = pianoReverseEngineer;
		this.div = div;
	}
	private double FFTGetMag(int freq)
	{
		
		return (Math.sqrt(fftbuf[freq * 2 + 0]
				* fftbuf[freq * 2 + 0]
				+ fftbuf[freq * 2 + 1]
				* fftbuf[freq * 2 + 1])+(Math.sqrt(fftbuf[(fftbuf.length/2-freq-1) * 2 + 0]
						* fftbuf[(fftbuf.length/2-freq-1) * 2 + 0]
								+ fftbuf[(fftbuf.length/2-freq-1) * 2 + 1]
								* fftbuf[(fftbuf.length/2-freq-1) * 2 + 1])))*0.5;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//--------- PARTE DA ESEGUIRE MULTITHREAD ----------
		// Calcola la trasformata di fourier
		FloatFFT_1D fft = new FloatFFT_1D(fftbuf.length/2);
		fft.complexForward(fftbuf);
		
		
		
		/*double sum = 0;
		for (int i = 0; i < fftbuf.length/2; i++)
		{
			sum += Math.sqrt(fftbuf[i * 2 + 0]
					* fftbuf[i * 2 + 0]
					+ fftbuf[i * 2 + 1]
					* fftbuf[i * 2 + 1]);
			
			
		}*/
		int d2 = 1 << div;
		int l = (int) (NoteHit.notes_freqs.length/d2);
		int p = NoteHit.notes_freqs.length-NoteHit.notes_freqs.length/(1 << (div-1));
		//System.out.println("d2="+d2+" l="+l+" p="+p);
		for (int i =  (int) (NoteHit.notes_freqs[p+l]+5) ;i < NoteHit.notes_freqs[p] ; i++) {
			/*
			 * if ( frequency_timedomain.containsKey(i) ==
			 * false) { ArrayList<Double> l = new
			 * ArrayList<Double>((int)
			 * (((double)w.getNumFrames()
			 * /(double)w.getSampleRate())*step_count));
			 * frequency_timedomain.put(i, l); }
			 */
			/*if ( i > 1000)
			{
				gain = 1.0 + (i - 1000)/170.0;
				
				
			}*/
			/*if ( i < 150 )
			{
				double magm1 = FFTGetMag(i-1);
				double mag = FFTGetMag(i);
				double magp1 = FFTGetMag(i+1);
		
				if ( magm1 < mag && mag > magp1 )
					frequency_timedomain.get(i)[s]=mag;
				else
					frequency_timedomain.get(i)[s]=0.0;
			}else{*/
			
			
			for ( int b = 0; b < (int)Math.pow(2.0,div-1); b++)
			{
				frequency_timedomain.get(i)[(int) (s*Math.pow(2.0,div-1)+b)]=FFTGetMag(i)/(double)div;
			}
			//}
		}
		pianoReverseEngineer.progressAdvance(1.0/((double)pianoReverseEngineer.step_count*(double)pianoReverseEngineer.total_seconds));
	}

} 
