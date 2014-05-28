package de.osthus.ambeth.orm;

public class ExternalLinkConfig extends LinkConfig implements ILinkConfig
{
	private String sourceColumn;

	private String targetMember;

	public ExternalLinkConfig(String source)
	{
		super(source);
	}

	public String getSourceColumn()
	{
		return sourceColumn;
	}

	public void setSourceColumn(String sourceColumn)
	{
		this.sourceColumn = sourceColumn;
	}

	public String getTargetMember()
	{
		return targetMember;
	}

	public void setTargetMember(String targetMember)
	{
		this.targetMember = targetMember;
	}
}
