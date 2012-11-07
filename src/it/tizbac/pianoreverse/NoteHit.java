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

public class NoteHit {
	private double mag;
	private int noteid,duration,mstime,intensity,origduration;
	private static int notes_duration_120bpm[] = {31,72,125,250,500,1000,2000,4000,8000};
	public static double notes_freqs[] = { 4186.01, 3951.07, 3729.31, 3520, 3322.44,
			3135.96, 2959.96, 2793.83, 2637.02, 2489.02, 2349.32, 2217.46,
			2093, 1975.53, 1864.66, 1760, 1661.22, 1567.98, 1479.98,
			1396.91, 1318.51, 1244.51, 1174.66, 1108.73, 1046.5, 987.767,
			932.328, 880, 830.609, 783.991, 739.989, 698.456, 659.255,
			622.254, 587.33, 554.365, 523.251, 493.883, 466.164, 440,
			415.305, 391.995, 369.994, 349.228, 329.628, 311.127, 293.665,
			277.183, 261.626, 246.942, 233.082, 220, 207.652, 195.998,
			184.997, 174.614, 164.814, 155.563, 146.832, 138.591, 130.813,
			123.471, 116.541, 110, 103.826, 97.9989, 92.4986, 87.3071,
			82.4069, 77.7817, 73.4162, 69.2957, 65.4064, 61.7354, 58.2705,
			55, 51.9131, 48.9994, 46.2493, 43.6535, 41.2034, 38.8909,
			36.7081, 34.6478, 32.7032, 30.8677, 29.1352, 27.5 };
	public static String notes_names[] = { "C8 Eighth octave", "B7", "A♯7/B♭7", "A7",
			"G♯7/A♭7", "G7", "F♯7/G♭7", "F7", "E7", "D♯7/E♭7", "D7",
			"C♯7/D♭7", "C7 Double high C", "B6", "A♯6/B♭6", "A6",
			"G♯6/A♭6", "G6", "F♯6/G♭6", "F6", "E6", "D♯6/E♭6", "D6",
			"C♯6/D♭6", "C6 Soprano C (High C)", "B5", "A♯5/B♭5", "A5",
			"G♯5/A♭5", "G5", "F♯5/G♭5", "F5", "E5", "D♯5/E♭5", "D5",
			"C♯5/D♭5", "C5 Tenor C", "B4", "A♯4/B♭4", "A4 A440", "G♯4/A♭4",
			"G4", "F♯4/G♭4", "F4", "E4", "D♯4/E♭4", "D4", "C♯4/D♭4",
			"C4 Middle C", "B3", "A♯3/B♭3", "A3", "G♯3/A♭3", "G3",
			"F♯3/G♭3", "F3", "E3", "D♯3/E♭3", "D3", "C♯3/D♭3", "C3 Low C",
			"B2", "A♯2/B♭2", "A2", "G♯2/A♭2", "G2", "F♯2/G♭2", "F2", "E2",
			"D♯2/E♭2", "D2", "C♯2/D♭2", "C2 Deep C", "B1", "A♯1/B♭1", "A1",
			"G♯1/A♭1", "G1", "F♯1/G♭1", "F1", "E1", "D♯1/E♭1", "D1",
			"C♯1/D♭1", "C1 Pedal C", "B0", "A♯0/B♭0", "A0 Double Pedal A" };
	public NoteHit() {
		// TODO Auto-generated constructor stub
		noteid = 0;
		duration = 500;
		mstime = 0;
		intensity = 127;
		origduration = 500;
	}
	public NoteHit(int noteid, int mstime, int duration , int intensity,double mag)
	{
		this.noteid = noteid;
		this.mstime = mstime;
		if ( this.mstime < 0 )
			this.mstime = 0; //Broken notes 
		/*this.mstime /= 75;
		this.mstime *= 75;*/
		this.duration = 8000;
		this.intensity = intensity;
		this.mag = mag;
		this.origduration = duration;
		for ( int x = 0; x < notes_duration_120bpm.length; x++ )
		{
			if ( duration <= notes_duration_120bpm[x])
			{
				this.duration = notes_duration_120bpm[x];
				break;
						
			}
		}
	}
	public int getDuration() {
		return duration;
	}
	public int getIntensity() {
		return intensity;
	}
	public int getMstime() {
		return mstime;
	}
	public void setDuration(int duration) {
		this.origduration = duration;
		for ( int x = 0; x < notes_duration_120bpm.length; x++ )
		{
			if ( duration <= notes_duration_120bpm[x])
			{
				this.duration = notes_duration_120bpm[x];
				break;
						
			}
		}
	}
	public void setEndTime(int ems)
	{
		setDuration(ems-getMstime());
	}
	public void setNoteid(int noteid) {
		this.noteid = noteid;
	}
	public void setIntensity(int intensity) {
		this.intensity = intensity & 0x7F;
	}
	public int getNoteid() {
		return noteid;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "("+notes_names[noteid]+", Intensity: "+intensity+" , Starttime: "+getMstime()+", Durata:"+getDuration()+";"+origduration+")";
	}
	public void setMstime(int mstime) {
		this.mstime = mstime;
	}
	public int getEndTime()
	{
		return mstime + duration;
	}
	public boolean isHarmonicOf(NoteHit note2)
	{
		int n = getNoteid()%12;
		int n2 = note2.getNoteid()%12;
		if ( note2 == this )
			return false;
		/*if ( Math.abs(note2.getNoteid()-getNoteid()) > 12 )
			return false;*/
		if (n == n2 && ((double)note2.getMag()/ (double)getMag()) > 1.5 )
		{
			return true;
		}
		return false;
	}
	public boolean isLeftHand()
	{
		return getNoteid() > notes_names.length/2;
	}
	public double getFreq()
	{
		return notes_freqs[getNoteid()];
	}
	public boolean isPressed(int timeMS) {
		
		return timeMS >= getMstime() && timeMS <= getEndTime();
	}
	public void center() {
		setMstime(getMstime()-getDuration()/2);
		setDuration(getDuration()+getDuration()/2);
		
	}
	public double getMag() {
		// TODO Auto-generated method stub
		return mag;
	}
}
