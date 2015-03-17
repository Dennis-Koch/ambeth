package de.osthus.esmeralda.handler;


public class TransformedMemberAccess implements ITransformedField
{
	protected final String owner, name, returnType;

	public TransformedMemberAccess(String owner, String name, String returnType)
	{
		this.owner = owner;
		this.name = name;
		this.returnType = returnType;
	}

	@Override
	public String getOwner()
	{
		return owner;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getReturnType()
	{
		return returnType;
	}

	@Override
	public String toString()
	{
		return getReturnType() + " " + getOwner() + "." + getName();
	}
}
