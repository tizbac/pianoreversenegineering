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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Hashtable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.*;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class PianoReverseEngineer {
	int peakdetect_count = 6;
	String inputfilename = "";
	String midifilename = "";
	int wcount = 0;
	double wsum = 0.0;
	double freqanalysis_progress = 0.0;
	double wmax = -1.0;
	int numChannels;
	double step_count = 256.0;
	int total_seconds;
	double[][] notes_timedomain;
	ConcurrentHashMap<Integer, double[]> frequency_timedomain;
	Hashtable<Integer, ArrayList<NoteHit>> notes_hit;
	ArrayList<Integer> times;
	int totalsteps;
	double windowWidth = 15.0;
	double minvalue = 15.0;
	boolean compress = false;
	double segmentfft[][];
	double maxmag = -1.0;
	boolean hand; //True: Mano destra , False: mano sinistra
	synchronized void progressAdvance(double val)
	{
		freqanalysis_progress += val;
	}
	
	public double getMinvalue() {
		return minvalue;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public void setMinvalue(double minvalue) {
		this.minvalue = minvalue;
	}

	public double[][] getNotes_timedomain() {
		return notes_timedomain;
	}

	public ArrayList<Integer> getTimes() {
		return times;
	}

	public Hashtable<Integer, ArrayList<NoteHit>> getNotes_hit() {
		return notes_hit;
	}

	public double getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(double windowWidth) {
		this.windowWidth = windowWidth;
	}

	private boolean stopping = false;
	private WavFile w;

	/*
	 * private void ResetPreAnalisysVars() { wcount = 0; wsum = 0; wmax = -1.0;
	 * frequency_timedomain.clear(); }
	 */
	public void setStep_count(double step_count) {
		this.step_count = step_count;
	}

	public double getStep_count() {
		return step_count;
	}

	public PianoReverseEngineer(boolean h /*Hand*/) {
		hand = h;
	}

	public String getInputfilename() {
		return inputfilename;
	}

	public String getMidifilename() {
		return midifilename;
	}

	public int getPeakdetect_count() {
		return peakdetect_count;
	}

	public void setInputfilename(String inputfilename) {
		this.inputfilename = inputfilename;
	}

	public void setMidifilename(String midifilename) {
		this.midifilename = midifilename;
	}

	public void setPeakdetect_count(int peakdetect_count) {
		this.peakdetect_count = peakdetect_count;
	}

	public double getFreqanalysis_progress() {
		return freqanalysis_progress;
	}
	private void DoRangeAnalisys(int div) throws IOException, WavFileException 
	{
		w = WavFile.openWavFile(new File(inputfilename));
		w.display();
		double step_count_old = step_count;
		step_count /= Math.pow(2.0, div-1);
		ExecutorService svc = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());
		
		freqanalysis_progress = 0;
		numChannels = w.getNumChannels();
		int fr;
		int stepcounter = 0;
		
		
		
		int bs = (int) ((w.getSampleRate()) * numChannels);
		double[] buffer = new double[bs];
		double[] chanbuf = new double[bs / numChannels];
		float[] fftbuf = new float[chanbuf.length * 2];
		ArrayList<Double> buf = new ArrayList<Double>();
		// CsvWriter csv = new CsvWriter("/home/tiziano/out.csv");
		// csv.writeRecord(notes_names);

		System.out.printf(
				"Allocazione tabella frequenza-tempo (%f Mbyte)...\n",
				((Double.SIZE / 8) * 5000 * step_count) / 1024 / 1024);
		for (int i = 0; i < 5000; i++) {
			if (frequency_timedomain.containsKey(i) == false) {
				frequency_timedomain.put(i, new double[(int) step_count]);
			}
		}
		// 1 Secondo di silenzio
		for (int i = 0; i < buffer.length; i++) {
			buf.add(0.0);
		}
		// 1 secondo buffering
		try {
			fr = w.readFrames(buffer, chanbuf.length);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			fr = 0;
		} catch (WavFileException e2) {
			// TODO Auto-generated catch block
			fr = 0;
		}
		if (fr > 0) {
			// Prende solo il primo canale
			for (int i = 0; i < fr ; i++) {
				double sum = 0;
				for ( int k = 0; k < numChannels; k++)
				{
					sum += buffer[i*numChannels+k];
					
					
				}
				sum/=(double)numChannels;
				buf.add(sum);
				if (Math.abs(sum) > wmax)
					wmax = sum;
				wcount++;
				wsum += sum;
			}
		} else {
			System.out.println("Read error. " + w.getFramesRemaining());
		}
		double step_len = 1.0/step_count;
		step_len *= 16;
		double wlen = (Math.log(2))/Math.pow(step_len/2,2.0);
		WindowingFunction wf = new WindowingFunction(chanbuf.length, wlen);

		// Questo limiterà la durata di una nota a 1 secondo ma è più che
		// sufficiente per il piano forte
		segmentfft = new double[total_seconds][buffer.length*2];//TODO: Controllare le note con l'fft calcolato secondo per secondo o ogni mezzo secondo
		
		for (int offset_in_seconds = 0; offset_in_seconds < total_seconds; offset_in_seconds++) {
			if (stopping)
				return;
			System.out.println("Analisi...");
			try {
				fr = w.readFrames(buffer, chanbuf.length);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				fr = 0;
			} catch (WavFileException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				fr = 0;
			}
			if (fr > 0) {
				// Prende solo il primo canale
				for (int i = 0; i < fr ; i++) {
					double sum = 0;
					for ( int k = 0; k < numChannels; k++)
					{
						sum += buffer[i*numChannels+k];
						
						
					}
					sum /=(double)numChannels;
					buf.add(sum);
					if (Math.abs(sum) > wmax)
						wmax = sum;
					wcount++;
					wsum += sum;
				}
			} else {
				System.out.println("Read error. " + w.getFramesRemaining());
			}
			System.out.println("B=" + (3 * chanbuf.length - buf.size()) + " A="
					+ buf.size());
			// Aggiunge se necessario il padding alla fine per allineare a 3
			// secondi
			for (int i = 0; i < 3 * chanbuf.length - buf.size(); i++) {
				buf.add(0.0);
			}
			System.out.println("Rimanenti "
					+ (total_seconds - offset_in_seconds)
					+ " secondi, buf.size=" + buf.size() + " cb.l="
					+ chanbuf.length);
			svc = Executors.newFixedThreadPool(Runtime.getRuntime()
					.availableProcessors());
			
			/*for ( int i = 0; i < buffer.length; i++)
			{
				segmentfft[offset_in_seconds][i*2+0] = buf.get(i);//REAL
				segmentfft[offset_in_seconds][i*2+1] = 0.0;//IMAG
			}
			DoubleFFT_1D sfft = new DoubleFFT_1D(buffer.length);
			sfft.complexForward(segmentfft[offset_in_seconds])*/
			for (int s = 0; s < step_count; s++) {
				for (int i = 0; i < chanbuf.length; i++) {
					// Viene applicata una funzione gaussiana di
					// windowing
					fftbuf[i * 2 + 0] = (float) (buf.get(i) * wf.GetCoeff(i));
					fftbuf[i * 2 + 1] = 0.0f;
				}
				FFTRunnable frun = null;

				frun = new FFTRunnable(fftbuf, s, frequency_timedomain, 0, this,div);

				// Rimuovi i campioni dello step vecchio
				buf.subList(0, (int) (chanbuf.length / step_count)).clear();
				svc.submit(frun);
				// --------- END ---------------------

			}
			svc.shutdown();
			try {
				svc.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int d2 = 1 << div;
			int L = (int) (NoteHit.notes_freqs.length/d2);
			int p = NoteHit.notes_freqs.length-NoteHit.notes_freqs.length/(1 << (div-1));
			for (int k = p; k < L+p; k++) {
				int freq = (int) NoteHit.notes_freqs[k];

				for (int i = 0; i < frequency_timedomain.get(freq).length; i++) {
					notes_timedomain[stepcounter + i][k] = 0;

				}
				int l = 1;
				if ( freq > 150 )
				{
					l = 2;
					
				}
				if ( freq > 400 )
				{
					l = 3;
					
				}
				for (int u = freq-l ; u < freq + 1+l; u++) {
					for (int i = 0; i < frequency_timedomain.get(u).length; i++) {
						if (notes_timedomain[stepcounter + i][k] < frequency_timedomain
								.get(u)[i])
							notes_timedomain[stepcounter + i][k] = frequency_timedomain
									.get(u)[i];

					}
				}
			}
			stepcounter += (int) step_count_old;

			/*
			 * for (int i = peakdetect_count; i < frequency_timedomain.get(
			 * frequency_timedomain.keySet().toArray()[0]).length -
			 * peakdetect_count; i++) { csv.writeRecord(cr[i]); }
			 */

		}
		System.out.println("Analisi massimo valore e media...");
		/*
		 * double wavg = wsum / (double) wcount; for (int k = 0; k <
		 * NoteHit.notes_freqs.length; k++) { for (int i = 0; i <
		 * notes_timedomain.length; i++) { notes_timedomain[i][k] *= (1.0 /
		 * wavg)*0.05;
		 * 
		 * }
		 * 
		 * }
		 */
		step_count = step_count_old;
	}
	public void DoTimeFrequencyAnalysis() throws IOException, WavFileException {
		w = WavFile.openWavFile(new File(inputfilename));
		w.display();
		double total_seconds_d = ((double) w.getFramesRemaining() / (double) w
				.getSampleRate());
		
		totalsteps = (int) (((double) w.getNumFrames() / (double) w
				.getSampleRate()) * step_count);
		total_seconds = (int) Math.ceil(total_seconds_d);

		
		frequency_timedomain = new ConcurrentHashMap<Integer, double[]>();

		
		notes_timedomain = new double[totalsteps + (int) step_count][NoteHit.notes_freqs.length];
		/*System.out.println("Risoluzione: " + (1.0 / step_count) + " secondi, "
				+ w.getSampleRate() / chanbuf.length + " Hz");*/
		notes_hit = new Hashtable<Integer, ArrayList<NoteHit>>();

		times = new ArrayList<Integer>();
		
		DoRangeAnalisys(1);
		DoRangeAnalisys(2);
		DoRangeAnalisys(3);
		DoRangeAnalisys(4);
	}

	public void PrintAnalysis() {
		FileWriter fstream = null;
		;
		try {
			fstream = new FileWriter("/home/tiziano/out.csv");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter out = new BufferedWriter(fstream);
		for (int k = 0; k < NoteHit.notes_freqs.length; k++) {
			try {
				out.write(NoteHit.notes_names[k] + "\t");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			out.write("\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < notes_timedomain.length; i++) {
			for (int k = 0; k < NoteHit.notes_freqs.length; k++) {
				try {
					out.write(notes_timedomain[i][k] + "\t");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				out.write("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void DetectNotes() {
		Hashtable<Integer, NoteHit> last_notes = new Hashtable<Integer, NoteHit>();
		System.out.println("Analisi grafici delle note...");
		int start;
		int end;
		/*if ( !hand )
		{
			start = NoteHit.notes_freqs.length/2;
			end = NoteHit.notes_freqs.length;
		}else{
			start = 0;
			end = NoteHit.notes_freqs.length/2;
		}*/
		for (int k = 0; k < NoteHit.notes_freqs.length; k++) {
			//String n = NoteHit.notes_names[k];
			double time = -0.5;
			int time_ms = -500;
			int step_ms = (int) ((1.0 / step_count) * 1000.0);
			time += (1.0 / step_count) * 2;
			time_ms += step_ms * 2;
			boolean pressed = false;
			int begin = 0;
			double max = Double.MIN_VALUE;
			int maxtime = 0;
			double startintens = 0.0;
			double old = notes_timedomain[0][k];
			int positive_delta_count = 0;
			int negative_delta_count = 0;
			for (int i = peakdetect_count; i < notes_timedomain.length
					- peakdetect_count; i++) {
				// System.out.println();
				double value = notes_timedomain[i][k];
				double startvalue = notes_timedomain[i-peakdetect_count][k];
				double delta = value-old;
				if ( delta > 0.0 )
				{
					if ( positive_delta_count == 0 && !pressed)
						startintens = value;
					positive_delta_count++;
					negative_delta_count = 0;
				}
				if ( delta < 0.0 )
				{
					positive_delta_count = 0;
					negative_delta_count ++;
				}
				if ( pressed && value > max )
				{
					max = value;
					maxtime = time_ms;
					positive_delta_count = 0;
				}
				if ( positive_delta_count >= peakdetect_count )
				{
					if ( pressed && positive_delta_count >= peakdetect_count*2 )
					{
						int duration = time_ms-begin;
						int t = maxtime;
						if ( max - startintens > 30.0)
						{
							t = ((int) t / (int) 75) * 75;
							if (!notes_hit.containsKey(t)) {
								notes_hit.put(t, new ArrayList<NoteHit>());
								times.add(t);
							}
							double intens = max*2.0;
		
							NoteHit note = new NoteHit(k, maxtime, duration,
									(int) Math.min(127, intens / 4.0),max);
							if (max > maxmag )
								maxmag = max;
							notes_hit.get(t).add(note);
							//note.center();
							last_notes.put(k, note);
						}
						
						
					}
					pressed = true;
					begin = time_ms;
					max = value;
					maxtime = time_ms;
					negative_delta_count = 0;
		
				}
				if ( (value < max/2.0 && pressed) )
				{
					int duration = time_ms-begin;
					int t = maxtime;
					if ( max - startintens > 30.0)
					{
						t = ((int) t / (int) 75) * 75;
						if (!notes_hit.containsKey(t)) {
							notes_hit.put(t, new ArrayList<NoteHit>());
							times.add(t);
						}
						double intens = max*2.0;
	
						NoteHit note = new NoteHit(k, maxtime, duration,
								(int) Math.min(127, intens / 4.0),max);
						
						notes_hit.get(t).add(note);
						//note.center();
						last_notes.put(k, note);
					}
					pressed = false;
					max = 0;
				}
				
				
				
				old = value;
				// l'FFT non ha risposta lineare in frequenza
				// double f =
				// (double)i/((double)frequency_timedomain.get(freq).size()*0.5);
				// value *= f;

				// ---------------------------------------------

				/*double lval = -1;
				boolean leftok = true;
				boolean rightok = true;
				for (int z = i - peakdetect_count / 2; z < i + 1; z++) {
					if (notes_timedomain[z][k] <= lval) {
						leftok = false;
						break;
					}
					lval = notes_timedomain[z][k];
				}
				lval = 99999;
				for (int z = i; z < i + peakdetect_count; z++) {
					if (notes_timedomain[z][k] >= lval) {
						rightok = false;
						break;
					}
					lval = notes_timedomain[z][k];
				}
				double delta = 0.5 * ((value - notes_timedomain[i
						- peakdetect_count][k]) + (value - notes_timedomain[i
						+ peakdetect_count][k]));
				int minimum_after_hit = -1;
				if (rightok && leftok && value > 10) {

					for (minimum_after_hit = i; minimum_after_hit < totalsteps; minimum_after_hit++) {
						double mvalue = notes_timedomain[minimum_after_hit][k];
						double mvalue_prev = notes_timedomain[minimum_after_hit - 1][k];
						double mvalue_prev2 = notes_timedomain[minimum_after_hit - 2][k];
						double mvalue_next = notes_timedomain[minimum_after_hit + 1][k];
						double mvalue_next2 = notes_timedomain[minimum_after_hit + 2][k];
						if (mvalue < mvalue_prev && mvalue_prev2 < mvalue_prev
								&& mvalue < mvalue_next
								&& mvalue_next2 < mvalue_next) {
							// Punto di minimo = fine nota
							break;
						}
						if (mvalue < value/2.0) {
							// Fine nota
							break;
						}
					}

					int t = time_ms;
					int duration = (minimum_after_hit - i) * step_ms;

					System.out.println("Note " + NoteHit.notes_names[k] + " hit on " + time
							+ " value=" + value + " d=" + (duration) + "delta="
							+ delta);
					if (notes_hit.containsKey(time_ms - step_ms)) {
						t = time_ms - step_ms;
					}
					if (notes_hit.containsKey(time_ms + step_ms)) {
						t = time_ms + step_ms;
					}
					if (notes_hit.containsKey(time_ms - step_ms * 2)) {
						t = time_ms - step_ms * 2;
					}
					if (notes_hit.containsKey(time_ms + step_ms * 2)) {
						t = time_ms + step_ms * 2;
					}

					boolean merged = false;
					// Check overlap

					if (last_notes.containsKey(k)) {

						NoteHit last = last_notes.get(k);

						if (last.getEndTime() >= t) {
							System.out.print("Resolving overlap...");
							int oldd = last.getDuration();
							last.setDuration(last.getDuration()
									- ((last.getEndTime() - t) + 75));
							System.out.println("OK ( durata vecchia:" + oldd
									+ ", durata nuova:" + last.getDuration());
						}
					}

					// double t_sec = (double)t/1000.0;
					int t_old = t;
					t = ((int) t / (int) 75) * 75;
					if (!notes_hit.containsKey(t)) {
						notes_hit.put(t, new ArrayList<NoteHit>());
						times.add(t);
					}
					double intens = value;
					if (!merged) {
						NoteHit note = new NoteHit(k, t_old, duration,
								(int) Math.min(127, intens / 4.0),value);
						notes_hit.get(t).add(note);
						//note.center();
						last_notes.put(k, note);
					}
					/*
					 * time_ms += duration; time += (double)duration/1000.0; i =
					 * minimum_after_hit; continue;
					 */

				/*}*/
				time += 1.0 / step_count;
				time_ms = (int) (time * 1000.0);

			}
		}
		Collections.sort(times);
	}

	public void Filter() {

		int removedharmonics = 0;
		ArrayList<NoteHit> removelist = new ArrayList<NoteHit>();
		
		/*System.out.println("Rimozione note non esistenti");
		int note_time[] = new int[NoteHit.notes_freqs.length];
		double note_currmag[] = new double[NoteHit.notes_freqs.length];
		NoteHit note_h[] = new NoteHit[NoteHit.notes_freqs.length];
		
		int lasttime = 0;
		for (int i = 0; i < times.size(); i++) {
			for ( int b = 0; b < note_time.length; b++)
				note_time[b] -= times.get(i)-lasttime;
			lasttime = times.get(i);
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				
				NoteHit hit1 = notes_hit.get(times.get(i)).get(k);
				int nid = hit1.getNoteid();
				/*int freq = (int) hit1.getFreq();
				int second = hit1.getMstime()/1000;
				double maxmag = -1;
				for ( int z = -5; z < 6; z++ )
				{
					double real = segmentfft[second][(freq+z)*2+0];
					double imag = segmentfft[second][(freq+z)*2+1];
					double mag = Math.sqrt((real*real)+(imag*imag));
					System.out.println("Note "+hit1.toString()+" mag="+mag);
					
					if ( mag > maxmag )
						maxmag = mag;
				}*/
				/*int z = 1;
				int u = -1;
				if ( note_h.length-1 == nid )
					z = 0;
				if ( nid == 0)
					u = 0;
				for ( int n = nid-u; n < nid+1+z; n++)
				{
					if ( hit1.getNoteid() == n )
						continue;
					if ( note_h[n] != null && Math.abs(note_time[n]-note_h[n].getDuration()) < 100 )//Nota sovrapposta
					{
						if ( note_h[n].getMag() > hit1.getMag() )//La nota già eseguita adiacente ha magnitudine maggiore
						{
							removelist.add(hit1);
							System.out.println("Remove "+hit1);
						}else{//La nota da eseguire ha magnitudine superiore alla nota in corso
							removelist.add(note_h[nid]);
							System.out.println("Remove "+note_h[nid]);
						}
						
					}
					
					
					
				}
				
				note_time[nid] = hit1.getDuration();
				note_h[nid] = hit1;
				note_currmag[nid] = hit1.getMag();
			}
			
			/*for (int o = 0; o < removelist.size(); o++) {
				notes_hit.get(times.get(i)).remove(removelist.get(o));
			}
			removelist.clear();*//*
		}*/
		/*for (int i = 0; i < times.size(); i++) {
			System.out.println("Removing invalid notes... ("+times.get(i)+") "+notes_hit.get(times.get(i)).size());
			notes_hit.get(times.get(i)).removeAll(removelist);
			System.out.println("Removed invalid notes ("+times.get(i)+") "+notes_hit.get(times.get(i)).size());
		}*/
		removelist.clear();
		System.out.println("Rimozione armoniche...");
		
		
		
		for (int i = 0; i < times.size(); i++) {
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				NoteHit hit1 = notes_hit.get(times.get(i)).get(k);
				NoteHit fund = getFundamentalHit(hit1);
				hit1.setIntensity((int) ((hit1.getMag()/maxmag)*127));
				if ( fund != null )
				{
					removelist.add(hit1);
					System.err.println(hit1.toString()+" is an harmonic of "+fund.toString());
					continue;
				}
				if ( isCovered(hit1))
				{
					removelist.add(hit1);
					System.err.println(hit1.toString()+" is covered");
					continue;
				}
				if ( isPianoImperfection(hit1) )
				{
					removelist.add(hit1);		
					System.err.println(hit1.toString()+" is piano imperfection");
					continue;

				}
			}


		}
		for (int i = 0; i < times.size(); i++) {
			notes_hit.get(times.get(i)).removeAll(removelist);
		}
		System.out.printf("%d armoniche rimosse\nFiltraggio...",
				removedharmonics);
		removelist = new ArrayList<NoteHit>();
		/*for (int i = 0; i < times.size(); i++) {
			int maxintensity = Integer.MIN_VALUE;
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				NoteHit hit1 = notes_hit.get(times.get(i)).get(k);
				if (hit1.getIntensity() > maxintensity) {
					maxintensity = hit1.getIntensity();
				}
			}
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				NoteHit hit1 = notes_hit.get(times.get(i)).get(k);
				/*if (hit1.getIntensity() < 25)// Le
																		// frequenze
																		// basse
																		// non
																		// sono
																		// affidabili in quanto utilizzando una finstra stretta coprono 10 tasti
																		// per risolvere questo problema è necessario usare un'altra finestra più larga da 150 hz in giù
				{
					removelist.add(hit1);
					continue;
				}*/
				/*if (maxintensity / hit1.getIntensity() > 1.6)
					removelist.add(hit1);
			}
			for (int o = 0; o < removelist.size(); o++) {
				notes_hit.get(times.get(i)).remove(removelist.get(o));
			}
			removelist.clear();
		}*/

		System.out.println("Risoluzione di note sovrapposte...");
		long notestatus[] = new long[NoteHit.notes_freqs.length];
		NoteHit lastnotes_hit[] = new NoteHit[NoteHit.notes_freqs.length];
		for (int i = 0; i < notestatus.length; i++) {
			notestatus[i] = 0;
		}
		int lasttime = 0;
		for (int i = 0; i < times.size(); i++) {
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				NoteHit hit1 = notes_hit.get(times.get(i)).get(k);
				int nid = hit1.getNoteid();
				if (notestatus[nid] > 0 && lastnotes_hit[nid] != null) {
					// removelist.add(hit1);

					lastnotes_hit[nid].setEndTime(hit1.getMstime());
					System.out.println("Risolto conflitto nota "
							+ hit1.toString() + " con "
							+ lastnotes_hit[nid].toString());
					notestatus[nid] = hit1.getDuration();
					lastnotes_hit[nid] = hit1;

					continue;
				}
				notestatus[nid] = hit1.getDuration();
				lastnotes_hit[nid] = hit1;
			}
			for (int u = 0; u < notestatus.length; u++) {
				notestatus[u] -= (times.get(i) - lasttime);
			}
			lasttime = times.get(i);
			/*
			 * for (int o = 0; o < removelist.size(); o++) {
			 * notes_hit.get(times.get(i)).remove(removelist.get(o)); }
			 * removelist.clear();
			 */
		}

	}
	private boolean isPianoImperfection(NoteHit hit1)
	{
		int start = hit1.getMstime()-31;
		int end = start+61;
		for (int i = 0; i < times.size(); i++) {
			if ( times.get(i) >= start && times.get(i) <= end)
			{
				for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
					NoteHit hit2 = notes_hit.get(times.get(i)).get(k);
					if ( Math.abs(hit2.getNoteid()-hit1.getNoteid()) == 1 && hit1.getMag() < hit2.getMag())
					{
						return true;
						
					}
					
				}
			}
		}
		return false;
	}
	private boolean isCovered(NoteHit hit1)
	{
		int start = hit1.getMstime()-100;
		int end = start+200;
		for (int i = 0; i < times.size(); i++) {
			if ( times.get(i) >= start && times.get(i) <= end)
			{
				for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
					NoteHit hit2 = notes_hit.get(times.get(i)).get(k);
					
					if ( hit2.getMag()/2.0 > hit1.getMag() && hit2.getNoteid() != hit1.getNoteid() )
					{
						return true;
					}
					/*if ( Math.abs(hit2.getNoteid()-hit1.getNoteid()) < 3 && hit2.getMag() > hit1.getMag() && hit2.getMstime() - hit1.getMstime() > 50 && hit2.getMstime() - hit1.getMstime() < 300
							&& hit2.getDuration() > hit1.getDuration())
					{
						return true;
						
					}*/
				}
			}
		}
		return false;
		
	}
	private NoteHit getFundamentalHit(NoteHit hit1)
	{
		int start = hit1.getMstime()-150;
		int end = start+300;
		for (int i = 0; i < times.size(); i++) {
			if ( times.get(i) >= start && times.get(i) <= end)
			{
				for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
					NoteHit hit2 = notes_hit.get(times.get(i)).get(k);
					if (hit1.isHarmonicOf(hit2))
						return hit2;
				}
			}
		}
		return null;
	}
	public void WriteMidi() throws IOException {

		Sequence s = null;
		MetaMessage mt;
		MidiEvent me;
		SysexMessage sm;
		Track t = null;
		Track t2 = null;
		try {
			// 8 = notes*.length-1
			// 9 = notes*.length-2
			// 119 = 0

			s = new Sequence(javax.sound.midi.Sequence.PPQ, 100);
			t = s.createTrack();
			t2 = s.createTrack();
			byte[] b = { (byte) 0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte) 0xF7 };
			sm = new SysexMessage();
			sm.setMessage(b, 6);
			me = new MidiEvent(sm, (long) 0);
			t.add(me);
			t2.add(me);
			/*
			 * mt = new MetaMessage(); byte[] bt = {0x01, (byte)0x86,
			 * (byte)0xa0}; mt.setMessage(0x51 ,bt, 3); me = new
			 * MidiEvent(mt,(long)0); t.add(me);
			 */
			mt = new MetaMessage();
			String TrackName = new String("Piano");
			mt.setMessage(0x03, TrackName.getBytes(), TrackName.length());
			me = new MidiEvent(mt, (long) 0);
			t.add(me);
			t2.add(me);
			ShortMessage mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7D, 0x00);
			me = new MidiEvent(mm, (long) 0);
			t.add(me);
			t2.add(me);
			mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7F, 0x00);
			me = new MidiEvent(mm, (long) 0);
			t.add(me);
			t2.add(me);
			mm = new ShortMessage();
			mm.setMessage(0xC0, 0x00, 0x00);
			me = new MidiEvent(mm, (long) 0);
			t.add(me);
			t2.add(me);

		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long note_midi_times[] = new long[NoteHit.notes_freqs.length];
		for (int i = 0; i < note_midi_times.length; i++)
			note_midi_times[i] = -1;
		int notecount = 0;
		for (int i = 0; i < times.size(); i++) {
			System.out.print(times.get(i) + " : ");
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				Track dtrack = t;
				NoteHit nota = notes_hit.get(times.get(i)).get(k);
				if (nota.getDuration() == 0)
					continue;
				if (nota.isLeftHand())
					dtrack = t2;
				byte notamidi = (byte) (108 - nota.getNoteid());
				if (note_midi_times[nota.getNoteid()] < (long) (nota
						.getMstime() / 5)) {
					ShortMessage mm = new ShortMessage();

					try {
						mm.setMessage(0x90, notamidi,
								(byte) (nota.getIntensity()) & 0x7F);
					} catch (InvalidMidiDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					notecount++;
					me = new MidiEvent(mm, (long) (nota.getMstime() / 5));

					dtrack.add(me);
				}
				ShortMessage mm = new ShortMessage();
				try {
					mm.setMessage(0x80, notamidi, 0x20);
				} catch (InvalidMidiDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				note_midi_times[nota.getNoteid()] = (long) (nota.getEndTime() / 5);
				me = new MidiEvent(mm, (long) (nota.getEndTime()) / 5);
				dtrack.add(me);
				System.out.print(" "
						+ notes_hit.get(times.get(i)).get(k).toString());

			}
			System.out.println();
		}
		mt = new MetaMessage();
		byte[] bet = {}; // empty array
		try {
			mt.setMessage(0x2F, bet, 0);
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		me = new MidiEvent(mt, (long) 140);
		t.add(me);
		t2.add(me);
		System.out.println("" + notecount + " Notes");
		File f = new File(midifilename);
		MidiSystem.write(s, 1, f);
	}

	public void abort() {
		// TODO Auto-generated method stub
		this.stopping = true;
	}

	ArrayList<NoteHit> getNoteHits(int noteid) {
		ArrayList<NoteHit> hits = new ArrayList<NoteHit>();
		for (int i = 0; i < times.size(); i++) {
			for (int k = 0; k < notes_hit.get(times.get(i)).size(); k++) {
				NoteHit nota = notes_hit.get(times.get(i)).get(k);
				if (nota.getNoteid() == noteid)
					hits.add(nota);
			}
		}
		return hits;
	}

}
