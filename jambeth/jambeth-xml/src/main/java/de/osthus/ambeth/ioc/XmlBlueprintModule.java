package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.blueprint.BlueprintEntityMetaDataReader;
import de.osthus.ambeth.orm.blueprint.BlueprintValueObjectConfigReader;
import de.osthus.ambeth.orm.blueprint.JavassistOrmEntityTypeProvider;

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

		beanContextFactory.registerBean(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER, JavassistOrmEntityTypeProvider.class);
		beanContextFactory.registerBean(XmlBlueprintModule.BLUEPRINT_META_DATA_READER, BlueprintEntityMetaDataReader.class);
		beanContextFactory.registerBean(XmlBlueprintModule.BLUEPRINT_VALUE_OBJECT_READER, BlueprintValueObjectConfigReader.class);

	}
}
