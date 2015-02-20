package de.osthus.esmeralda.snippet;

public class SnippetTrigger extends RuntimeException
{
	private static final long serialVersionUID = 3058855403144819674L;

	private String context;

	public SnippetTrigger(String message)
	{
		super(message);
	}

	public SnippetTrigger(String message, Throwable cause)
	{
		super(message, cause);
	}

	public String getContext()
	{
		return context;
	}

	public SnippetTrigger setContext(String context)
	{
		this.context = context;
		return this;
	}
}
