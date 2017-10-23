package com.koch.ambeth.security.xml;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.privilege.transfer.TypePropertyPrivilegeOfService;
import com.koch.ambeth.xml.ITypeBasedHandlerExtendable;
import com.koch.ambeth.xml.ioc.XmlModule;

@FrameworkModule
public class SecurityXmlModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration instantTypeHandlerBC =
				beanContextFactory.registerBean(TypePropertyPrivilegeOfServiceHandler.class)
						.parent("abstractElementHandler");
		beanContextFactory.link(instantTypeHandlerBC)
				.to(XmlModule.CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class)
				.with(TypePropertyPrivilegeOfService.class);
	}
}
