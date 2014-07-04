// Copyright 2013 SICK AG. All rights reserved.
package de.sick.guicheck;

/**
 * Exception thrown by GUIcheck in all cases where its not a "normal" check for UI conditions.
 * 
 * @see GcAssertException
 * @author linggol (created)
 */
public class GcException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public GcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GcException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GcException(String message)
	{
		super(message);
	}
}
