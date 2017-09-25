package com.koch.ambeth.persistence.jdbc.lob;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Arrays;
import java.util.Iterator;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.Table;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IDataItem;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputStream;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class LobInputSourceController implements ILobInputSourceController {
	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected ITransaction transaction;

	@Override
	public IInputStream deriveInputStream(final Object parentEntity, final Member member) {
		return deriveBinaryInputStreamIntern(parentEntity, member);
	}

	@Override
	public IBinaryInputStream deriveBinaryInputStream(final Object parentEntity,
			final Member member) {
		if (transaction.isActive()) {
			return (IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
		}
		if (!connectionDialect.isTransactionNecessaryDuringLobStreaming()) {
			return transaction
					.runInTransaction(new IResultingBackgroundWorkerDelegate<IBinaryInputStream>() {
						@Override
						public IBinaryInputStream invoke() throws Exception {
							return (IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
						}
					});
		}
		try {
			transaction.begin(true);
			final IBinaryInputStream bis =
					(IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
			return new IBinaryInputStream() {
				@Override
				public void close() throws IOException {
					try {
						bis.close();
					}
					finally {
						transaction.rollback(false);
					}
				}

				@Override
				public int readByte() {
					return bis.readByte();
				}
			};
		}
		catch (Throwable e) {
			transaction.rollback(false);
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream(final Object parentEntity,
			final Member member) {
		if (transaction.isActive()) {
			return (ICharacterInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
		}
		return transaction
				.runInTransaction(new IResultingBackgroundWorkerDelegate<ICharacterInputStream>() {
					@Override
					public ICharacterInputStream invoke() throws Exception {
						return (ICharacterInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
					}
				});
	}

	protected IInputStream deriveBinaryInputStreamIntern(final Object parentEntity,
			final Member member) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) parentEntity).get__EntityMetaData();
		IDatabase database = this.database.getCurrent();
		Table table = (Table) database.getTableByType(metaData.getEntityType());

		IFieldMetaData idField = table.getMetaData().getIdField();

		Object persistedId = conversionHelper.convertValueToType(idField.getFieldType(),
				metaData.getIdMember().getValue(parentEntity));

		IFieldMetaData lobField = table.getMetaData().getFieldByMemberName(member.getName());

		boolean success = false;
		IDataCursor dataCursor = table.selectDataJoin(Arrays.asList("\"" + lobField.getName() + "\""),
				null, "\"" + idField.getName() + "\"=?", null, null, Arrays.asList(persistedId));
		try {
			Iterator<IDataItem> dataCursorIter = dataCursor.iterator();
			if (!dataCursorIter.hasNext()) {
				if (Clob.class.equals(lobField.getFieldType())) {
					return new EmptyClobInputStream();
				}
				return new EmptyBlobInputStream();
			}
			IDataItem dataItem = dataCursorIter.next();
			if (Clob.class.equals(lobField.getFieldType())) {
				Clob value = (Clob) connectionDialect.convertFromFieldType(database, lobField, Clob.class,
						dataItem.getValue(0));
				success = true;
				return new ClobInputStream(dataCursor, value);
			}
			else {
				Blob value = (Blob) connectionDialect.convertFromFieldType(database, lobField, Blob.class,
						dataItem.getValue(0));
				success = true;
				return new BlobInputStream(dataCursor, value);
			}
		}
		finally {
			if (!success) {
				dataCursor.dispose();
			}
		}
	}
}
