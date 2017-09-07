package com.koch.ambeth.eclipse.databinding.ioc;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.mixin.IPropertyChangeItemListenerExtendable;
import com.koch.ambeth.cache.util.IRelationalCollectionFactory;
import com.koch.ambeth.eclipse.databinding.EclipseDatabindingCollectionFactory;
import com.koch.ambeth.eclipse.databinding.IRealmHolder;
import com.koch.ambeth.eclipse.databinding.RealmHolder;
import com.koch.ambeth.eclipse.databinding.bytecode.EclipseBindingMixin;
import com.koch.ambeth.eclipse.databinding.bytecode.EclipseDatabindingBehavior;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;

public class EclipseDatabindingModule implements IInitializingModule, IPropertyLoadingBean {
	@Override
	public void applyProperties(Properties contextProperties) {
		contextProperties
				.putIfUndefined(CacheConfigurationConstants.OverwriteToManyRelationsInChildCache, "false");
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(RealmHolder.class).autowireable(IRealmHolder.class);

		beanContextFactory.registerBean(EclipseDatabindingCollectionFactory.class)
				.autowireable(IRelationalCollectionFactory.class);

		IBeanConfiguration eclipseBindingMixin =
				beanContextFactory.registerBean(EclipseBindingMixin.class)
						.autowireable(EclipseBindingMixin.class);
		beanContextFactory.link(eclipseBindingMixin).to(IPropertyChangeItemListenerExtendable.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory,
				EclipseDatabindingBehavior.class);
	}
}
