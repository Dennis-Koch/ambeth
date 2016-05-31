package de.osthus.ambeth.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.AbstractTuple2KeyHashMap;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyExpansionProvider implements IPropertyExpansionProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected Tuple2KeyHashMap<Class<?>, String, PropertyExpansion> propertyExpansionCache = new Tuple2KeyHashMap<Class<?>, String, PropertyExpansion>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public PropertyExpansion getPropertyExpansion(Class<?> entityType, String propertyPath)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(propertyPath, "propertyPath");

		PropertyExpansion propertyExpansion = propertyExpansionCache.get(entityType, propertyPath);
		if (propertyExpansion != null)
		{
			return propertyExpansion;
		}
		// we have a cache miss. create the propertyExpansion
		propertyExpansion = getPropertyExpansionIntern(entityType, propertyPath);
		if (propertyExpansion == null)
		{
			return propertyExpansion;
		}
		// here: COPY-ON-WRITE pattern to be threadsafe with reads (above) without a lock. This makes sense because after a limited "warmup" phase there will
		// not be a cache miss any more with further runtime
		writeLock.lock();
		try
		{
			Tuple2KeyHashMap<Class<?>, String, PropertyExpansion> propertyExpansionCache = new Tuple2KeyHashMap<Class<?>, String, PropertyExpansion>(
					(int) (this.propertyExpansionCache.size() / AbstractTuple2KeyHashMap.DEFAULT_LOAD_FACTOR) + 2);
			propertyExpansionCache.putAll(this.propertyExpansionCache);
			if (!propertyExpansionCache.putIfNotExists(entityType, propertyPath, propertyExpansion))
			{
				return propertyExpansionCache.get(entityType, propertyPath);
			}
			this.propertyExpansionCache = propertyExpansionCache;
			return propertyExpansion;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected PropertyExpansion getPropertyExpansionIntern(Class<?> entityType, String propertyPath)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		if (entityType == null)
		{
			return null;
		}

		List<String> path = getPath(propertyPath);
		Member[] memberPath = new Member[path.size()];
		IEntityMetaData[] metaDataPath = new IEntityMetaData[path.size()];
		Class<?> lastType = null;
		for (int a = 0, size = path.size(); a < size; a++)
		{
			String pathToken = path.get(a);
			if (metaData == null)
			{
				throw new IllegalArgumentException("Could not find metaData for:" + (lastType == null ? "null" : lastType.toString()));
			}
			Member member = metaData.getMemberByName(pathToken);
			if (member == null)
			{
				throw new IllegalArgumentException("The provided propertyPath can not be resolved. Check: " + pathToken
						+ " (Hint: propertyNames need to start with an UpperCase letter!)");
			}
			memberPath[a] = member;
			// get next metaData
			metaData = entityMetaDataProvider.getMetaData(member.getRealType(), true);
			metaDataPath[a] = metaData;
			lastType = member.getRealType();
		}

		PropertyExpansion propertyExpansion = new PropertyExpansion(memberPath, metaDataPath);
		return propertyExpansion;
	}

	protected List<String> getPath(String propertyPath)
	{
		String[] pathTokens = propertyPath.split("\\.");
		if (pathTokens != null)
		{
			return Arrays.asList(pathTokens);
		}

		return Collections.emptyList();
	}
}
