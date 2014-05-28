package de.osthus.ambeth.testutil;

import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.connection.IPreparedStatementParamLogger;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class PreparedStatementParamLogger implements IPreparedStatementParamLogger, IInitializingBean
{
	private static final String NL = System.getProperty("line.separator");

	@LogInstance
	private ILogger log;

	protected final IList<ILinkedMap<Integer, Object>> params = new ArrayList<ILinkedMap<Integer, Object>>();

	protected ILinkedMap<Integer, Object> currentBatch = new LinkedHashMap<Integer, Object>();

	protected IConversionHelper conversionHelper;

	protected Set<Method> paramSetters;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(paramSetters, "paramSetters");
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setParamSetters(Set<Method> paramSetters)
	{
		this.paramSetters = paramSetters;
	}

	@Override
	public boolean isCallToBeLogged(Method method)
	{
		return paramSetters.contains(method);
	}

	@Override
	public void logParams(Method method, Object[] args)
	{
		if (!isCallToBeLogged(method))
		{
			return;
		}

		currentBatch.put((Integer) args[0], args[1]);
	}

	@Override
	public void addBatch()
	{
		params.add(currentBatch);
		currentBatch = new LinkedHashMap<Integer, Object>();
	}

	@Override
	public void doLog()
	{
		if (!log.isDebugEnabled() || currentBatch.isEmpty())
		{
			return;
		}

		StringBuilder sb = new StringBuilder();

		appendDataSet(sb, currentBatch);

		params.clear();
		currentBatch.clear();

		log.debug(sb.toString());
	}

	@Override
	public void doLogBatch()
	{
		if (!log.isDebugEnabled() || params.isEmpty())
		{
			return;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0, size = params.size(); i < size; i++)
		{
			appendDataSet(sb, params.get(i));
			if (i < size - 1)
			{
				sb.append(NL).append("\t");
			}
		}

		params.clear();
		currentBatch.clear();

		log.debug(sb.toString());
	}

	private void appendDataSet(StringBuilder sb, ILinkedMap<Integer, Object> dataSet)
	{
		sb.append("[ ");
		String separator = "";
		for (Entry<Integer, Object> entry : dataSet)
		{
			Integer index = entry.getKey();
			Object param = entry.getValue();

			String paramString = conversionHelper.convertValueToType(String.class, param);
			sb.append(separator).append(index).append(": ").append(paramString);
			separator = ", ";
		}
		sb.append(" ]");
	}
}
