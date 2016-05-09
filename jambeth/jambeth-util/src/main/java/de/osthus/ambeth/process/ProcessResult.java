package de.osthus.ambeth.process;


public class ProcessResult
{
	public final String out;

	public final String err;

	public final int result;

	public ProcessResult(String out, String err, int result)
	{
		super();
		this.out = out;
		this.err = err;
		this.result = result;
	}
}
