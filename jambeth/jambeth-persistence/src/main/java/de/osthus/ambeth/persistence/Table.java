package de.osthus.ambeth.persistence;

import java.util.HashMap;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.IAlreadyLinkedCache;

public class Table implements ITable, IInitializingBean
{
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property
	protected ITableMetaData metaData;

	protected final ArrayList<IDirectedLink> links = new ArrayList<IDirectedLink>();

	protected final HashMap<String, IDirectedLink> fieldNameToLinkDict = new HashMap<String, IDirectedLink>();

	protected final HashMap<String, IDirectedLink> linkNameToLinkDict = new HashMap<String, IDirectedLink>();

	protected final HashMap<String, IDirectedLink> memberNameToLinkDict = new HashMap<String, IDirectedLink>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public ITableMetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public List<IDirectedLink> getLinks()
	{
		return links;
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersionWhere(CharSequence whereSql)
	{
		return selectVersionWhere(null, whereSql, null, null, null);
	}

	@Override
	public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters, String tableAlias, boolean retrieveAlternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias, boolean retrieveAlternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public long selectCountJoin(CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, List<Object> parameters, String tableAlias)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters, String tableAlias)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(List<?> ids)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(String alternateIdMemberName, List<?> alternateIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IDirectedLink getLinkByName(String linkName)
	{
		return linkNameToLinkDict.get(linkName);
	}

	@Override
	public IDirectedLink getLinkByFieldName(String fieldName)
	{
		return fieldNameToLinkDict.get(fieldName);
	}

	@Override
	public IDirectedLink getLinkByMemberName(String memberName)
	{
		return memberNameToLinkDict.get(memberName);
	}

	@Override
	public void delete(List<IObjRef> oris)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void startBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int[] finishBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void clearBatch()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object insert(Object id, ILinkedMap<IFieldMetaData, Object> puis)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<IFieldMetaData, Object> puis)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected void deleteLinksToId(Object id)
	{
		for (IDirectedLink relatedLink : links)
		{
			relatedLink.unlinkAllIds(id);
		}
	}

	@Override
	public String toString()
	{
		return "Table: " + getMetaData().getName();
	}
}
