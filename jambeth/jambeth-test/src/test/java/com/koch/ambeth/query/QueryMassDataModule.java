package com.koch.ambeth.query;

import java.util.Collection;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class QueryMassDataModule implements IInitializingModule {
	public static class QueryMassDataBuilder extends AbstractDatasetBuilder {
		@Property(name = ROW_COUNT)
		protected int dataCount;

		@Override
		public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
			return null;
		}

		@Override
		protected void buildDatasetInternal() {
			for (int a = 0; a < dataCount; a++) {
				createEntity(QueryEntity.class);
			}
		}
	}

	public static final String ROW_COUNT = "QueryMassdataTest.rowcount";

	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(IQueryEntityCRUD.class).autowireable(IQueryEntityCRUD.class);

		IBeanConfiguration queryMassDataBuilder =
				beanContextFactory.registerBean(QueryMassDataBuilder.class);
		beanContextFactory.link(queryMassDataBuilder).to(IDatasetBuilderExtendable.class);

		IBeanConfiguration queryBeanBC = beanContextFactory.registerBean("myQuery1", QueryBean.class);
		queryBeanBC.propertyValue("EntityType", QueryEntity.class);
		queryBeanBC.propertyValue("QueryCreator", new IQueryCreator() {
			@Override
			public <T> IQuery<T> createCustomQuery(IQueryBuilder<T> qb) {
				return qb.build();
			}
		});
	}

	public void setProxyFactory(IProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}
}
