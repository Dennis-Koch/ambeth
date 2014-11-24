package de.osthus.esmeralda.handler;

import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import demo.codeanalyzer.common.model.Field;

public class Variable implements IVariable
{
	protected final String name;

	protected final String type;

	public Variable(Field field)
	{
		name = field.getName();
		type = field.getFieldType().toString();
	}

	public Variable(JCVariableDecl variableDecl)
	{
		name = variableDecl.getName().toString();
		type = variableDecl.getType().toString();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return getType() + " " + getName();
	}
}
