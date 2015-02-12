package de.osthus.ambeth.persistence.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.xml.model.ParentOfEmbeddedType;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("embeddedtype_data.sql")
@SQLStructure("embeddedtype_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/embedded_type_orm.xml")
public class EmbeddedTypeTest extends AbstractPersistenceTest
{
	@Test
	public void simple() throws Throwable
	{
		// This is rather evil but for Testing ok: Do not extract database-objects out of a transaction scope in
		// production code!
		ITableMetaData table = transaction.processAndCommit(new ResultingDatabaseCallback<ITableMetaData>()
		{

			@Override
			public ITableMetaData callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				return persistenceUnitToDatabaseMap.iterator().next().getValue().getTableByType(ParentOfEmbeddedType.class).getMetaData();
			}
		});
		List<IFieldMetaData> primitiveFields = table.getPrimitiveFields();

		IFieldMetaData nameField = table.getFieldByMemberName("MyEmbeddedType.Name");
		IFieldMetaData valueField = table.getFieldByMemberName("MyEmbeddedType.Value");

		Assert.assertNotNull(nameField);
		Assert.assertNotNull(valueField);
		Assert.assertTrue(primitiveFields.contains(nameField));
		Assert.assertTrue(primitiveFields.contains(valueField));
	}

	@Test
	public void selectWithQueryBuilder() throws Throwable
	{
		String param1 = "param1";

		IQueryBuilder<ParentOfEmbeddedType> qb = queryBuilderFactory.create(ParentOfEmbeddedType.class);
		IQuery<ParentOfEmbeddedType> query = qb.build(qb.isEqualTo(qb.property("MyEmbeddedType.Name"), qb.valueName(param1)));

		List<ParentOfEmbeddedType> parents = query.param(param1, "My Name Is").retrieve();
		Assert.assertEquals(1, parents.size());
	}
}
