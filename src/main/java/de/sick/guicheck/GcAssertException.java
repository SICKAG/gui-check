// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck;

/**
 * Exception thrown when "normal/expected" evaluations fail.
 * 
 * @see GcException
 * @author linggol (created)
 */
public class GcAssertException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public GcAssertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GcAssertException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GcAssertException(String message)
	{
		super(message);
	}
}
