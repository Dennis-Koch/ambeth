package de.osthus.esmeralda.handler;

import java.util.Arrays;

public class TransformedMethod implements ITransformedMethod
{
	protected final String owner;

	protected final String name;

	protected String[] argTypes;

	protected boolean isIndexedInvocation;

	protected boolean isPropertyInvocation;

	protected boolean isStatic;

	protected IMethodParameterProcessor parameterProcessor;

	protected final Boolean writeOwner;

	private boolean isOwnerAType;

	public TransformedMethod(String owner, String name, String[] argTypes, boolean isStatic)
	{
		this(owner, name, argTypes, isStatic, null, false);
	}

	public TransformedMethod(String owner, String name, String[] argTypes, boolean isStatic, Boolean writeOwner, boolean isOwnerAType)
	{
		this.owner = owner;
		this.name = name;
		this.argTypes = argTypes;
		this.isStatic = isStatic;
		this.writeOwner = writeOwner;
		this.isOwnerAType = isOwnerAType;
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
	public String[] getArgumentTypes()
	{
		return argTypes;
	}

	@Override
	public boolean isStatic()
	{
		return isStatic;
	}

	public void setStatic(boolean isStatic)
	{
		this.isStatic = isStatic;
	}

	@Override
	public Boolean isWriteOwner()
	{
		return writeOwner;
	}

	@Override
	public boolean isOwnerAType()
	{
		return isOwnerAType;
	}

	@Override
	public boolean isPropertyInvocation()
	{
		return isPropertyInvocation;
	}

	public void setPropertyInvocation(boolean isPropertyInvocation)
	{
		this.isPropertyInvocation = isPropertyInvocation;
	}

	@Override
	public boolean isIndexedInvocation()
	{
		return isIndexedInvocation;
	}

	public void setIndexedInvocation(boolean isIndexedInvocation)
	{
		this.isIndexedInvocation = isIndexedInvocation;
	}

	public void setParameterProcessor(IMethodParameterProcessor parameterProcessor)
	{
		this.parameterProcessor = parameterProcessor;
	}

	@Override
	public IMethodParameterProcessor getParameterProcessor()
	{
		return parameterProcessor;
	}

	@Override
	public String toString()
	{
		return getOwner() + "." + getName() + "(" + Arrays.toString(getArgumentTypes()) + ")";
	}
}
