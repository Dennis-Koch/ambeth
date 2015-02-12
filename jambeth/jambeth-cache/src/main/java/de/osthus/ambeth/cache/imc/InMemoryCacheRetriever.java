package de.osthus.ambeth.cache.imc;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.copy.IObjectCopier;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.util.IConversionHelper;

public class InMemoryCacheRetriever implements ICacheRetriever, IMergeServiceExtension, ICacheService
{
	protected final HashMap<IObjRef, ILoadContainer> databaseMap = new HashMap<IObjRef, ILoadContainer>();

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjectCopier objectCopier;

	protected final Lock writeLock = new ReentrantLock();

	void addWithKey(LoadContainer lc, String alternateIdMember, Object alternateId)
	{
		IObjRef reference = lc.getReference();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
		byte idIndex = metaData.getIdIndexByMemberName(alternateIdMember);
		alternateId = conversionHelper.convertValueToType(metaData.getAlternateIdMembers()[idIndex].getRealType(), alternateId);
		databaseMap.put(new ObjRef(reference.getRealType(), idIndex, alternateId, reference.getVersion()), lc);
	}

	public IInMemoryConfig add(Class<?> entityType, Object primaryId)
	{
		return add(entityType, primaryId, null);
	}

	public IInMemoryConfig add(Class<?> entityType, Object primaryId, Object version)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		LoadContainer lc = new LoadContainer();
		lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
		lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);

		primaryId = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), primaryId);
		if (metaData.getVersionMember() != null)
		{
			version = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), version);
		}
		lc.setReference(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, primaryId, version));

		databaseMap.put(lc.getReference(), lc);

		return new InMemoryEntryConfig(this, metaData, lc);
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		List<ILoadContainer> result = new ArrayList<ILoadContainer>(orisToLoad.size());
		writeLock.lock();
		try
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
		finally
		{
			writeLock.unlock();
		}
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ICUDResult evaluateImplictChanges(ICUDResult cudResult, IIncrementalMergeState incrementalState)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		throw new UnsupportedOperationException();
	}

	public Class<?> getTargetProviderType(Class<?> clientInterface)
	{
		throw new UnsupportedOperationException();
	}

	public Class<?> getSyncInterceptorType(Class<?> clientInterface)
	{
		throw new UnsupportedOperationException();
	}

	public String getServiceName(Class<?> clientInterface)
	{
		throw new UnsupportedOperationException();
	}

	public void postProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription)
	{
		throw new UnsupportedOperationException();
	}
}