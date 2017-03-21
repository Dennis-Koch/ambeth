package com.koch.ambeth.xml.ioc;

/*-
 * #%L
 * jambeth-xml
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
