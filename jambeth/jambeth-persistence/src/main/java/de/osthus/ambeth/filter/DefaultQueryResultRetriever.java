package de.osthus.ambeth.filter;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.query.IQueryIntern;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.ParamChecker;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class DefaultQueryResultRetriever implements IQueryResultRetriever, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConversionHelper conversionHelper;

	protected Map<Object, Object> currentNameToValueMap;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IQueryIntern<?> query;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
		ParamChecker.assertNotNull(currentNameToValueMap, "CurrentNameToValueMap");
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(query, "Query");
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setCurrentNameToValueMap(Map<Object, Object> currentNameToValueMap)
	{
		this.currentNameToValueMap = currentNameToValueMap;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setQuery(IQueryIntern<?> query)
	{
		this.query = query;
	}

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
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IQueryResultCacheItem getQueryResult()
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IQueryIntern<?> query = this.query;
		Class<?> entityType = query.getEntityType();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITypeInfoItem[] alternateIdMembers = metaData.getAlternateIdMembers();
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

		IVersionCursor versionCursor = query.retrieveAsVersions(currentNameToValueMap);
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

	protected Object convertListToArray(List<Object> list, Class<?> expectedItemType)
	{
		if (expectedItemType != null)
		{
			Class<?> unwrappedType = ImmutableTypeSet.getUnwrappedType(expectedItemType);
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