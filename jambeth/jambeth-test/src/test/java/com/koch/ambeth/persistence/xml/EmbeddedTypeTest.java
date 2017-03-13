package com.koch.ambeth.persistence.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.persistence.xml.model.ParentOfEmbeddedType;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.ILinkedMap;

@SQLData("embeddedtype_data.sql")
@SQLStructure("embeddedtype_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/embedded_type_orm.xml")
public class EmbeddedTypeTest extends AbstractInformationBusWithPersistenceTest
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
