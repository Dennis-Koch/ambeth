package de.osthus.ambeth.template;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;

import de.osthus.ambeth.annotation.FireTargetOnPropertyChange;
import de.osthus.ambeth.annotation.FireThisOnPropertyChange;
import de.osthus.ambeth.annotation.IgnoreToBeUpdated;
import de.osthus.ambeth.annotation.ParentChild;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.specialized.INotifyCollectionChanged;
import de.osthus.ambeth.collections.specialized.NotifyCollectionChangedEvent;
import de.osthus.ambeth.collections.specialized.PropertyChangeSupport;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.databinding.ICollectionChangeExtendable;
import de.osthus.ambeth.databinding.ICollectionChangeExtension;
import de.osthus.ambeth.databinding.IPropertyChangeExtendable;
import de.osthus.ambeth.databinding.IPropertyChangeExtension;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableListContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.model.IEmbeddedType;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.model.INotifyPropertyChangedSource;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;
import de.osthus.ambeth.util.ImmutableTypeSet;

public class PropertyChangeTemplate implements IPropertyChangeExtendable, ICollectionChangeExtendable
{
	public static final Object UNKNOWN_VALUE = new Object();

	public class PropertyEntry
	{
		public final String propertyName;

		public final ITypeInfoItem getDelegate;

		public final boolean doesModifyToBeUpdated;

		public final boolean firesToBeCreatedPCE;

		public final String[] propertyNames;

		public final Object[] unknownValues;

		public final boolean isParentChildSetter;

		public final boolean isAddedRemovedCheckNecessary;

		public PropertyEntry(Class<?> type, String propertyName, IPropertyInfoProvider propertyInfoProvider)
		{
			this.propertyName = propertyName;
			LinkedHashSet<String> propertyNames = new LinkedHashSet<String>();
			propertyNames.add(propertyName);
			IPropertyInfo prop = propertyInfoProvider.getProperty(type, propertyName);
			doesModifyToBeUpdated = !prop.isAnnotationPresent(IgnoreToBeUpdated.class);
			isParentChildSetter = prop.isAnnotationPresent(ParentChild.class);
			isAddedRemovedCheckNecessary = !prop.getPropertyType().isPrimitive() && ImmutableTypeSet.getUnwrappedType(prop.getPropertyType()) == null
					&& !String.class.equals(prop.getPropertyType());

			evaluateDependentProperties(type, prop, propertyNames, propertyInfoProvider);

			while (true)
			{
				int startCount = propertyNames.size();

				for (String currPropertyName : new ArrayList<String>(propertyNames))
				{
					IPropertyInfo currProp = propertyInfoProvider.getProperty(type, currPropertyName);
					if (currProp.isWritable())
					{
						continue;
					}
					// Is is just an evaluating property which has to be re-evaluated because of the change on the current property
					evaluateDependentProperties(type, currProp, propertyNames, propertyInfoProvider);
				}
				if (startCount == propertyNames.size())
				{
					break;
				}
			}
			this.propertyNames = propertyNames.toArray(String.class);
			boolean firesToBeCreatedPCE = false;
			unknownValues = createArrayOfValues(UNKNOWN_VALUE, this.propertyNames.length);
			for (String invokedPropertyName : this.propertyNames)
			{
				firesToBeCreatedPCE |= "ToBeCreated".equals(invokedPropertyName);
			}
			this.firesToBeCreatedPCE = firesToBeCreatedPCE;
			if (prop.isReadable())
			{
				getDelegate = new PropertyInfoItem(prop);
			}
			else
			{
				getDelegate = null;
			}
		}
	}

	public static Object[] createArrayOfValues(Object value, int length)
	{
		Object[] values = new Object[length];
		Arrays.fill(values, UNKNOWN_VALUE);
		values[0] = value;
		return values;
	}

