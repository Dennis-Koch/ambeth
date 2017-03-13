package com.koch.ambeth.persistence.jdbc.auto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.ILinkedMap;

@SQLData("autoindex_data.sql")
@SQLStructure("autoindex_structure.sql")
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.AutoIndexForeignKeys, value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/auto/autoindex_orm.xml") })
public class AutoIndexTrueTest extends AbstractInformationBusWithPersistenceTest
{
	@Test
	public void testAutoIndexTrue()
	{
		transaction.processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
			{
				int countOfUnindexedFKs = getCountOfUnindexedFKs(beanContext);
				Assert.assertEquals(0, countOfUnindexedFKs);
			}
		});
	}

	protected static int getCountOfUnindexedFKs(IServiceContext beanContext) throws SQLException
	{
		Connection connection = beanContext.getService(Connection.class);
		Statement stm = connection.createStatement();
		int count = 0;
		ResultSet rs = null;
		try
		{
			String subSelect1 = "SELECT SUBSTR(table_name, 1, 30) table_name, SUBSTR(constraint_name, 1, 30) constraint_name, SUBSTR(column_name, 1, 30) column_name, position " //
					+ "FROM user_cons_columns";
			String subSelect2 = "SELECT b.table_name, b.constraint_name, MAX(decode(position, 1, column_name, null)) cname1, " //
					+ "MAX(decode(position, 2, column_name, null)) cname2, MAX(decode(position, 3, column_name, null)) cname3, " //
					+ "MAX(decode(position, 4, column_name, null)) cname4, MAX(decode(position, 5, column_name, null)) cname5, " //
					+ "MAX(decode(position, 6, column_name, null)) cname6, MAX(decode(position, 7, column_name, null)) cname7, " //
					+ "MAX(decode(position, 8, column_name, null)) cname8, COUNT(*) col_cnt " //
					+ "FROM (" + subSelect1 + ") a, user_constraints b " //
					+ "WHERE a.constraint_name = b.constraint_name " //
					+ "AND b.constraint_type = 'R' " //
					+ "GROUP BY b.table_name, b.constraint_name";
			String subSelect3 = "SELECT COUNT(*) " //
					+ "FROM user_ind_columns i " //
					+ "WHERE i.table_name = cons.table_name " //
					+ "AND i.column_name in (cname1, cname2, cname3, cname4, cname5, cname6, cname7, cname8) " //
					+ "AND i.column_position <= cons.col_cnt " //
					+ "GROUP BY i.index_name";
			String mainQuery = "SELECT table_name, constraint_name, " //
					+ "cname1 " //
					+ "|| nvl2(cname2,','||cname2, null) || nvl2(cname3,','||cname3, null) || nvl2(cname4,','||cname4, null) "
					+ "|| nvl2(cname5,','||cname5, null) || nvl2(cname6,','||cname6, null) || nvl2(cname7,','||cname7, null) || nvl2(cname8,','||cname8, null) "
					+ "columns " //
					+ "FROM (" + subSelect2 + ") cons " //
					+ "WHERE col_cnt > ALL (" + subSelect3 + ")";
			rs = stm.executeQuery(mainQuery);
			while (rs.next())
			{
				count++;
			}
			return count;
		}
		finally
		{
			JdbcUtil.close(stm, rs);
		}
	}
}
