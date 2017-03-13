package com.koch.ambeth.util.exception;

public interface IExceptionHandler
{
	/**
	 * This method must not throw an exception of any type by itself. The calling convention expects a robust handler in this case. Any inconsistent state will
	 * be handled outside of the scope of the exception handler (before or after the call).
	 * 
	 * @param e
	 *            Throwable to handle robustly
	 */
	void handleException(Throwable e);
}
