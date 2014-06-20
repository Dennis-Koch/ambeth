package de.osthus.ambeth.persistence.jdbc.bigstatements;

import java.net.SocketException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.persistence.ICursor;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLDataRebuild;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc.JdbcTable", value = "INFO") })
@SQLStructure("BigStatement_structure.sql")
@SQLData("BigStatement_data.sql")
@SQLDataRebuild(false)
public class BigStatementTest extends AbstractPersistenceTest
{
	@Test
	public void testBigQuery100000() throws Exception
	{
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isIn(qb.property("Id"), qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<Object>();
		for (int a = 100000; a-- > 0;)
		{
			bigList.add(Integer.valueOf(a + 1));
		}
		try
		{
			IList<Material> materials = query.param(paramName, bigList).retrieve();
			Assert.assertNotNull(materials);
			Assert.assertEquals(90006, materials.size());
		}
		catch (MaskingRuntimeException e)
		{
			Throwable cause = e.getCause();
			Assert.assertTrue(cause instanceof PersistenceException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SQLSyntaxErrorException);
			Assert.assertEquals("ORA-01745: invalid host/bind variable name\n", cause.getMessage());
			throw e;
		}

	}

	@Test
	public void testBigQuery20000() throws Exception
	{
		String paramName = "paramName";
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.isIn(qb.property("Id"), qb.valueName(paramName)));

		ArrayList<Object> bigList = new ArrayList<Object>();
		for (int a = 20000; a-- > 0;)
		{
			bigList.add(Integer.valueOf(a + 1));
		}
		try
		{
			IList<Material> materials = query.param(paramName, bigList).retrieve();
			Assert.assertNotNull(materials);
			Assert.assertEquals(10006, materials.size());
		}
		catch (MaskingRuntimeException e)
		{
			Throwable cause = e.getCause();
			Assert.assertTrue(cause instanceof PersistenceException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SQLRecoverableException);
			cause = cause.getCause();
			Assert.assertTrue(cause instanceof SocketException);
			Assert.assertEquals("Connection reset by peer: socket write error", cause.getMessage());
			throw e;
		}
	}

	@Test
	public void testSelectFields100000() throws Exception
	{
		final ArrayList<Object> bigList = new ArrayList<Object>();
		for (int a = 100001; a-- > 0;)
		{
			bigList.add(Integer.valueOf(a + 1));
		}
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				IDatabase database = persistenceUnitToDatabaseMap.iterator().next().getValue();
				ITable table = database.getTableByType(Material.class);
				IVersionCursor cursor = table.selectVersion(bigList);
				try
				{

				}
				finally
				{
					cursor.dispose();
				}
			}
		});
		transaction.processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				IDatabase database = persistenceUnitToDatabaseMap.iterator().next().getValue();
				ITable table = database.getTableByType(Material.class);
				ICursor cursor = table.selectValues(bigList);
				try
				{

				}
				finally
				{
					cursor.dispose();
				}
			}
		});
	}

	@Test
	public void testMerge100000() throws Exception
	{
		IQueryBuilder<Material> qb = queryBuilderFactory.create(Material.class);
		IQuery<Material> query = qb.build(qb.all());
		IList<Material> materials = query.retrieve();
		Assert.assertTrue(materials.size() > 100000);

		for (Material material : materials)
		{
			material.setName(material.getName() + "2");
		}
		beanContext.getService(IMergeProcess.class).process(materials, null, null, null);
	}
}
