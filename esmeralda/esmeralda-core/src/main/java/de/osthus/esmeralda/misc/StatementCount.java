package de.osthus.esmeralda.misc;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class StatementCount
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String name = null;

	protected int statements = 0;

	protected int untranslatableStatements = 0;

	public StatementCount()
	{
	}

	public StatementCount(String name)
	{
		this.name = name;
	}

	public int getStatements()
	{
		return statements;
	}

	public void setStatements(int statements)
	{
		this.statements = statements;
	}

	public int getUntranslatableStatements()
	{
		return untranslatableStatements;
	}

	public void setUntranslatableStatements(int untranslatableStatements)
	{
		this.untranslatableStatements = untranslatableStatements;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Conversion metrics");
		if (name != null)
		{
			sb.append(" for ").append(name);
		}
		if (statements > 0)
		{
			int converted = statements - untranslatableStatements;
			double percentage = (converted * 10000 / statements) / 100.;
			sb.append(": " + converted + " of " + statements + " statements automatically converted, that is " + percentage + "%");
		}
		else
		{
			sb.append(" is empty");
		}

		return sb.toString();
	}
}
