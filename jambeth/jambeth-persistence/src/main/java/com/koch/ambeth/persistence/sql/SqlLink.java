package com.koch.ambeth.persistence.sql;

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

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.Link;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SqlLink extends Link {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected ISqlConnection sqlConnection;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		ParamChecker.assertTrue(
				getMetaData().getFromField() != null || getMetaData().getToField() != null,
				"FromField or ToField");
	}

	@Override
	public ILinkCursor findAllLinked(IDirectedLink fromLink, List<?> fromIds) {
		return findAllLinkedIntern(fromLink, fromIds, false);
	}

	@Override
	public ILinkCursor findAllLinkedTo(IDirectedLink fromLink, List<?> toIds) {
		return findAllLinkedIntern(fromLink, toIds, true);
	}

	protected ILinkCursor findAllLinkedIntern(IDirectedLink fromLink, List<?> fromOrToIds,
			boolean isToId) {
		// Link
		// DirLink
		// 1) F,T WHERE F IN (?) findAllLinked
		// 2) F,T WHERE T IN (?) findAllLinkedTo isToId=true
		// RevDirLink
		// 1) T,F WHERE T IN (?) findAllLinked
		// 2) T,F WHERE F IN (?) findAllLinkedTo isToId=true
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		AppendableStringBuilder fieldNamesSB = current.create(AppendableStringBuilder.class);
		try {
			IDirectedLinkMetaData fromLinkMetaData = fromLink.getMetaData();
			IFieldMetaData fromField = fromLinkMetaData.getFromField();
			IFieldMetaData toField = fromLinkMetaData.getToField();

			if (!getMetaData().hasLinkTable()) {
				if (fromLink.getFromTable().equals(fromLink.getLink().getFromTable())) {
					toField = fromField.getTable().getIdField();
				}
				else {
					fromField = toField.getTable().getIdField();
				}
				// All fields themselves are correct now. But for "some" reason we have to switch the fields
				// :(
				IFieldMetaData tempField = fromField;
				fromField = toField;
				toField = tempField;
			}
			sqlBuilder.appendName(fromField.getName(), fieldNamesSB);
			fieldNamesSB.append(',');
			sqlBuilder.appendName(toField.getName(), fieldNamesSB);

			String fieldNames = fieldNamesSB.toString();
			IFieldMetaData wantedField = isToId ? fromField : toField;
			IFieldMetaData whereField = isToId ? toField : fromField;

			fieldNamesSB.reset();
			sqlBuilder.appendName(wantedField.getName(), fieldNamesSB);
			fieldNamesSB.append(" IS NOT NULL");
			String whereSQL = fieldNamesSB.toString();

			IResultSet resultSet =
					sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedTableName(),
							whereField.getName(), whereField.getFieldType(), fieldNames, whereSQL, fromOrToIds);

			ResultSetLinkCursor linkCursor = new ResultSetLinkCursor();
			linkCursor.setFromIdIndex(fromField.getIdIndex());
			linkCursor.setToIdIndex(toField.getIdIndex());
			linkCursor.setResultSet(resultSet);
			linkCursor.afterPropertiesSet();

			return linkCursor;
		}
		finally {
			current.dispose(fieldNamesSB);
		}
	}

	@Override
	public void linkIds(IDirectedLink fromLink, Object fromId, List<?> toIds) {
		ILinkMetaData metaData = getMetaData();
		if (!metaData.getName().equals(metaData.getTableName())) {
			updateLinks(fromLink, fromId, toIds);
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ArrayList<Object> convertedToIds = new ArrayList<>();
		IFieldMetaData fromField, toField;
		if (getDirectedLink() == fromLink) {
			fromField = metaData.getFromField();
			toField = metaData.getToField();
		}
		else if (getReverseDirectedLink() == fromLink) {
			fromField = metaData.getToField();
			toField = metaData.getFromField();
		}
		else {
			throw new IllegalArgumentException("Invalid link " + fromLink);
		}
		Class<?> fromFieldType = fromField.getFieldType();
		Class<?> toFieldType = toField.getFieldType();

		fromId = conversionHelper.convertValueToType(fromFieldType, fromId);

		for (int a = toIds.size(); a-- > 0;) {
			Object toId = toIds.get(a);

			toId = conversionHelper.convertValueToType(toFieldType, toId);
			if (!addLinkModToCache(fromLink, fromId, toId)) {
				continue;
			}
			convertedToIds.add(toId);
		}
		if (convertedToIds.size() == 0) {
			return;
		}
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder namesSB = objectCollector.create(AppendableStringBuilder.class);
		try {
			sqlBuilder.appendName(fromField.getName(), namesSB);
			namesSB.append(',');
			sqlBuilder.appendName(toField.getName(), namesSB);
			linkIdsIntern(namesSB.toString(), fromId, toFieldType, convertedToIds);
		}
		finally {
			objectCollector.dispose(namesSB);
		}
	}

	protected void linkIdsIntern(String names, Object fromId, Class<?> toIdType, List<Object> toIds) {
		throw new UnsupportedOperationException();
	}

	protected void unlinkIdsIntern(String whereSQL, Class<?> toIdType, List<Object> parameters) {
		throw new UnsupportedOperationException();
	}

	public void updateLinks(IDirectedLink fromLink, Object fromId, List<?> toIds) {
		if (toIds.size() == 1) {
			updateLink(fromLink, fromId, toIds.get(0));
		}
		else {
			// TODO!!!
			for (int i = toIds.size(); i-- > 0;) {
				updateLink(fromLink, fromId, toIds.get(i));
			}
			// throw new UnsupportedOperationException("Not yet implemented!");
		}
	}

	@Override
	public void updateLink(IDirectedLink fromLink, Object fromId, Object toId) {
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		AppendableStringBuilder namesAndvaluesSB = current.create(AppendableStringBuilder.class);
		AppendableStringBuilder whereSB = current.create(AppendableStringBuilder.class);
		try {
			if (getDirectedLink() == fromLink) {
			}
			else if (getReverseDirectedLink() == fromLink) {
				Object tempId = toId;
				toId = fromId;
				fromId = tempId;
			}
			else {
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}
			IFieldMetaData toField = getMetaData().getToField();
			IFieldMetaData fromField = getMetaData().getFromField();
			// TODO alten im cache loeschen und neuen anlegen
			toId = conversionHelper.convertValueToType(toField.getFieldType(), toId);
			sqlBuilder.appendNameValue(toField.getName(), toId, namesAndvaluesSB);

			fromId = conversionHelper.convertValueToType(fromField.getFieldType(), fromId);
			sqlBuilder.appendNameValue(fromField.getName(), fromId, whereSB);

			sqlConnection.queueUpdate(getMetaData().getFullqualifiedEscapedTableName(),
					namesAndvaluesSB.toString(), whereSB.toString());
		}
		finally {
			current.dispose(namesAndvaluesSB);
			current.dispose(whereSB);
		}
	}

	@Override
	public void unlinkIds(IDirectedLink fromLink, Object fromId, List<?> toIds) {
		ILinkMetaData metaData = getMetaData();
		if (!metaData.getName().equals(metaData.getTableName())) {
			unlinkByUpdate(fromLink, fromId, toIds);
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ArrayList<Object> convertedToIds = new ArrayList<>();
		IFieldMetaData fromField, toField;
		if (getDirectedLink() == fromLink) {
			fromField = metaData.getFromField();
			toField = metaData.getToField();
		}
		else if (getReverseDirectedLink() == fromLink) {
			fromField = metaData.getToField();
			toField = metaData.getFromField();
		}
		else {
			throw new IllegalArgumentException("Invalid link " + fromLink);
		}
		Class<?> toFieldType = toField.getFieldType();

		for (int a = toIds.size(); a-- > 0;) {
			Object toId = toIds.get(a);

			toId = conversionHelper.convertValueToType(toFieldType, toId);
			if (!addLinkModToCache(fromLink, fromId, toId)) {
				continue;
			}
			convertedToIds.add(toId);
		}
		if (convertedToIds.size() == 0) {
			return;
		}
		Class<?> fromFieldType = fromField.getFieldType();
		fromId = conversionHelper.convertValueToType(fromFieldType, fromId);

		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder whereSB = objectCollector.create(AppendableStringBuilder.class);
		try {
			ArrayList<Object> parameters = new ArrayList<>();
			sqlBuilder.appendName(fromField.getName(), whereSB);
			ParamsUtil.addParam(parameters, fromId);
			whereSB.append("=? AND ");

			persistenceHelper.appendSplittedValues(toField.getName(), toField.getFieldType(),
					convertedToIds, parameters, whereSB);

			unlinkIdsIntern(whereSB.toString(), toFieldType, parameters);
		}
		finally {
			objectCollector.dispose(whereSB);
		}
	}

	@Override
	public void unlinkAllIds(IDirectedLink fromLink, Object fromId) {
		ILinkMetaData metaData = getMetaData();
		if (!metaData.getName().equals(metaData.getTableName())) {
			unlinkByUpdate(fromLink, fromId, null);
			return;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
		try {
			if (getDirectedLink() == fromLink) {
				IFieldMetaData fromField = metaData.getFromField();
				fromId = conversionHelper.convertValueToType(fromField.getFieldType(), fromId);
				sqlBuilder.appendNameValue(fromField.getName(), fromId, sb);
			}
			else if (getReverseDirectedLink() == fromLink) {
				IFieldMetaData toField = metaData.getToField();
				fromId = conversionHelper.convertValueToType(toField.getFieldType(), fromId);
				sqlBuilder.appendNameValue(toField.getName(), fromId, sb);
			}
			else {
				throw new IllegalArgumentException("Invalid table " + fromLink);
			}

			sqlConnection.queueDelete(getMetaData().getFullqualifiedEscapedTableName(), sb.toString(),
					null);
		}
		finally {
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public void unlinkAllIds() {
		if (getMetaData().getName().equals(getMetaData().getTableName())) {
			sqlConnection.queueDeleteAll(getMetaData().getFullqualifiedEscapedTableName());
		}
		else {
			unlinkByUpdate(null, null, null);
		}
	}

	/**
	 *
	 * @param fromTable
	 * @param fromId
	 * @param toIds For 1:n version
	 */
	private void unlinkByUpdate(IDirectedLink fromLink, Object fromId, List<?> toIds) {
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		AppendableStringBuilder nameAndValueSB = current.create(AppendableStringBuilder.class);
		AppendableStringBuilder whereSB = current.create(AppendableStringBuilder.class);
		IList<Object> values = null;
		try {
			if (getDirectedLink() != fromLink && getReverseDirectedLink() != fromLink) {
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}
			IFieldMetaData fromField = getMetaData().getFromField();
			IFieldMetaData toField = getMetaData().getToField();
			Class<?> fromFieldType = fromField.getFieldType();
			Class<?> toFieldType = toField.getFieldType();

			sqlBuilder.appendNameValue(toField.getName(), "", nameAndValueSB);

			if (fromId != null) {
				fromId = conversionHelper.convertValueToType(fromFieldType, fromId);
				sqlBuilder.appendNameValue(fromField.getName(), fromId, whereSB);
			}
			if (toIds != null && !toIds.isEmpty()) {
				values = new ArrayList<>();
				for (int a = toIds.size(); a-- > 0;) {
					Object toId = toIds.get(a);
					if (addLinkModToCache(fromLink, fromId, toId)) {
						values.add(conversionHelper.convertValueToType(toFieldType, toId));
					}
				}
				if (!values.isEmpty()) {
					if (fromId != null) {
						whereSB.append(" AND ");
					}
					sqlBuilder.appendNameValues(toField.getName(), values, whereSB);
				}
			}

			sqlConnection.queueUpdate(getMetaData().getFullqualifiedEscapedTableName(),
					nameAndValueSB.toString(), whereSB.toString());
		}
		finally {
			current.dispose(nameAndValueSB);
			current.dispose(whereSB);
		}
	}
}
