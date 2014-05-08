package de.osthus.ambeth.cache.valueholdercontainer;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.copy.IObjectCopier;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.util.ParamChecker;

public class CacheRetrieverMock implements IInitializingBean, ICacheRetriever, IMergeService, ICacheService, IStartingBean
// , IClientServiceFactory
{
	protected final HashMap<IObjRef, ILoadContainer> databaseMap = new HashMap<IObjRef, ILoadContainer>();

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjectCopier objectCopier;

	protected Object reader;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(objectCopier, "ObjectCopier");
	}

	public void setReader(Object reader)
	{
		this.reader = reader;
	}

	@Override
	public void afterStarted()
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Material.class);

		LoadContainer lc = new LoadContainer();
		lc.setReference(new ObjRef(Material.class, 1, 1));
		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);

		lc.getPrimitives()[metaData.getIndexByPrimitiveName("Name")] = "Name1";

		databaseMap.put(lc.getReference(), lc);

		IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(MaterialType.class);
		LoadContainer lc2 = new LoadContainer();
		lc2.setReference(new ObjRef(MaterialType.class, 2, 1));
		lc2.setPrimitives(new Object[metaData2.getPrimitiveMembers().length]);
		lc2.setRelations(new IObjRef[metaData2.getRelationMembers().length][]);

		lc2.getPrimitives()[metaData2.getIndexByPrimitiveName("Name")] = "Name2";

		lc.getRelations()[metaData.getIndexByRelationName("Types")] = new IObjRef[] { lc2.getReference() };

		databaseMap.put(lc2.getReference(), lc2);
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		List<ILoadContainer> result = new ArrayList<ILoadContainer>(orisToLoad.size());
		synchronized (databaseMap)
		{
			for (IObjRef oriToLoad : orisToLoad)
			{
				ILoadContainer lc = databaseMap.get(oriToLoad);
				if (lc == null)
				{
					continue;
				}
				result.add(lc);
			}
			result = objectCopier.clone(result);
		}
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		throw new NotImplementedException();
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		throw new NotImplementedException();
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		throw new NotImplementedException();
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		throw new NotImplementedException();
	}

	public Class<?> getTargetProviderType(Class<?> clientInterface)
	{
		throw new NotImplementedException();
	}

	public Class<?> getSyncInterceptorType(Class<?> clientInterface)
	{
		throw new NotImplementedException();
	}

	public String getServiceName(Class<?> clientInterface)
	{
		throw new NotImplementedException();
	}

	public void postProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory)
	{
		throw new NotImplementedException();
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription)
	{
		throw new NotImplementedException();
	}
}