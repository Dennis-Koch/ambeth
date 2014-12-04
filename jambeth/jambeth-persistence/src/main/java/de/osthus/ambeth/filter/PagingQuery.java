package de.osthus.ambeth.filter;

import java.util.Collections;
import java.util.Map;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.PagingRequest;
import de.osthus.ambeth.filter.model.PagingResponse;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.query.IQueryIntern;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.query.config.QueryConfigurationConstants;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.util.StringBuilderUtil;

public class PagingQuery<T> implements IPagingQuery<T>, IPagingQueryIntern<T>
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IQueryIntern<Object> query;

	@Autowired
	protected IQueryResultCache queryResultCache;

	@Property(name = QueryConfigurationConstants.PagingPrefetchBehavior, defaultValue = "false")
	protected boolean prefetchAllPages;

	@Override
	public void dispose()
	{
		beanContext = null;
		cache = null;
		entityMetaDataProvider = null;
		objectCollector = null;
		queryResultCache = null;
		if (query != null)
		{
			query.dispose();
			query = null;
		}
	}

	@Override
	public IPagingRequest createRequest(int pageNumber, int sizePerPage)
	{
		PagingRequest request = new PagingRequest();
		request.setNumber(pageNumber);
		request.setSize(sizePerPage);
		return request;
	}

	@Override
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
	{
		return query.getQueryKey(nameToValueMap);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
		return retrieveIntern(pagingRequest, metaData.getIdMember().getName(), null);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName)
	{
		return retrieveIntern(pagingRequest, alternateIdPropertyName, null);
	}

	@Override
	public IPagingResponse<T> retrieve(IPagingRequest pagingRequest)
	{
		return retrieveIntern(pagingRequest, null, null);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, Map<Object, Object> paramMap)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
		return retrieveIntern(pagingRequest, metaData.getIdMember().getName(), paramMap);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> paramMap)
	{
		return retrieveIntern(pagingRequest, alternateIdPropertyName, paramMap);
	}

	@Override
	public IPagingResponse<T> retrieve(IPagingRequest pagingRequest, Map<Object, Object> paramMap)
	{
		return retrieveIntern(pagingRequest, null, paramMap);
	}

	protected IPagingResponse<T> retrieveIntern(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> nameToValueMap)
	{
		long start = System.currentTimeMillis();
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		HashMap<Object, Object> currentNameToValueMap = new HashMap<Object, Object>();
		if (nameToValueMap != null)
		{
			currentNameToValueMap.putAll(nameToValueMap);
		}
		PagingResponse<T> pagingResponse = new PagingResponse<T>();

		int offset, length;

		if (pagingRequest != null)
		{
			int number = pagingRequest.getNumber();
			length = pagingRequest.getSize();

			offset = number * length;

			if (!prefetchAllPages)
			{
				currentNameToValueMap.put(QueryConstants.PAGING_INDEX_OBJECT, new Integer(offset));
				currentNameToValueMap.put(QueryConstants.PAGING_SIZE_OBJECT, new Integer(length));
			}

			pagingResponse.setNumber(number);
		}
		else
		{
			offset = 0;
			length = -1;
		}
		pagingResponse.setTotalNumber(-1);
		pagingResponse.setTotalSize(-1);

		IQueryKey queryKey = query.getQueryKey(currentNameToValueMap);

		ParamHolder<Integer> totalSize = new ParamHolder<Integer>();

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(query.getEntityType());
		byte idIndex = alternateIdPropertyName == null ? ObjRef.PRIMARY_KEY_INDEX : metaData.getIdIndexByMemberName(alternateIdPropertyName);

		IBeanRuntime<DefaultQueryResultRetriever> queryResultRetrieverBC = beanContext.registerBean(DefaultQueryResultRetriever.class);
		queryResultRetrieverBC.propertyValue("Query", query);
		queryResultRetrieverBC.propertyValue("CurrentNameToValueMap", currentNameToValueMap);
		IQueryResultRetriever queryResultRetriever = queryResultRetrieverBC.finish();

		IList<IObjRef> queryRefResult = queryResultCache.getQueryResult(queryKey, queryResultRetriever, idIndex, offset, length, totalSize);

		pagingResponse.setTotalSize(totalSize.getValue().intValue());
		if (length <= 0)
		{
			// No Paging or zero-length paging (the latter is a rare usecase) means there is 1 page
			// with all data
			pagingResponse.setTotalNumber(1);
		}
		else
		{
			// Calculate page count by the length of each page in relation to the overall length of the result
			pagingResponse.setTotalNumber((pagingResponse.getTotalSize() + length - 1) / length);
		}
		if (alternateIdPropertyName != null)
		{
			pagingResponse.setRefResult(queryRefResult);
		}
		else
		{
			@SuppressWarnings("unchecked")
			IList<T> result = (IList<T>) cache.getObjects(queryRefResult, Collections.<CacheDirective> emptySet());
			pagingResponse.setResult(result);
		}
		long end = System.currentTimeMillis();
		if (log.isDebugEnabled())
		{
			if (alternateIdPropertyName != null)
			{
				log.debug(StringBuilderUtil.concat(tlObjectCollector, "Spent ", end - start, " ms executing query returning ", pagingResponse.getRefResult()
						.size(), " ORIs of entity type '", query.getEntityType(), "'"));
			}
			else
			{
				log.debug(StringBuilderUtil.concat(tlObjectCollector, "Spent ", end - start, " ms executing query returning ", pagingResponse.getResult()
						.size(), " instances of entity type '", query.getEntityType(), "'"));
			}
		}
		return pagingResponse;
	}

	@Override
	public IPagingQuery<T> param(Object paramKey, Object param)
	{
		StatefulPagingQuery<T> statefulQuery = new StatefulPagingQuery<T>(this);
		return statefulQuery.param(paramKey, param);
	}
}
