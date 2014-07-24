package de.osthus.ambeth.persistence.jdbc.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.format.PaddingDecimalFormat;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.JDBCResultSet;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.DefaultStatementPerformanceLogger.StatementInfo;
import de.osthus.ambeth.sensor.IntervalInfo;
import de.osthus.ambeth.sensor.ReentrantIntervalSensor;

public class DefaultStatementPerformanceLogger extends ReentrantIntervalSensor<StatementInfo> implements IStatementPerformanceReport
{
	public static class StatementInfo extends IntervalInfo
	{
		protected final String sql;

		public StatementInfo(String sql, long startedTime)
		{
			super(startedTime);
			this.sql = sql;
		}
	}

	public static class StatementEntry
	{
		protected final String sql;

		protected int count;

		protected int duration, cursorDuration;

		public StatementEntry(String sql)
		{
			this.sql = sql;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected ITransaction transaction;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String[] schemaNames;

	protected final LinkedHashMap<String, StatementEntry> sqlToEntryMap = new LinkedHashMap<String, StatementEntry>();

	@Override
	protected StatementInfo createIntervalInfo(String sensorName)
	{
		return null;
	}

	@Override
	protected StatementInfo createIntervalInfo(String sensorName, Object[] additionalData)
	{
		StatementInfo statementInfo = new StatementInfo((String) additionalData[0], System.currentTimeMillis());
		return statementInfo;
	}

	@Override
	protected void handleFinishedIntervalInfo(String sensorName, StatementInfo intervalInfo, long endTime)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			String sql = intervalInfo.sql;
			StatementEntry statementEntry = sqlToEntryMap.get(sql);
			if (statementEntry == null)
			{
				statementEntry = new StatementEntry(sql);
				sqlToEntryMap.put(sql, statementEntry);
			}
			if (JDBCResultSet.SENSOR_NAME.equals(sensorName))
			{
				int cursorDuration = (int) (endTime - intervalInfo.getStartedTime());
				statementEntry.cursorDuration += cursorDuration;
				return;
			}
			int duration = (int) (endTime - intervalInfo.getStartedTime());
			statementEntry.duration += duration;
			statementEntry.count++;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void reset()
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			sqlToEntryMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public long getOverallDuration(boolean reset)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			int durationSum = 0;

			for (Entry<String, StatementEntry> entry : sqlToEntryMap)
			{
				durationSum += entry.getValue().duration;
			}
			if (reset)
			{
				sqlToEntryMap.clear();
			}
			return durationSum;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void printTop(StringBuilder sb, boolean reset)
	{
		IMap<String, StatementEntry> sqlToRemoteEntryMap = joinRemoteDatabaseInfo(schemaNames);
		final long currentTime;
		final LinkedHashMap<String, StatementEntry> sqlToEntryMap = new LinkedHashMap<String, StatementEntry>();
		ArrayList<StatementInfo> statementInfos = new ArrayList<StatementInfo>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			currentTime = System.currentTimeMillis();
			for (Entry<String, StatementEntry> entry : this.sqlToEntryMap)
			{
				StatementEntry value = entry.getValue();
				StatementEntry clone = new StatementEntry(value.sql);
				clone.count = value.count;
				clone.cursorDuration = value.cursorDuration;
				clone.duration = value.duration;
				sqlToEntryMap.put(entry.getKey(), clone);
			}
			for (int a = 0, size = sharedInfos.size(); a < size; a++)
			{
				StatementInfo statementInfo = sharedInfos.get(a);
				StatementInfo clone = new StatementInfo(statementInfo.sql, statementInfo.getStartedTime());
				sharedInfos.add(clone);
			}
			if (reset)
			{
				this.sqlToEntryMap.clear();
			}
		}
		finally
		{
			writeLock.unlock();
		}
		ISet<String> keys = sqlToEntryMap.keySet();
		final LinkedHashMap<String, IList<StatementInfo>> sqlToStatementInfosMap = new LinkedHashMap<String, IList<StatementInfo>>();
		for (int a = statementInfos.size(); a-- > 0;)
		{
			StatementInfo statementInfo = statementInfos.get(a);
			String sql = statementInfo.sql;
			IList<StatementInfo> sis = sqlToStatementInfosMap.get(sql);
			if (sis == null)
			{
				sis = new ArrayList<StatementInfo>();
				sqlToStatementInfosMap.put(sql, sis);
			}
			sis.add(statementInfo);
			keys.add(sql);
		}
		IList<String> keysList = keys.toList();
		Collections.sort(keysList, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				StatementEntry se1 = sqlToEntryMap.get(o1);
				List<StatementInfo> sis1 = sqlToStatementInfosMap.get(o1);
				StatementEntry se2 = sqlToEntryMap.get(o2);
				List<StatementInfo> sis2 = sqlToStatementInfosMap.get(o2);
				double d1 = se1 != null ? calculateDurationPerRequest(se1) : 0;
				double d2 = se2 != null ? calculateDurationPerRequest(se2) : 0;
				if (sis1 != null)
				{
					for (int a = sis1.size(); a-- > 0;)
					{
						d1 += (currentTime - sis1.get(a).getStartedTime());
					}
				}
				if (sis2 != null)
				{
					for (int a = sis2.size(); a-- > 0;)
					{
						d2 += (currentTime - sis2.get(a).getStartedTime());
					}
				}
				if (Math.abs(d1 - d2) < 0.00001)
				{
					return 0;
				}
				return d1 < d2 ? 1 : -1;
			}
		});
		DecimalFormat floatDF = new PaddingDecimalFormat("######0.00");
		DecimalFormat integerDF = new PaddingDecimalFormat("#######");

