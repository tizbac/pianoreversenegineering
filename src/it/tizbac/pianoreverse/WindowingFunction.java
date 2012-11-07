package it.tizbac.pianoreverse;

public class WindowingFunction {
	private double[] coefficients;
	private double l;
	public WindowingFunction(int count,double l) {
		coefficients = new double[count];
		this.l = l;
		for ( int i = 0; i < count; i++ )
		{
			double x = (double)i/(double)(count);
			x*=2.0;
			x-=1.0;
			coefficients[i] = Math.pow(Math.E,(-Math.pow((x-0), 2)*this.l));
		}
	}
	public double GetCoeff(int index)
	{
		return coefficients[index];
	}
	
}
