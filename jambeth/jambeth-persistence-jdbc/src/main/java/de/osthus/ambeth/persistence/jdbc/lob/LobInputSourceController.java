package de.osthus.ambeth.persistence.jdbc.lob;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Arrays;

import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDataItem;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;

public class LobInputSourceController implements ILobInputSourceController
{
	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected ITransaction transaction;

	@Override
	public IInputStream deriveInputStream(final Object parentEntity, final Member member)
	{
		return deriveBinaryInputStreamIntern(parentEntity, member);
	}

	@Override
	public IBinaryInputStream deriveBinaryInputStream(final Object parentEntity, final Member member)
	{
		if (transaction.isActive())
		{
			return (IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
		}
		if (!connectionDialect.isTransactionNecessaryDuringLobStreaming())
		{
			return transaction.runInTransaction(new IResultingBackgroundWorkerDelegate<IBinaryInputStream>()
			{
				@Override
				public IBinaryInputStream invoke() throws Throwable
				{
					return (IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
				}
			});
		}
		try
		{
			transaction.begin(true);
			final IBinaryInputStream bis = (IBinaryInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
			return new IBinaryInputStream()
			{
				@Override
				public void close() throws IOException
				{
					try
					{
						bis.close();
					}
					finally
					{
						transaction.rollback(false);
					}
				}

				@Override
				public int readByte()
				{
					return bis.readByte();
				}
			};
		}
		catch (Throwable e)
		{
			transaction.rollback(false);
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream(final Object parentEntity, final Member member)
	{
		if (transaction.isActive())
		{
			return (ICharacterInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
		}
		return transaction.runInTransaction(new IResultingBackgroundWorkerDelegate<ICharacterInputStream>()
		{
			@Override
			public ICharacterInputStream invoke() throws Throwable
			{
				return (ICharacterInputStream) deriveBinaryInputStreamIntern(parentEntity, member);
			}
		});
	}

	protected IInputStream deriveBinaryInputStreamIntern(final Object parentEntity, final Member member)
	{
		IEntityMetaData metaData = ((IEntityMetaDataHolder) parentEntity).get__EntityMetaData();
		IDatabase database = this.database.getCurrent();
		Table table = (Table) database.getTableByType(metaData.getEntityType());

		IFieldMetaData idField = table.getMetaData().getIdField();

		Object persistedId = conversionHelper.convertValueToType(idField.getFieldType(), metaData.getIdMember().getValue(parentEntity));

		IFieldMetaData lobField = table.getMetaData().getFieldByMemberName(member.getName());

		boolean success = false;
		IDataCursor dataCursor = table.selectDataJoin(Arrays.asList("\"" + lobField.getName() + "\""), null, "\"" + idField.getName() + "\"=?", null, null,
				Arrays.asList(persistedId));
		try
		{
			if (!dataCursor.moveNext())
			{
				if (Clob.class.equals(lobField.getFieldType()))
				{
					return new EmptyClobInputStream();
				}
				return new EmptyBlobInputStream();
			}
			IDataItem dataItem = dataCursor.getCurrent();
			if (Clob.class.equals(lobField.getFieldType()))
			{
				Clob value = (Clob) connectionDialect.convertFromFieldType(database, lobField, Clob.class, dataItem.getValue(0));
				success = true;
				return new ClobInputStream(dataCursor, value);
			}
			else
			{
				Blob value = (Blob) connectionDialect.convertFromFieldType(database, lobField, Blob.class, dataItem.getValue(0));
				success = true;
				return new BlobInputStream(dataCursor, value);
			}
		}
		finally
		{
			if (!success)
			{
				dataCursor.dispose();
			}
		}
	}
}
