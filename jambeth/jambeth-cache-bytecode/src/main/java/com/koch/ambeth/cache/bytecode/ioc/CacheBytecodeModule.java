package com.koch.ambeth.cache.bytecode.ioc;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.bytecode.behavior.CacheMapEntryBehavior;
import com.koch.ambeth.cache.bytecode.behavior.DataObjectBehavior;
import com.koch.ambeth.cache.bytecode.behavior.DefaultPropertiesBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EmbeddedTypeBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EnhancedTypeBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EntityEqualsBehavior;
import com.koch.ambeth.cache.bytecode.behavior.InitializeEmbeddedMemberBehavior;
import com.koch.ambeth.cache.bytecode.behavior.LazyRelationsBehavior;
import com.koch.ambeth.cache.bytecode.behavior.NotifyPropertyChangedBehavior;
import com.koch.ambeth.cache.bytecode.behavior.ParentCacheHardRefBehavior;
import com.koch.ambeth.cache.bytecode.behavior.RootCacheValueBehavior;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

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

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, InitializeEmbeddedMemberBehavior.class);
		// cascade $3
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, NotifyPropertyChangedBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ParentCacheHardRefBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityEqualsBehavior.class);
		// BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, PublicEmbeddedConstructorBehavior.class);
		// cascade $4
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, DataObjectBehavior.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CacheMapEntryBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, RootCacheValueBehavior.class);
	}
}