	protected static void evaluateDependentProperties(Class<?> type, IPropertyInfo pi, Collection<String> propertyNames,
			IPropertyInfoProvider propertyInfoProvider)
	{
		FireTargetOnPropertyChange fireTargetOnPropertyChange = pi.getAnnotation(FireTargetOnPropertyChange.class);
		if (fireTargetOnPropertyChange != null)
		{
			for (String attrPropName : fireTargetOnPropertyChange.value())
			{
				if (!propertyNames.contains(attrPropName))
				{
					propertyNames.add(attrPropName);
				}
			}
		}
		String propertyName = pi.getName();
		for (IPropertyInfo currProp : propertyInfoProvider.getProperties(type))
		{
			FireThisOnPropertyChange fireThisOnPropertyChange = currProp.getAnnotation(FireThisOnPropertyChange.class);
			if (fireThisOnPropertyChange == null)
			{
				continue;
			}
			String attrPropName = currProp.getName();
			for (String ftopc_propertyName : fireThisOnPropertyChange.value())
			{
				if (propertyName.equals(ftopc_propertyName))
				{
					if (!propertyNames.contains(attrPropName))
					{
						propertyNames.add(attrPropName);
					}
				}
			}
		}
	}

	protected final SmartCopyMap<IPropertyInfo, PropertyEntry> propertyToEntryMap = new SmartCopyMap<IPropertyInfo, PropertyEntry>();

	protected final ClassExtendableListContainer<IPropertyChangeExtension> propertyChangeExtensions = new ClassExtendableListContainer<IPropertyChangeExtension>(
			"propertyChangeExtension", "entityType");

	protected final ClassExtendableListContainer<ICollectionChangeExtension> collectionChangeExtensions = new ClassExtendableListContainer<ICollectionChangeExtension>(
			"collectionChangeExtension", "entityType");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Property(name = CacheConfigurationConstants.AsyncPropertyChangeActive, defaultValue = "false")
	protected boolean asyncPropertyChangeActive;

	@Property(name = CacheConfigurationConstants.FireOldPropertyValueActive, defaultValue = "false")
	protected boolean fireOldPropertyValueActive;

	protected PropertyEntry getPropertyEntry(Class<?> type, IPropertyInfo property)
	{
		PropertyEntry entry = propertyToEntryMap.get(property);
		if (entry == null)
		{
			entry = new PropertyEntry(type, property.getName(), propertyInfoProvider);
			propertyToEntryMap.put(property, entry);
		}
		return entry;
	}

