package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.HashMap;
import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class Table implements ITable, IInitializingBean {
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

	protected final ArrayList<IDirectedLink> links = new ArrayList<>();

	protected final HashMap<String, IDirectedLink> fieldNameToLinkDict =
			new HashMap<>();

	protected final HashMap<String, IDirectedLink> linkNameToLinkDict =
			new HashMap<>();

	protected final HashMap<String, IDirectedLink> memberNameToLinkDict =
			new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Throwable {
	}

	@Override
	public ITableMetaData getMetaData() {
		return metaData;
	}

	@Override
	public List<IDirectedLink> getLinks() {
		return links;
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectVersionWhere(CharSequence whereSql) {
		return selectVersionWhere(null, whereSql, null, null, null);
	}

	@Override
	public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList,
			CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList,
			CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters, String tableAlias, boolean retrieveAlternateIds) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList,
			CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			int offset, int length, List<Object> parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList,
			CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			int offset, int length, List<Object> parameters, String tableAlias,
			boolean retrieveAlternateIds) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public long selectCountJoin(CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			List<Object> parameters, String tableAlias) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters, String tableAlias) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql,
			CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql, int offset, int length,
			List<Object> parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor selectAll() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(List<?> ids) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ICursor selectValues(String alternateIdMemberName, List<?> alternateIds) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IDirectedLink getLinkByName(String linkName) {
		return linkNameToLinkDict.get(linkName);
	}

	@Override
	public IDirectedLink getLinkByFieldName(String fieldName) {
		return fieldNameToLinkDict.get(fieldName);
	}

	@Override
	public IDirectedLink getLinkByMemberName(String memberName) {
		return memberNameToLinkDict.get(memberName);
	}

	@Override
	public void delete(List<IObjRef> oris) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void startBatch() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int[] finishBatch() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void clearBatch() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object insert(Object id, ILinkedMap<IFieldMetaData, Object> puis) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Object update(Object id, Object version, ILinkedMap<IFieldMetaData, Object> puis) {
		throw new UnsupportedOperationException("Not implemented");
	}

	protected void deleteLinksToId(Object id) {
		for (IDirectedLink relatedLink : links) {
			relatedLink.unlinkAllIds(id);
		}
	}

	@Override
	public String toString() {
		return "Table: " + getMetaData().getName();
	}

	@Override
	public void updateLinks() {
		for (IDirectedLinkMetaData directedLinkMD : metaData.getLinks()) {
			RelationMember member = directedLinkMD.getMember();

			if (member == null || memberNameToLinkDict.containsKey(member.getName())) {
				continue;
			}

			IDirectedLink directedLink = linkNameToLinkDict.get(directedLinkMD.getName());
			memberNameToLinkDict.put(member.getName(), directedLink);
		}
	}
}
