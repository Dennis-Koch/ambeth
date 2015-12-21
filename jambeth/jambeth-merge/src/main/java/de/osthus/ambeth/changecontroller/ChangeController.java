package de.osthus.ambeth.changecontroller;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableListContainer;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeListener;
import de.osthus.ambeth.merge.MergeHandle;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;

/**
 * A ChangeController listens on all changes that should be persisted by implementing a {@link IMergeListener}.
 * 
 * To use this controller, you have to link it with the {@link de.osthus.ambeth.merge.IMergeExtendable} interface.
 */
public class ChangeController implements IChangeController, IChangeControllerExtendable, IMergeListener
{
	public static final String CLASSNAME = "de.osthus.rtc.egxp.core.changecontrol.ChangeController";

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	protected ThreadLocal<Boolean> edblActiveTL = new ThreadLocal<Boolean>();

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IMergeController mergeController;

	@Property(name = MergeConfigurationConstants.edblActive, defaultValue = "true")
	protected boolean edblActive;

	protected final ClassExtendableListContainer<IChangeControllerExtension<?>> extensions = new ClassExtendableListContainer<IChangeControllerExtension<?>>(
			"change controller extension", "entity");

	protected final SmartCopyMap<Class<?>, IChangeControllerExtension<?>[]> typeToSortedExtensions = new SmartCopyMap<Class<?>, IChangeControllerExtension<?>[]>();

	@Override
	public ICUDResult preMerge(ICUDResult cudResult, ICache cache)
	{
		if (!edblActive || (edblActiveTL.get() != null && Boolean.FALSE.equals(edblActiveTL.get())))
		{
			return cudResult;
		}
		List<IChangeContainer> changes = cudResult.getAllChanges();
		if (!changes.isEmpty() && !extensions.isEmpty())
		{
			// used to lookup the previous values
			IDisposableCache oldCache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE, "ChangeController.PreMerge");
			try
			{
				IObjRef[] references = extractReferences(changes);
				final List<Object> newObjects = cudResult.getOriginalRefs();
				final List<Object> oldObjects = oldCache.getObjects(references, CacheDirective.returnMisses());

				boolean extensionCalled = cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Boolean>()
				{
					@Override
					public Boolean invoke() throws Throwable
					{
						return Boolean.valueOf(processChanges(newObjects, oldObjects));
					}
				}).booleanValue();

				// If no extension has been called, we have no changes and do not need to change the CudResult
				if (extensionCalled)
				{
					// Load all new objects from Cache (maybe there have been some created)
					Collection<Object> objectsToMerge = retrieveChangedObjects(newObjects, cache);
					// A merge handler that contains a reference to the old cache is needed ...
					MergeHandle mergeHandle = beanContext.registerBean(MergeHandle.class).propertyValue("Cache", oldCache)
							.propertyValue("PrivilegedCache", oldCache).finish();
					// ... to create a new CudResult via the mergeController
					cudResult = mergeController.mergeDeep(objectsToMerge, mergeHandle);
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				oldCache.dispose();
			}
		}
		return cudResult;
	}

	protected Collection<Object> retrieveChangedObjects(Collection<Object> objectsBefore, ICache cache)
	{
		// We have at least as many objects as before
		final Set<Object> newObjects = new HashSet<Object>(objectsBefore);
		HandleContentDelegate delegate = new HandleContentDelegate()
		{
			@Override
			public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
			{
				newObjects.add(value);
			}
		};
		cache.getContent(delegate);
		return newObjects;
	}

	/**
	 * Iterate over all entities and process each pair of new and old values.
	 * 
	 * @param newEntities
	 * @param oldEntities
	 * @return true if any of the extensions have been called
	 */
	protected boolean processChanges(List<Object> newEntities, List<Object> oldEntities)
	{
		boolean extensionCalled = false;
		int size = newEntities.size();
		ParamChecker.assertTrue(size == oldEntities.size(), "number of old and new objects should be equal");
		CacheView views = new CacheView(newEntities, oldEntities);
		for (int index = 0; index < size; index += 1)
		{
			Object newEntity = newEntities.get(index);
			boolean toBeDeleted = ((IDataObject) newEntity).isToBeDeleted();
			boolean toBeCreated = false;
			Object oldEntity = oldEntities.get(index);
			if (oldEntity == null)
			{
				toBeCreated = true;
			}
			extensionCalled |= processChange(newEntity, oldEntity, toBeDeleted, toBeCreated, views);
		}
		return extensionCalled;
	}

	/**
	 * Process a single change represented by the new and old version of the object. We look up if there are any extensions registered for the given objects. If
	 * yes, the extensions are called with the change.
	 * 
	 * @param newEntity
	 *            the new version of the entity
	 * @param oldEntity
	 *            the old version of the entity
	 * @param toBeDeleted
	 *            true, if the new entity is to be deleted
	 * @param toBeCreated
	 *            true, if the new entity is to be created
	 * @param views
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected boolean processChange(Object newEntity, Object oldEntity, boolean toBeDeleted, boolean toBeCreated, CacheView views)
	{
		boolean extensionCalled = false;
		// Both objects should be of the same class, so we just need one them. We just have to keep in mind that one of them could be null.
		Class<?> entityType = newEntity != null ? newEntity.getClass() : oldEntity.getClass();
		// Search for registered extensions for the implemented classes
		IChangeControllerExtension<?>[] sortedExtensions = typeToSortedExtensions.get(entityType);
		if (sortedExtensions == null)
		{
			sortedExtensions = extensions.getExtensions(entityType).toArray(IChangeControllerExtension.class);
			Arrays.sort(sortedExtensions);
			typeToSortedExtensions.put(entityType, sortedExtensions);
		}
		for (IChangeControllerExtension ext : sortedExtensions)
		{
			ext.processChange(newEntity, oldEntity, toBeDeleted, toBeCreated, views);
			extensionCalled = true;
		}
		return extensionCalled;
	}

	/**
	 * Create an array that contains all references of the given changes.
	 * 
	 * @param changes
	 * @return the created array, never <code>null</code>
	 */
	protected IObjRef[] extractReferences(List<IChangeContainer> changes)
	{
		IObjRef[] references = new IObjRef[changes.size()];
		for (int a = changes.size(); a-- > 0;)
		{
			IChangeContainer change = changes.get(a);
			if (change instanceof CreateContainer)
			{
				continue;
			}
			references[a] = change.getReference();
		}
		return references;
	}

	@Override
	public void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs)
	{
		// intentionally left blank
	}

	@Override
	public void registerChangeControllerExtension(IChangeControllerExtension<?> extension, Class<?> clazz)
	{
		ParamChecker.assertTrue(clazz.isInterface(), "Currently only interfaces are supported for ChangeControllerExtensions");
		extensions.register(extension, clazz);
		typeToSortedExtensions.clear();
	}

	@Override
	public void unregisterChangeControllerExtension(IChangeControllerExtension<?> extension, Class<?> clazz)
	{
		extensions.unregister(extension, clazz);
		typeToSortedExtensions.clear();
	}

	@Override
	public <T> T runWithoutEDBL(IResultingBackgroundWorkerDelegate<T> runnable) throws Throwable
	{
		Boolean oldValue = edblActiveTL.get();
		try
		{
			edblActiveTL.set(false);
			return runnable.invoke();
		}
		finally
		{
			edblActiveTL.set(oldValue);
		}
	}
}
