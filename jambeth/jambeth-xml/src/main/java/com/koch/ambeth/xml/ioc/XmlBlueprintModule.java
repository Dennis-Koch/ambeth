package com.koch.ambeth.xml.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.blueprint.IRuntimeBlueprintEntityMetadataReader;
import com.koch.ambeth.merge.orm.blueprint.IRuntimeBlueprintVomReader;
import com.koch.ambeth.xml.orm.blueprint.BlueprintEntityMetaDataReader;
import com.koch.ambeth.xml.orm.blueprint.BlueprintValueObjectConfigReader;
import com.koch.ambeth.xml.orm.blueprint.JavassistOrmEntityTypeProvider;

@BootstrapModule
public class XmlBlueprintModule implements IInitializingModule
{
	@LogInstance
	private ILogger log;
	public static final String BLUEPRINT_META_DATA_READER = "blueprintMetaDataReader";
	public static final String BLUEPRINT_VALUE_OBJECT_READER = "blueprintValueObjectReader";
	public static final String JAVASSIST_ORM_ENTITY_TYPE_PROVIDER = "javassistOrmEntityTypeProvider";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{

		beanContextFactory.registerBean(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER, JavassistOrmEntityTypeProvider.class).precedence(
				PrecedenceType.HIGHEST);
		beanContextFactory.registerBean(XmlBlueprintModule.BLUEPRINT_META_DATA_READER, BlueprintEntityMetaDataReader.class)
				.autowireable(IRuntimeBlueprintEntityMetadataReader.class).precedence(PrecedenceType.HIGH);
		beanContextFactory.registerBean(XmlBlueprintModule.BLUEPRINT_VALUE_OBJECT_READER, BlueprintValueObjectConfigReader.class).autowireable(
				IRuntimeBlueprintVomReader.class);

	}
}
