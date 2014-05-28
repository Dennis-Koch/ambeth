package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.ParamChecker;

public class ObjRefFutureHandler implements IObjectFutureHandler, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ICache cache;

	protected IEntityFactory entityFactory;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(entityFactory, "EntityFactory");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setEntityFactory(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
	}

	@Override
	public void handle(IList<IObjectFuture> objectFutures)
	{
		IEntityFactory entityFactory = this.entityFactory;
		IList<IObjRef> oris = new ArrayList<IObjRef>(objectFutures.size());
		// ObjectFutures have to be handled in order
		for (int i = 0, size = objectFutures.size(); i < size; i++)
		{
			IObjectFuture objectFuture = objectFutures.get(i);
			if (!(objectFuture instanceof ObjRefFuture))
			{
				throw new IllegalArgumentException("'" + getClass().getName() + "' cannot handle " + IObjectFuture.class.getSimpleName()
						+ " implementations of type '" + objectFuture.getClass().getName() + "'");
			}

			ObjRefFuture objRefFuture = (ObjRefFuture) objectFuture;
			IObjRef ori = objRefFuture.getOri();
			if (ori.getId() != null)
			{
				oris.add(ori);
			}
			else if (ori instanceof IDirectObjRef && ((IDirectObjRef) ori).getDirect() != null)
			{
				Object entity = ((IDirectObjRef) ori).getDirect();
				objRefFuture.setValue(entity);
				oris.add(null);
			}
			else
			{
				try
				{
					Object newEntity = entityFactory.createEntity(ori.getRealType());
					objRefFuture.setValue(newEntity);
					oris.add(null);
				}
				catch (Exception e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}

		IList<Object> objects = cache.getObjects(oris, CacheDirective.returnMisses());

		// ObjectFutures have to be handled in order
		for (int i = 0, size = objectFutures.size(); i < size; i++)
		{
			if (oris.get(i) == null)
			{
				continue;
			}

			ObjRefFuture objRefFuture = (ObjRefFuture) objectFutures.get(i);
			Object object = objects.get(i);
			objRefFuture.setValue(object);
		}
	}
}
