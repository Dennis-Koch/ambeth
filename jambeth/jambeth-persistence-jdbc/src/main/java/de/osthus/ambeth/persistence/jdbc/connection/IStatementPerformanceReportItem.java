package de.osthus.ambeth.persistence.jdbc.connection;

public interface IStatementPerformanceReportItem
{
	int getExecutionCount();

	double getSpentTimePerExecution();

	double getSpentTimePerCursor();

	String getStatement();
}
