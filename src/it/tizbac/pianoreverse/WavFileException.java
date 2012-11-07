package it.tizbac.pianoreverse;
public class WavFileException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7658925515087154378L;

	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause) 
	{
		super(cause);
	}
}