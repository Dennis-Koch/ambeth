package de.osthus.ambeth.persistence.blueprint;

import javassist.ClassPool;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.orm.IOrmEntityTypeProvider;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.StringConversionHelper;

public class BlueprintTableCreator
{
	@LogInstance
	private ILogger log;

	@Autowired(XmlModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected IOrmEntityTypeProvider ormEntityTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected ClassPool pool = ClassPool.getDefault();

	public void createTable(IEntityTypeBlueprint entityType, String tableName)
	{

		// CREATE TABLE "AUDIT_ENTRY"
		// (
		// "ID" NUMBER(9,0) NOT NULL,
		// "VERSION" NUMBER(9,0) NOT NULL,
		// "USER_ID" NUMBER(9,0),
		// "USER_IDENTIFIER" VARCHAR(64 CHAR),
		// "SIGNATURE_OF_USER_ID" NUMBER(9,0),
		// "SIGNATURE" CHAR(140 CHAR),
		// "PROTOCOL" NUMBER(9,0),
		// "TIMESTAMP" NUMBER(18,0) NOT NULL,
		// "REASON" VARCHAR2(4000 CHAR),
		// "CONTEXT" VARCHAR2(256 CHAR),
		// "HASH_ALGORITHM" VARCHAR2(64 CHAR),
		// CONSTRAINT "AUDIT_ENTRY_PK" PRIMARY KEY ("ID") USING INDEX,
		// CONSTRAINT "AUDIT_ENTRY_FK_USER" FOREIGN KEY ("USER_ID") REFERENCES "USER" ("ID") DEFERRABLE INITIALLY DEFERRED,
		// CONSTRAINT "AUDIT_ENTRY_FK_SIGNATURE" FOREIGN KEY ("SIGNATURE_OF_USER_ID") REFERENCES "SIGNATURE" ("ID") DEFERRABLE INITIALLY DEFERRED
		// );
		// CREATE SEQUENCE "AUDIT_ENTRY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

		StringBuilder s = new StringBuilder("CREATE TABLE \"");
		s.append(tableName);
		s.append("\" (");
		Class<?> resolvedEntityType = ormEntityTypeProvider.resolveEntityType(entityType.getName());
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(resolvedEntityType);
		boolean firstProp = true;
		for (IPropertyInfo property : properties)
		{
			if (firstProp)
			{
				firstProp = false;
			}
			else
			{
				s.append(", ");
			}
			s.append("\"");
			s.append(StringConversionHelper.camelCaseToUnderscore(objectCollector, property.getName()));
			s.append("\"");
			// TODO: NOT NULL for ID, VERSION
		}

		s.append("); ");
	}

}
