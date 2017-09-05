package com.koch.ambeth.eclipse.databinding.ioc;

import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.Realm;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.mixin.IPropertyChangeItemListenerExtendable;
import com.koch.ambeth.cache.util.IRelationalCollectionFactory;
import com.koch.ambeth.eclipse.databinding.EclipseDatabindingCollectionFactory;
import com.koch.ambeth.eclipse.databinding.bytecode.EclipseBindingMixin;
import com.koch.ambeth.eclipse.databinding.bytecode.EclipseDatabindingBehavior;
import com.koch.ambeth.eclipse.databinding.config.EclipseDatabindingConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;

public class EclipseDatabindingModule implements IInitializingModule, IPropertyLoadingBean {
	@Autowired(optional = true)
	protected Realm realm;

	@Property(name = EclipseDatabindingConfigurationConstants.Realm, mandatory = false)
	protected Object realmProperty;

	@Override
	public void applyProperties(Properties contextProperties) {
		contextProperties
				.putIfUndefined(CacheConfigurationConstants.OverwriteToManyRelationsInChildCache, "false");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		if (realm == null) {
			if (realmProperty == null) {
				throw new IllegalArgumentException(
						"Either a bean of type '" + Realm.class + "' or a property for key '"
								+ EclipseDatabindingConfigurationConstants.Realm + "' must be specified");
			}
			if (realmProperty instanceof Realm) {
				realm = (Realm) realmProperty;
			}
			else {
				realm = ((Supplier<Realm>) realmProperty).get();
			}
		}
		beanContextFactory.registerBean(EclipseDatabindingCollectionFactory.class)
				.propertyValue(EclipseDatabindingCollectionFactory.P_REALM, realm)
				.autowireable(IRelationalCollectionFactory.class);

		IBeanConfiguration eclipseBindingMixin =
				beanContextFactory.registerBean(EclipseBindingMixin.class)
						.autowireable(EclipseBindingMixin.class);
		beanContextFactory.link(eclipseBindingMixin).to(IPropertyChangeItemListenerExtendable.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory,
				EclipseDatabindingBehavior.class);
	}
}
