package de.osthus.ambeth.ioc;

import de.osthus.ambeth.bytecode.behavior.CacheMapEntryBehavior;
import de.osthus.ambeth.bytecode.behavior.DataObjectBehavior;
import de.osthus.ambeth.bytecode.behavior.DefaultPropertiesBehavior;
import de.osthus.ambeth.bytecode.behavior.EmbeddedTypeBehavior;
import de.osthus.ambeth.bytecode.behavior.EnhancedTypeBehavior;
import de.osthus.ambeth.bytecode.behavior.EntityEqualsBehavior;
import de.osthus.ambeth.bytecode.behavior.LazyRelationsBehavior;
import de.osthus.ambeth.bytecode.behavior.NotifyPropertyChangedBehavior;
import de.osthus.ambeth.bytecode.behavior.ParentCacheHardRefBehavior;
import de.osthus.ambeth.bytecode.behavior.RootCacheValueBehavior;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.template.DataObjectTemplate;
import de.osthus.ambeth.template.EmbeddedTypeTemplate;
import de.osthus.ambeth.template.PropertyChangeTemplate;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;

@FrameworkModule
public class CacheBytecodeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// cascade $1
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EmbeddedTypeBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EnhancedTypeBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, DefaultPropertiesBehavior.class);
		// cascade $2
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, LazyRelationsBehavior.class);
		// cascade $3
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, NotifyPropertyChangedBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ParentCacheHardRefBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityEqualsBehavior.class);
		// BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, PublicEmbeddedConstructorBehavior.class);
		// cascade $4
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, DataObjectBehavior.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CacheMapEntryBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, RootCacheValueBehavior.class);

		beanContextFactory.registerAnonymousBean(DataObjectTemplate.class).autowireable(DataObjectTemplate.class);
		beanContextFactory.registerAnonymousBean(EmbeddedTypeTemplate.class).autowireable(EmbeddedTypeTemplate.class);
		beanContextFactory.registerAnonymousBean(PropertyChangeTemplate.class).autowireable(PropertyChangeTemplate.class);
		beanContextFactory.registerAnonymousBean(ValueHolderContainerTemplate.class).autowireable(ValueHolderContainerTemplate.class);
	}
}
