package de.osthus.ambeth.persistence.jdbc.connection;

public interface IStatementPerformanceReport
{
	void reset();

	long getOverallDuration(boolean reset);

	void printTop(StringBuilder sb, boolean reset);
}
