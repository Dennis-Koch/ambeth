package de.osthus.ambeth.persistence.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.xml.model.ParentOfEmbeddedType;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("embeddedtype_data.sql")
@SQLStructure("embeddedtype_structure.sql")
@TestModule(TestServicesModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/embedded_type_orm.xml")
public class EmbeddedTypeTest extends AbstractPersistenceTest
{
	@Test
	public void simple() throws Throwable
	{
		// This is rather evil but for Testing ok: Do not extract database-objects out of a transaction scope in
		// production code!
		ITable table = transaction.processAndCommit(new ResultingDatabaseCallback<ITable>()
		{

			@Override
			public ITable callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				return persistenceUnitToDatabaseMap.iterator().next().getValue().getTableByType(ParentOfEmbeddedType.class);
			}
		});
		List<IField> primitiveFields = table.getPrimitiveFields();

		IField nameField = table.getFieldByMemberName("MyEmbeddedType.Name");
		IField valueField = table.getFieldByMemberName("MyEmbeddedType.Value");

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
