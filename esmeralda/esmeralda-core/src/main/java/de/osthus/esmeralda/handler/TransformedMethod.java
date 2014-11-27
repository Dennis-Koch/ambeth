package de.osthus.esmeralda.handler;

import java.util.Arrays;

public class TransformedMethod implements ITransformedMethod
{
	protected String owner;

	protected String name;

	protected String[] argTypes;

	protected boolean isPropertyInvocation;

	protected boolean isStatic;

	private IMethodParameterProcessor parameterProcessor;

	public TransformedMethod(String owner, String name, String[] argTypes, boolean isPropertyInvocation, boolean isStatic)
	{
		this.owner = owner;
		this.name = name;
		this.argTypes = argTypes;
		this.isPropertyInvocation = isPropertyInvocation;
		this.isStatic = isStatic;
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

	@Override
	public boolean isPropertyInvocation()
	{
		return isPropertyInvocation;
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
