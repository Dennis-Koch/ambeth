package de.osthus.ambeth.template;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import de.osthus.ambeth.annotation.FireTargetOnPropertyChange;
import de.osthus.ambeth.annotation.FireThisOnPropertyChange;
import de.osthus.ambeth.annotation.IgnoreToBeUpdated;
import de.osthus.ambeth.annotation.ParentChild;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.specialized.INotifyCollectionChanged;
import de.osthus.ambeth.collections.specialized.NotifyCollectionChangedEvent;
import de.osthus.ambeth.collections.specialized.PropertyChangeSupport;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
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

public class PropertyChangeTemplate
{
	public class PropertyEntry
	{
		public final String propertyName;

		public final ITypeInfoItem getDelegate;

		public final boolean doesModifyToBeUpdated;

		public final boolean firesToBeCreatedPCE;

		public final String[] propertyNames;

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
			for (String invokedPropertyName : propertyNames)
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

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	protected boolean asyncPropertyChangeActive;

	@Property(name = CacheConfigurationConstants.AsyncPropertyChangeActive, defaultValue = "false")
	public void setAsyncPropertyChangeActive(boolean asyncPropertyChangeActive)
	{
		this.asyncPropertyChangeActive = asyncPropertyChangeActive;
	}

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
		if (cacheModification.isActiveOrFlushing())
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
		boolean oldCacheModification = cacheModification.isActive();
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
			firePropertyChange(obj, entry.propertyNames);
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
			if (!oldCacheModification && entry.doesModifyToBeUpdated)
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

	public void firePropertyChange(final INotifyPropertyChangedSource obj, final String[] propertyNames)
	{
		final PropertyChangeSupport propertyChangeSupport = obj.getPropertyChangeSupport();
		if (propertyChangeSupport == null)
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
					executeFirePropertyChange(propertyChangeSupport, obj, propertyNames);
				}
			});
			return;
		}
		executeFirePropertyChange(propertyChangeSupport, obj, propertyNames);
	}

	protected void executeFirePropertyChange(final PropertyChangeSupport propertyChangeSupport, final Object obj, final String[] propertyNames)
	{
		if (asyncPropertyChangeActive)
		{
			guiThreadHelper.invokeInGui(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					executeFirePropertyChangeIntern(propertyChangeSupport, obj, propertyNames);
				}
			});
		}
		else
		{
			executeFirePropertyChangeIntern(propertyChangeSupport, obj, propertyNames);
		}
	}

	protected void executeFirePropertyChangeIntern(PropertyChangeSupport propertyChangeSupport, Object obj, String[] propertyNames)
	{
		boolean debugEnabled = log.isDebugEnabled();
		for (String propertyName : propertyNames)
		{
			if (debugEnabled)
			{
				log.debug("Process PCE '" + propertyName + "' on " + obj);
			}
			PropertyChangeEvent evnt = new PropertyChangeEvent(obj, propertyName, null, null);
			propertyChangeSupport.firePropertyChange(evnt);
		}
	}
}