		String ignoreSql = getRemoteDatabaseInfoSql(schemaNames);
		for (int a = 0, size = keysList.size(); a < size; a++)
		{
			String sql = keysList.get(a);
			if (sql.equals(ignoreSql))
			{
				continue;
			}
			StatementEntry se = sqlToEntryMap.get(sql);
			List<StatementInfo> sis = sqlToStatementInfosMap.get(sql);
			int count = (se != null ? se.count : 0) + (sis != null ? sis.size() : 0);
			int duration = (se != null ? se.duration : 0);
			int cursorDuration = (se != null ? se.cursorDuration : 0);
			if (sis != null)
			{
				for (int b = sis.size(); b-- > 0;)
				{
					duration += (currentTime - sis.get(b).getStartedTime());
				}
			}
			if (a > 0)
			{
				sb.append(System.getProperty("line.separator"));
			}
			sb.append(integerDF.format(a + 1)).append(") ").append(integerDF.format(count)).append(" ")
					.append(floatDF.format(calculateDurationPerRequest(duration, count))).append(" ")
					.append(floatDF.format(calculateDurationPerRequest(cursorDuration, count)));

			StatementEntry remoteEntry = sqlToRemoteEntryMap.get(sql);
			if (remoteEntry != null)
			{
				sb.append(" ").append(integerDF.format(remoteEntry.count)).append(" ").append(floatDF.format(calculateDurationPerRequest(remoteEntry)));
			}
			else
			{
				sb.append("       -        -   ");
			}
			sb.append(" ").append(sql);
		}
	}

	protected double calculateDurationPerRequest(StatementEntry statementEntry)
	{
		return calculateDurationPerRequest(statementEntry.duration, statementEntry.count);
	}

	protected double calculateDurationPerRequest(long duration, int count)
	{
		return (long) (100 * duration / (double) count) / 100.0;
	}

	protected String getRemoteDatabaseInfoSql(String[] schemaNames)
	{
		return "SELECT sql_text,cpu_time/1000000 cpu_time,elapsed_time/1000000 elapsed_time,executions FROM v_$sqlarea WHERE parsing_schema_name IN (?) AND module='JDBC Thin Client' ORDER BY executions DESC";
	}

	protected IMap<String, StatementEntry> joinRemoteDatabaseInfo(final String... schemaNames)
	{
		final LinkedHashMap<String, StatementEntry> statementInfoMap = new LinkedHashMap<String, StatementEntry>();

		final String sql = getRemoteDatabaseInfoSql(schemaNames);

		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				PreparedStatement pstm = null;
				ResultSet rs = null;
				try
				{
					pstm = connection.prepareStatement(sql);
					String[] uppercaseSchemaNames = new String[schemaNames.length];
					for (int a = schemaNames.length; a-- > 0;)
					{
						uppercaseSchemaNames[a] = schemaNames[a].toUpperCase();
					}
					pstm.setObject(1, uppercaseSchemaNames);
					rs = pstm.executeQuery();
					while (rs.next())
					{
						String sql = rs.getString("sql_text");
						sql = sql.replaceAll(":\\d+ ", Matcher.quoteReplacement("?"));
						int elapsedTime = rs.getInt("elapsed_time");
						int executions = rs.getInt("executions");
						StatementEntry statementInfo = new StatementEntry(sql);
						statementInfo.count = executions;
						statementInfo.duration = elapsedTime;
						statementInfoMap.put(sql, statementInfo);
					}
				}
				finally
				{
					JdbcUtil.close(pstm, rs);
				}
			}
		});
		return statementInfoMap;
	}
}
