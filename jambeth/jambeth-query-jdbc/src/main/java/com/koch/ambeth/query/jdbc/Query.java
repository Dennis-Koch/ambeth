package com.koch.ambeth.query.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;

/*-
 * #%L
 * jambeth-query-jdbc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.EntityCursor;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.Table;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderExtension;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.StatefulQuery;
import com.koch.ambeth.query.jdbc.sql.ITableAliasHolder;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

@PersistenceContext
public class Query<T> implements IQuery<T>, IQueryIntern<T>, ISubQuery<T> {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IServiceUtil serviceUtil;

	@Autowired
	protected IStringQuery stringQuery;

	@Autowired
	protected ITableAliasHolder tableAliasHolder;

	@Autowired
	protected ITransaction transaction;

	@Property
	protected Class<T> entityType;

	@Property(defaultValue = "false")
	protected boolean containsSubQuery;

	@Property
	protected IOperand rootOperand;

	@Property
	protected IOperand[] groupByOperands;

	@Property
	protected IOperand[] orderByOperands;

	@Property
	protected IOperand limitOperand;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Property
	protected IOperand[] selectOperands;

	@Property
	protected List<Class<?>> relatedEntityTypes;

	@Property
	protected IQueryIntern<T> transactionalQuery;

	@Property
	protected IQueryBuilderExtension[] queryBuilderExtensions;

	protected IQueryKey queryKey;

	@Override
	public Class<T> getEntityType() {
		return this.entityType;
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes) {
		for (int a = 0, size = this.relatedEntityTypes.size(); a < size; a++) {
			relatedEntityTypes.add(this.relatedEntityTypes.get(a));
		}
	}

	@Override
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap) {
		if (this.queryKey == null) {
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

			IMap<Object, Object> tempNameToValueMap = null;
			if (nameToValueMap == null) {
				tempNameToValueMap = new HashMap<>();
				nameToValueMap = tempNameToValueMap;
			}

			String[] sqlParts = getSqlParts(nameToValueMap, null, null);
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try {
				for (int i = 0; i < sqlParts.length; i++) {
					sb.append('#');
					if (sqlParts[i] != null) {
						sb.append(sqlParts[i]);
					}
				}
				this.queryKey = new StringQueryKey(this.entityType, sb.toString());
			}
			finally {
				tlObjectCollector.dispose(sb);
			}
		}

		return this.queryKey;
	}

	protected Object buildCursor(IMap<Object, Object> nameToValueMapSrc, RetrievalType retrievalType,
			int limitValue, boolean retrieveAlternateIds) {
		if (!transaction.isActive()) {
			throw new IllegalStateException(IQuery.class.getSimpleName()
					+ ".retrieveAsVersions() can only be called from within a @"
					+ PersistenceContext.class.getSimpleName()
					+ ". This is because the IVersionCursor may hold active resources of underlying databases");
		}
		HashMap<Object, Object> nameToValueMap = new HashMap<>();
		if (nameToValueMapSrc == null) {
			nameToValueMapSrc = EmptyMap.emptyMap();
		}
		else {
			nameToValueMap.putAll(nameToValueMapSrc);
		}
		if (containsSubQuery) {
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}
		if (limitValue > 0) {
			nameToValueMap.put(QueryConstants.LIMIT_VALUE, Integer.valueOf(limitValue));
		}
		else {
			nameToValueMap.put(QueryConstants.LIMIT_VALUE, null);
		}

		ArrayList<String> additionalSelectColumnList = new ArrayList<>();
		ArrayList<Object> parameters = new ArrayList<>();
		Object pagingSizeObject = nameToValueMap.get(QueryConstants.PAGING_SIZE_OBJECT);

		String[] sqlParts = getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
		String joinSql = sqlParts[0];
		String whereSql = sqlParts[1];
		String orderBySql = sqlParts[2];
		String limitSql = sqlParts[3];

		String tableAlias = (stringQuery.isJoinQuery() || containsSubQuery)
				? tableAliasHolder.getTableAlias()
				: null;

		Table table = (Table) this.database.getTableByType(this.entityType);

		if (RetrievalType.DATA.equals(retrievalType)) {
			fillAdditionalFieldsSQL(additionalSelectColumnList, null, nameToValueMap,
					stringQuery.isJoinQuery(), parameters);
		}
		if (pagingSizeObject == null) {
			if (RetrievalType.VERSION.equals(retrievalType)) {
				return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql,
						limitSql, parameters, tableAlias, retrieveAlternateIds);
			}
			else if (RetrievalType.COUNT.equals(retrievalType)) {
				return Long
						.valueOf(table.selectCountJoin(joinSql, whereSql, orderBySql, parameters, tableAlias));
			}
			return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql,
					limitSql, parameters, tableAlias);
		}
		else if (RetrievalType.COUNT.equals(retrievalType)) {
			return Long
					.valueOf(table.selectCountJoin(joinSql, whereSql, orderBySql, parameters, tableAlias));
		}
		Object pagingIndexObject = nameToValueMap.get(QueryConstants.PAGING_INDEX_OBJECT);

		int pagingLimit = conversionHelper.convertValueToType(Integer.TYPE, pagingSizeObject);
		int pagingOffset = conversionHelper.convertValueToType(Integer.TYPE, pagingIndexObject);

		if (RetrievalType.VERSION.equals(retrievalType)) {
			return table.selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql,
					limitSql, pagingOffset, pagingLimit, parameters, tableAlias, retrieveAlternateIds);
		}
		return table.selectDataPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql,
				limitSql, pagingOffset, pagingLimit, parameters);
	}

	@Override
	public String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<String> additionalSelectColumnList) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		Object useTableAliasOriginal = nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS);
		if (containsSubQuery) {
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}
		for (IQueryBuilderExtension queryBuilderExtension : queryBuilderExtensions) {
			queryBuilderExtension.applyOnQuery(nameToValueMap, parameters, additionalSelectColumnList);
		}
		String joinSql, whereSql;
		boolean joinQuery = stringQuery.isJoinQuery();
		if (!joinQuery) {
			joinSql = null;
			whereSql = stringQuery.fillQuery(nameToValueMap, parameters);
		}
		else {
			String[] sqlParts = stringQuery.fillJoinQuery(nameToValueMap, parameters);

			joinSql = sqlParts[0];
			whereSql = sqlParts[1];
		}
		AppendableStringBuilder tempSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			fillOrderBySQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);
			String orderBySql = tempSB.length() > 0 ? tempSB.toString() : null;
			tempSB.reset();
			fillLimitSQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);
			String limitSql = tempSB.length() > 0 ? tempSB.toString() : null;
			String[] sqlParts = {joinSql, whereSql, orderBySql, limitSql};
			return sqlParts;
		}
		finally {
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, useTableAliasOriginal);
			tlObjectCollector.dispose(tempSB);
		}
	}

	protected void fillOrderBySQL(List<String> additionalSelectColumnList, IAppendable orderBySB,
			IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
		if (orderByOperands.length == 0 && groupByOperands.length == 0) {
			return;
		}
		nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.TRUE);
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try {
			boolean firstGroupBy = true;
			for (int a = 0, size = groupByOperands.length; a < size; a++) {
				if (firstGroupBy) {
					orderBySB.append("GROUP BY ");
					firstGroupBy = false;
				}
				else {
					orderBySB.append(',');
				}
				groupByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
			}
			for (int a = 0, size = orderByOperands.length; a < size; a++) {
				orderByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally {
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
			nameToValueMap.remove(QueryConstants.FIRST_ORDER_BY_STATE);
		}
	}

	protected void fillLimitSQL(List<String> additionalSelectColumnList, IAppendable limitSB,
			IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters) {
		if (limitOperand == null) {
			return;
		}
		limitOperand.expandQuery(limitSB, nameToValueMap, joinQuery, parameters);
	}

	protected void fillAdditionalFieldsSQL(IList<String> additionalSelectColumnList,
			IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters) {
		if (selectOperands.length == 0) {
			return;
		}
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try {
			for (int a = 0, size = selectOperands.length; a < size; a++) {
				selectOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally {
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		}
	}

	@Override
	public IVersionCursor retrieveAsVersions() {
		return (IVersionCursor) buildCursor(null, RetrievalType.VERSION, 0, true);
	}

	@Override
	public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds) {
		return (IVersionCursor) buildCursor(null, RetrievalType.VERSION, 0, retrieveAlternateIds);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap) {
		return (IVersionCursor) buildCursor(nameToValueMap, RetrievalType.VERSION, 0, true);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap,
			boolean retrieveAlternateIds) {
		return (IVersionCursor) buildCursor(nameToValueMap, RetrievalType.VERSION, 0,
				retrieveAlternateIds);
	}

	@Override
	public IList<IObjRef> retrieveAsObjRefs(IMap<Object, Object> nameToValueMap, int idIndex) {
		return serviceUtil.loadObjRefs(entityType, idIndex,
				retrieveAsVersions(nameToValueMap, ObjRef.PRIMARY_KEY_INDEX != idIndex));
	}

	@Override
	public IList<IObjRef> retrieveAsObjRefs(int idIndex) {
		return serviceUtil.loadObjRefs(entityType, idIndex,
				retrieveAsVersions(null, ObjRef.PRIMARY_KEY_INDEX != idIndex));
	}

	@Override
	public IDataCursor retrieveAsData() {
		return (IDataCursor) buildCursor(null, RetrievalType.DATA, 0, false);
	}

	@Override
	public IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap) {
		return (IDataCursor) buildCursor(nameToValueMap, RetrievalType.DATA, 0, false);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor() {
		return retrieveAsCursor(null);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap) {
		IVersionCursor cursor = retrieveAsVersions(nameToValueMap);
		return new EntityCursor<>(cursor, this.entityType, this.serviceUtil);
	}

	@Override
	public IList<T> retrieve() {
		return retrieve(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<T> retrieve(IMap<Object, Object> nameToValueMap) {
		IVersionCursor cursor = retrieveAsVersions(nameToValueMap);
		if (cursor == null) {
			return (IList<T>) EmptyList.instance;
		}
		ArrayList<T> resultList = new ArrayList<>();
		serviceUtil.loadObjectsIntoCollection(resultList, entityType, cursor);
		return resultList;
	}

	@Override
	public T retrieveSingle() {
		IList<T> result = retrieve();
		if (result == null || result.isEmpty()) {
			return null;
		}
		if (result.size() != 1) {
			throw new IllegalStateException("Query result is not unique: " + result.size());
		}
		return result.get(0);
	}

	@Override
	public long count() {
		return count(null);
	}

	@Override
	public long count(IMap<Object, Object> paramNameToValueMap) {
		return ((Long) buildCursor(paramNameToValueMap, RetrievalType.COUNT, 0, false)).longValue();
	}

	@Override
	public boolean isEmpty() {
		return isEmpty(null);
	}

	@Override
	public boolean isEmpty(IMap<Object, Object> paramNameToValueMap) {
		IVersionCursor versionCursor = (IVersionCursor) buildCursor(paramNameToValueMap,
				RetrievalType.VERSION, 1, false);
		try {
			Iterator<IVersionItem> iter = versionCursor.iterator();
			return !iter.hasNext();
		}
		finally {
			versionCursor.dispose();
		}
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param) {
		StatefulQuery<T> statefulQuery = new StatefulQuery<>(transactionalQuery);
		return statefulQuery.param(paramKey, param);
	}

	@Override
	public String getMainTableAlias() {
		return tableAliasHolder.getTableAlias();
	}

	@Override
	public void setMainTableAlias(String alias) {
		tableAliasHolder.setTableAlias(alias);
	}

	@Override
	public void dispose() {
		for (Field field : ReflectUtil.getDeclaredFieldsInHierarchy(getClass())) {
			int modifiers = field.getModifiers();
			if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)
					|| field.getType().isPrimitive()) {
				continue;
			}
			try {
				field.set(this, null);
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}
}
