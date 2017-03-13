package com.koch.ambeth.persistence.filter;

import java.lang.reflect.Array;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.filter.IQueryResultCacheItem;
import com.koch.ambeth.query.filter.IQueryResultRetriever;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IMap;

public class DefaultQueryResultRetriever implements IQueryResultRetriever
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IMap<Object, Object> currentNameToValueMap;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IQueryIntern<?> query;

	@Autowired
	protected ITransaction transaction;

	@Override
	public boolean containsPageOnly()
	{
		return currentNameToValueMap.containsKey(QueryConstants.PAGING_SIZE_OBJECT);
	}

	@Override
	public List<Class<?>> getRelatedEntityTypes()
	{
		ArrayList<Class<?>> relatedEntityTypes = new ArrayList<Class<?>>();
		query.fillRelatedEntityTypes(relatedEntityTypes);
		return relatedEntityTypes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQueryResultCacheItem getQueryResult()
	{
		return transaction.processAndCommit(new ResultingDatabaseCallback<IQueryResultCacheItem>()
		{
			@Override
			public IQueryResultCacheItem callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				IConversionHelper conversionHelper = DefaultQueryResultRetriever.this.conversionHelper;
				IQueryIntern<?> query = DefaultQueryResultRetriever.this.query;
				Class<?> entityType = query.getEntityType();
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				Member[] alternateIdMembers = metaData.getAlternateIdMembers();
				int length = alternateIdMembers.length + 1;

				ArrayList<Object>[] idLists = new ArrayList[length];
				Class<?> versionType = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;
				Class<?>[] idTypes = new Class[length];
				for (int a = length; a-- > 0;)
				{
					idLists[a] = new ArrayList<Object>();
					idTypes[a] = metaData.getIdMemberByIdIndex((byte) (a - 1)).getRealType();
				}
				ArrayList<Object> versionList = new ArrayList<Object>();

				IVersionCursor versionCursor = query.retrieveAsVersions(currentNameToValueMap, true);
				try
				{
					while (versionCursor.moveNext())
					{
						IVersionItem versionItem = versionCursor.getCurrent();
						for (int idIndex = length; idIndex-- > 0;)
						{
							Object id = conversionHelper.convertValueToType(idTypes[idIndex], versionItem.getId((byte) (idIndex - 1)));
							idLists[idIndex].add(id);
						}
						Object version = versionType != null ? conversionHelper.convertValueToType(versionType, versionItem.getVersion()) : null;
						versionList.add(version);
					}
					Object[] idArrays = new Object[length];
					for (int a = length; a-- > 0;)
					{
						idArrays[a] = convertListToArray(idLists[a], idTypes[a]);
					}
					Object versionArray = versionType != null ? convertListToArray(versionList, versionType) : null;
					return new QueryResultCacheItem(entityType, idLists[0].size(), idArrays, versionArray);
				}
				finally
				{
					versionCursor.dispose();
				}
			}
		});
	}

	protected Object convertListToArray(List<Object> list, Class<?> expectedItemType)
	{
		if (expectedItemType != null)
		{
			Class<?> unwrappedType = WrapperTypeSet.getUnwrappedType(expectedItemType);
			if (unwrappedType != null)
			{
				expectedItemType = unwrappedType;
			}
		}
		if (expectedItemType == null)
		{
			return list.toArray(new Object[list.size()]);
		}
		Object array = Array.newInstance(expectedItemType, list.size());
		for (int a = list.size(); a-- > 0;)
		{
			Array.set(array, a, list.get(a));
		}
		return array;
	}
}