	public void addPropertyChangeListener(PropertyChangeSupport propertyChangeSupport, PropertyChangeListener listener)
	{
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeSupport propertyChangeSupport, PropertyChangeListener listener)
	{
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void handleParentChildPropertyChange(INotifyPropertyChangedSource obj, PropertyChangeEvent evnt)
	{
		if (cacheModification.isActiveOrFlushingOrInternalUpdate())
		{
			return;
		}
		setToBeUpdated(obj, true);
	}

	public void handleCollectionChange(INotifyPropertyChangedSource obj, NotifyCollectionChangedEvent evnt)
	{
		ICacheModification cacheModification = this.cacheModification;
		boolean oldCacheModification = cacheModification.isActive();
		boolean cacheModificationUsed = false;
		try
		{
			switch (evnt.getAction())
			{
				case Add:
				case Remove:
				case Replace:
					if (evnt.getOldItems() != null)
					{
						for (Object oldItem : evnt.getOldItems())
						{
							handleRemovedItem(obj, oldItem, true);
						}
					}
					if (evnt.getNewItems() != null)
					{
						for (Object newItem : evnt.getNewItems())
						{
							handleAddedItem(obj, newItem, true);
						}
					}
					break;
				case Move:
					// Nothing to do in that case
					break;
				case Reset:
					throw new UnsupportedOperationException("Reset is not allowed in a managed collection");
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(evnt.getAction());
			}
			IList<ICollectionChangeExtension> extensions = collectionChangeExtensions.getExtensions(obj.getClass());
			if (extensions != null)
			{
				for (int a = 0, size = extensions.size(); a < size; a++)
				{
					extensions.get(a).collectionChanged(obj, evnt);
				}
			}
		}
		finally
		{
			if (!oldCacheModification)
			{
				setToBeUpdated(obj, true);
			}
			if (cacheModificationUsed)
			{
				cacheModification.setActive(oldCacheModification);
			}
		}
	}

	public PropertyChangeSupport newPropertyChangeSupport(Object entity)
	{
		return new PropertyChangeSupport();
	}

	public IPropertyInfo getMethodHandle(INotifyPropertyChangedSource obj, String propertyName)
	{
		IPropertyInfo property = propertyInfoProvider.getProperty(obj.getClass(), propertyName);
		if (property != null)
		{
			return property;
		}
		throw new IllegalStateException("Property not found: " + obj.getClass().getName() + "." + propertyName);
	}

	protected void handleRemovedItem(INotifyPropertyChangedSource obj, Object removedItem, boolean isParentChildProperty)
	{
		if (removedItem instanceof INotifyCollectionChanged)
		{
			((INotifyCollectionChanged) removedItem).removeNotifyCollectionChangedListener(obj.getCollectionEventHandler());
		}
		if (isParentChildProperty && removedItem instanceof INotifyPropertyChanged)
		{
			((INotifyPropertyChanged) removedItem).removePropertyChangeListener(obj.getParentChildEventHandler());
		}
	}

	protected void handleAddedItem(INotifyPropertyChangedSource obj, Object addedItem, boolean isParentChildProperty)
	{
		if (isParentChildProperty && addedItem instanceof INotifyPropertyChanged)
		{
			((INotifyPropertyChanged) addedItem).addPropertyChangeListener(obj.getParentChildEventHandler());
		}
		if (addedItem instanceof INotifyCollectionChanged)
		{
			((INotifyCollectionChanged) addedItem).addNotifyCollectionChangedListener(obj.getCollectionEventHandler());
		}
	}

	// / <summary>
	// / Checks, if the oldValue differs from the new value. If so, the new value is set.
	// / Several EventListeners are deregistered for the old value and registered for the new value.
	// / </summary>
	// / <param name="invocation">IInvocation of the Setter</param>
	// / <param name="getterItem">The getter fitting to the setter in invocation</param>
	public void firePropertyChange(INotifyPropertyChangedSource obj, PropertyChangeSupport propertyChangeSupport, IPropertyInfo property, Object oldValue,
			Object currentValue)
	{
		ICacheModification cacheModification = this.cacheModification;
		PropertyEntry entry = getPropertyEntry(obj.getClass(), property);
		currentValue = entry.getDelegate.getValue(obj);
		try
		{
			if (entry.isAddedRemovedCheckNecessary)
			{
				if (oldValue != null)
				{
					handleRemovedItem(obj, oldValue, entry.isParentChildSetter);
				}
				if (currentValue != null)
				{
					handleAddedItem(obj, currentValue, entry.isParentChildSetter);
				}
			}
			String[] propertyNames = entry.propertyNames;
			Object[] oldValues;
			Object[] currentValues;

			// if (fireOldPropertyValueActive)
			{
				oldValues = createArrayOfValues(oldValue, propertyNames.length);
				currentValues = currentValue == oldValue ? oldValues : createArrayOfValues(currentValue, propertyNames.length);
			}
			// else
			// {
			// oldValues = entry.unknownValues;
			// currentValues = oldValues;
			// }
			firePropertyChange(obj, propertyNames, oldValues, currentValues);
			if (entry.firesToBeCreatedPCE)
			{
				IDataObject dObj = (IDataObject) obj;
				if (dObj.isToBeCreated() && dObj.isToBeUpdated())
				{
					dObj.setToBeUpdated(false);
				}
			}
		}
		finally
		{
			if (entry.doesModifyToBeUpdated && !cacheModification.isActiveOrFlushingOrInternalUpdate())
			{
				setToBeUpdated(obj, true);
			}
		}
	}

	protected void setToBeUpdated(Object obj, boolean value)
	{
		if (obj instanceof IEmbeddedType)
		{
			obj = ((IEmbeddedType) obj).getRoot();
		}
		if (obj instanceof IDataObject)
		{
			IDataObject dp = (IDataObject) obj;
			if (!dp.isToBeCreated())
			{
				dp.setToBeUpdated(value);
			}
		}
	}

	public void firePropertyChange(final INotifyPropertyChangedSource obj, final String[] propertyNames, final Object[] oldValues, final Object[] currentValues)
	{
		final PropertyChangeSupport propertyChangeSupport = obj.getPropertyChangeSupport();
		final IList<IPropertyChangeExtension> extensions = propertyChangeExtensions.getExtensions(obj.getClass());
		if (propertyChangeSupport == null && extensions == null)
		{
			return;
		}
		ICacheModification cacheModification = this.cacheModification;
		if (cacheModification.isActive())
		{
			cacheModification.queuePropertyChangeEvent(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					executeFirePropertyChange(propertyChangeSupport, extensions, obj, propertyNames, oldValues, currentValues);
				}
			});
			return;
		}
		executeFirePropertyChange(propertyChangeSupport, extensions, obj, propertyNames, oldValues, currentValues);
	}

	protected void executeFirePropertyChange(final PropertyChangeSupport propertyChangeSupport, final IList<IPropertyChangeExtension> extensions,
			final Object obj, final String[] propertyNames, final Object[] oldValues, final Object[] currentValues)
	{
		if (asyncPropertyChangeActive)
		{
			guiThreadHelper.invokeInGui(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					executeFirePropertyChangeIntern(propertyChangeSupport, extensions, obj, propertyNames, oldValues, currentValues);
				}
			});
		}
		else
		{
			executeFirePropertyChangeIntern(propertyChangeSupport, extensions, obj, propertyNames, oldValues, currentValues);
		}
	}

	protected void executeFirePropertyChangeIntern(PropertyChangeSupport propertyChangeSupport, IList<IPropertyChangeExtension> extensions, Object obj,
			String[] propertyNames, Object[] oldValues, Object[] currentValues)
	{
		boolean debugEnabled = log.isDebugEnabled();

		for (int a = 0, size = propertyNames.length; a < size; a++)
		{
			String propertyName = propertyNames[a];
			if (debugEnabled)
			{
				log.debug("Process PCE '" + propertyName + "' on " + obj);
			}
			Object oldValue = oldValues[a];
			Object currentValue = currentValues[a];
			if (oldValue == UNKNOWN_VALUE)
			{
				oldValue = null;
			}
			if (currentValue == UNKNOWN_VALUE)
			{
				currentValue = null;
			}
			if (propertyChangeSupport != null)
			{
				propertyChangeSupport.firePropertyChange(obj, propertyName, oldValue, currentValue);
			}
			if (extensions != null)
			{
				for (int b = 0, sizeB = extensions.size(); b < sizeB; b++)
				{
					extensions.get(b).propertyChanged(obj, propertyName, oldValue, currentValue);
				}
			}
		}
	}

	@Override
	public void registerPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Class<?> entityType)
	{
		propertyChangeExtensions.register(propertyChangeExtension, entityType);
	}

	@Override
	public void unregisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension, Class<?> entityType)
	{
		propertyChangeExtensions.unregister(propertyChangeExtension, entityType);
	}

	@Override
	public void registerCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType)
	{
		collectionChangeExtensions.register(collectionChangeExtension, entityType);
	}

	@Override
	public void unregisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType)
	{
		collectionChangeExtensions.unregister(collectionChangeExtension, entityType);
	}
}
