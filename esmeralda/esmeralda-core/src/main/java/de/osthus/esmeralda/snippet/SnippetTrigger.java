package de.osthus.esmeralda.snippet;

public class SnippetTrigger extends RuntimeException
{
	private static final long serialVersionUID = 3058855403144819674L;

	private final String label;

	public SnippetTrigger(String label)
	{
		this.label = label;
	}

	public SnippetTrigger(String label, Throwable e)
	{
		super(e);
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}
}
