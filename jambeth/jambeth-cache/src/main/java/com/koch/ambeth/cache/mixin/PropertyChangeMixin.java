package com.koch.ambeth.cache.mixin;

import java.beans.Introspector;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;

import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.databinding.ICollectionChangeExtension;
import com.koch.ambeth.cache.databinding.ICollectionChangeExtensionExtendable;
import com.koch.ambeth.cache.databinding.IPropertyChangeExtension;
import com.koch.ambeth.cache.databinding.IPropertyChangeExtensionExtendable;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ClassExtendableListContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.proxy.IEnhancedType;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.service.typeinfo.PropertyInfoItem;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.annotation.FireTargetOnPropertyChange;
import com.koch.ambeth.util.annotation.FireThisOnPropertyChange;
import com.koch.ambeth.util.annotation.IgnoreToBeUpdated;
import com.koch.ambeth.util.annotation.ParentChild;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.collections.specialized.INotifyCollectionChanged;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedEvent;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.model.IEmbeddedType;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class PropertyChangeMixin
		implements IPropertyChangeExtensionExtendable, ICollectionChangeExtensionExtendable {
	public static final Object UNKNOWN_VALUE = new Object();

	public class PropertyEntry {
		public final String propertyName;

		public final String javaBeansPropertyName;

		public final ITypeInfoItem getDelegate;

		public final boolean doesModifyToBeUpdated;

		public final boolean firesToBeCreatedPCE;

		public final String[] propertyNames;

		public final Object[] unknownValues;

		public final boolean isParentChildSetter;

		public final boolean isAddedRemovedCheckNecessary;

		public final Boolean includeNewValue, includeOldValue;

		public PropertyEntry(Class<?> type, String propertyName, String javaBeansPropertyName,
				IPropertyInfoProvider propertyInfoProvider) {
			this.propertyName = propertyName;
			this.javaBeansPropertyName = javaBeansPropertyName;
			LinkedHashSet<String> propertyNames = new LinkedHashSet<>();
			propertyNames.add(propertyName);
			IPropertyInfo prop = propertyInfoProvider.getProperty(type, propertyName);
			PropertyChangeAspect propertyChangeAspect = null;
			Class<?> currType = type;
			while (IEnhancedType.class.isAssignableFrom(currType)) {
				propertyChangeAspect = currType.getSuperclass().getAnnotation(PropertyChangeAspect.class);
				if (propertyChangeAspect != null) {
					break;
				}
				currType = currType.getSuperclass();
			}
			if (propertyChangeAspect != null) {
				includeNewValue = propertyChangeAspect.includeNewValue();
				includeOldValue = propertyChangeAspect.includeOldValue();
			}
			else {
				includeNewValue = null;
				includeOldValue = null;
			}
			doesModifyToBeUpdated = (IDataObject.class.isAssignableFrom(type)
					|| IEmbeddedType.class.isAssignableFrom(type))
					&& !prop.isAnnotationPresent(IgnoreToBeUpdated.class);
			isParentChildSetter = IDataObject.class.isAssignableFrom(type)
					&& prop.isAnnotationPresent(ParentChild.class);
			isAddedRemovedCheckNecessary = !prop.getPropertyType().isPrimitive()
					&& WrapperTypeSet.getUnwrappedType(prop.getPropertyType()) == null
					&& !String.class.equals(prop.getPropertyType());

			evaluateDependentProperties(type, prop, propertyNames, propertyInfoProvider);

			while (true) {
				int startCount = propertyNames.size();

				for (String currPropertyName : new ArrayList<>(propertyNames)) {
					IPropertyInfo currProp = propertyInfoProvider.getProperty(type, currPropertyName);
					if (currProp.isWritable()) {
						continue;
					}
					// Is is just an evaluating property which has to be re-evaluated because of the change on
					// the current property
					evaluateDependentProperties(type, currProp, propertyNames, propertyInfoProvider);
				}
				if (startCount == propertyNames.size()) {
					break;
				}
			}
			String[] normalPropertyNames = propertyNames.toArray(String.class);
			propertyNames.clear();
			for (String normalPropertyName : normalPropertyNames) {
				propertyNames.add(Introspector.decapitalize(normalPropertyName));
			}
			this.propertyNames = propertyNames.toArray(String.class);
			boolean firesToBeCreatedPCE = false;
			unknownValues = createArrayOfValues(UNKNOWN_VALUE, this.propertyNames.length);
			for (String invokedPropertyName : this.propertyNames) {
				firesToBeCreatedPCE |= "ToBeCreated".equals(invokedPropertyName);
			}
			this.firesToBeCreatedPCE = firesToBeCreatedPCE;
			if (prop.isReadable()) {
				getDelegate = new PropertyInfoItem(prop);
			}
			else {
				getDelegate = null;
			}
		}
	}

	public static Object[] createArrayOfValues(Object value, int length) {
		Object[] values = new Object[length];
		Arrays.fill(values, UNKNOWN_VALUE);
		values[0] = value;
		return values;
	}

	protected static void evaluateDependentProperties(Class<?> type, IPropertyInfo pi,
			Collection<String> propertyNames, IPropertyInfoProvider propertyInfoProvider) {
		FireTargetOnPropertyChange fireTargetOnPropertyChange = pi
				.getAnnotation(FireTargetOnPropertyChange.class);
		if (fireTargetOnPropertyChange != null) {
			for (String attrPropName : fireTargetOnPropertyChange.value()) {
				if (!propertyNames.contains(attrPropName)) {
					propertyNames.add(attrPropName);
				}
			}
		}
		String propertyName = pi.getName();
		for (IPropertyInfo currProp : propertyInfoProvider.getProperties(type)) {
			FireThisOnPropertyChange fireThisOnPropertyChange = currProp
					.getAnnotation(FireThisOnPropertyChange.class);
			if (fireThisOnPropertyChange == null) {
				continue;
			}
			String attrPropName = currProp.getName();
			for (String ftopc_propertyName : fireThisOnPropertyChange.value()) {
				if (propertyName.equals(ftopc_propertyName)) {
					if (!propertyNames.contains(attrPropName)) {
						propertyNames.add(attrPropName);
					}
				}
			}
		}
	}

	protected final SmartCopyMap<IPropertyInfo, PropertyEntry> propertyToEntryMap = new SmartCopyMap<>();

	protected final ClassExtendableListContainer<IPropertyChangeExtension> propertyChangeExtensions = new ClassExtendableListContainer<>(
			"propertyChangeExtension", "entityType");

	protected final ClassExtendableListContainer<ICollectionChangeExtension> collectionChangeExtensions = new ClassExtendableListContainer<>(
			"collectionChangeExtension", "entityType");

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

	protected PropertyEntry getPropertyEntry(Class<?> type, IPropertyInfo property) {
		PropertyEntry entry = propertyToEntryMap.get(property);
		if (entry == null) {
			entry = new PropertyEntry(type, property.getName(), property.getNameForJavaBeans(),
					propertyInfoProvider);
			propertyToEntryMap.put(property, entry);
		}
		return entry;
	}

	public void addPropertyChangeListener(PropertyChangeSupport propertyChangeSupport,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeSupport propertyChangeSupport,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void handleParentChildPropertyChange(INotifyPropertyChangedSource obj,
			PropertyChangeEvent evnt) {
		if (!(obj instanceof IDataObject)) {
			return;
		}
		if (cacheModification.isActiveOrFlushingOrInternalUpdate()) {
			return;
		}
		((IDataObject) obj).setToBeUpdated(true);
	}

	public void handleCollectionChange(INotifyPropertyChangedSource obj,
			NotifyCollectionChangedEvent evnt) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();

		Object source = evnt.getSource();
		Object base = null;
		boolean parentChildProperty = false;

		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
			Object valueDirect = ((IValueHolderContainer) obj).get__ValueDirect(relationIndex);
			if (valueDirect != source) {
				continue;
			}
			if (relationMembers[relationIndex].getAnnotation(ParentChild.class) != null) {
				base = obj;
				parentChildProperty = true;
			}
			else {
				base = source;
			}
			break;
		}
		if (base == null) {
			for (PrimitiveMember primitiveMember : metaData.getPrimitiveMembers()) {
				Object valueDirect = primitiveMember.getValue(obj);
				if (valueDirect != source) {
					continue;
				}
				base = obj;
				parentChildProperty = true;
				break;
			}
		}
		if (base == null) {
			throw new IllegalStateException("Must never happen");
		}
		ICacheModification cacheModification = this.cacheModification;
		boolean oldCacheModification = cacheModification.isActive();
		boolean cacheModificationUsed = false;
		try {
			switch (evnt.getAction()) {
				case Add:
				case Remove:
				case Replace:
					if (evnt.getOldItems() != null) {
						for (Object oldItem : evnt.getOldItems()) {
							handleRemovedItem(obj, oldItem, parentChildProperty);
						}
					}
					if (evnt.getNewItems() != null) {
						for (Object newItem : evnt.getNewItems()) {
							handleAddedItem(obj, newItem, parentChildProperty);
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
			IList<ICollectionChangeExtension> extensions = collectionChangeExtensions
					.getExtensions(obj.getClass());
			if (extensions != null) {
				for (int a = 0, size = extensions.size(); a < size; a++) {
					extensions.get(a).collectionChanged(obj, evnt);
				}
			}
		}
		finally {
			if (!oldCacheModification) {
				setToBeUpdated(obj, true);
			}
			if (cacheModificationUsed) {
				cacheModification.setActive(oldCacheModification);
			}
		}
	}

	public PropertyChangeSupport newPropertyChangeSupport(Object entity) {
		return new PropertyChangeSupport();
	}

	public IPropertyInfo getMethodHandle(INotifyPropertyChangedSource obj, String propertyName) {
		IPropertyInfo property = propertyInfoProvider.getProperty(obj.getClass(), propertyName);
		if (property != null) {
			return property;
		}
		throw new IllegalStateException(
				"Property not found: " + obj.getClass().getName() + "." + propertyName);
	}

	protected void handleRemovedItem(INotifyPropertyChangedSource obj, Object removedItem,
			boolean isParentChildProperty) {
		if (removedItem instanceof INotifyCollectionChanged) {
			((INotifyCollectionChanged) removedItem)
					.removeNotifyCollectionChangedListener(obj.getCollectionEventHandler());
		}
		if (isParentChildProperty && removedItem instanceof INotifyPropertyChanged) {
			((INotifyPropertyChanged) removedItem)
					.removePropertyChangeListener(obj.getParentChildEventHandler());
		}
	}

	protected void handleAddedItem(INotifyPropertyChangedSource obj, Object addedItem,
			boolean isParentChildProperty) {
		if (isParentChildProperty && addedItem instanceof INotifyPropertyChanged) {
			((INotifyPropertyChanged) addedItem)
					.addPropertyChangeListener(obj.getParentChildEventHandler());
		}
		if (addedItem instanceof INotifyCollectionChanged) {
			((INotifyCollectionChanged) addedItem)
					.addNotifyCollectionChangedListener(obj.getCollectionEventHandler());
		}
	}

	// / <summary>
	// / Checks, if the oldValue differs from the new value. If so, the new value is set.
	// / Several EventListeners are deregistered for the old value and registered for the new value.
	// / </summary>
	// / <param name="invocation">IInvocation of the Setter</param>
	// / <param name="getterItem">The getter fitting to the setter in invocation</param>
	public void firePropertyChange(INotifyPropertyChangedSource obj,
			PropertyChangeSupport propertyChangeSupport, IPropertyInfo property, Object oldValue,
			Object currentValue) {
		ICacheModification cacheModification = this.cacheModification;
		PropertyEntry entry = getPropertyEntry(obj.getClass(), property);
		try {
			if (entry.isAddedRemovedCheckNecessary) {
				if (oldValue != null) {
					handleRemovedItem(obj, oldValue, entry.isParentChildSetter);
				}
				if (currentValue != null) {
					handleAddedItem(obj, currentValue, entry.isParentChildSetter);
				}
			}
			String[] propertyNames = entry.propertyNames;

			boolean includeNewValue = entry.includeNewValue != null ? entry.includeNewValue.booleanValue()
					: fireOldPropertyValueActive;
			boolean includeOldValue = entry.includeOldValue != null ? entry.includeOldValue.booleanValue()
					: fireOldPropertyValueActive;

			Object[] oldValues;
			if (includeOldValue) {
				oldValues = createArrayOfValues(oldValue, propertyNames.length);
			}
			else {
				oldValues = entry.unknownValues;
			}
			Object[] currentValues;
			if (includeNewValue) {
				if (includeOldValue && currentValue == oldValue) {
					currentValues = oldValues;
				}
				else {
					currentValues = createArrayOfValues(currentValue, propertyNames.length);
				}
			}
			else {
				currentValues = entry.unknownValues;
			}
			firePropertyChange(obj, propertyNames, oldValues, currentValues);
			if (entry.firesToBeCreatedPCE) {
				IDataObject dObj = (IDataObject) obj;
				if (dObj.isToBeCreated() && dObj.isToBeUpdated()) {
					dObj.setToBeUpdated(false);
				}
			}
		}
		finally {
			if (entry.doesModifyToBeUpdated && !cacheModification.isActiveOrFlushingOrInternalUpdate()) {
				setToBeUpdated(obj, true);
			}
		}
	}

	protected void setToBeUpdated(Object obj, boolean value) {
		if (obj instanceof IEmbeddedType) {
			obj = ((IEmbeddedType) obj).getRoot();
		}
		if (obj instanceof IDataObject) {
			IDataObject dp = (IDataObject) obj;
			if (!dp.isToBeCreated()) {
				dp.setToBeUpdated(value);
			}
		}
	}

	public void firePropertyChange(final INotifyPropertyChangedSource obj,
			final String[] propertyNames, final Object[] oldValues, final Object[] currentValues) {
		final PropertyChangeSupport propertyChangeSupport = obj.getPropertyChangeSupport();
		final IList<IPropertyChangeExtension> extensions = propertyChangeExtensions
				.getExtensions(obj.getClass());
		if (propertyChangeSupport == null && extensions == null
				&& !(obj instanceof PropertyChangeListener)) {
			return;
		}
		if (obj instanceof IDataObject) {
			ICacheModification cacheModification = this.cacheModification;
			if (cacheModification.isActive()) {
				cacheModification.queuePropertyChangeEvent(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Exception {
						executeFirePropertyChange(propertyChangeSupport, extensions, obj, propertyNames,
								oldValues, currentValues);
					}
				});
				return;
			}
		}
		executeFirePropertyChange(propertyChangeSupport, extensions, obj, propertyNames, oldValues,
				currentValues);
	}

	protected void executeFirePropertyChange(final PropertyChangeSupport propertyChangeSupport,
			final IList<IPropertyChangeExtension> extensions, final Object obj,
			final String[] propertyNames, final Object[] oldValues, final Object[] currentValues) {
		if (asyncPropertyChangeActive) {
			guiThreadHelper.invokeInGui(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Exception {
					executeFirePropertyChangeIntern(propertyChangeSupport, extensions, obj, propertyNames,
							oldValues, currentValues);
				}
			});
		}
		else {
			executeFirePropertyChangeIntern(propertyChangeSupport, extensions, obj, propertyNames,
					oldValues, currentValues);
		}
	}

	protected void executeFirePropertyChangeIntern(PropertyChangeSupport propertyChangeSupport,
			IList<IPropertyChangeExtension> extensions, Object obj, String[] propertyNames,
			Object[] oldValues, Object[] currentValues) {
		boolean debugEnabled = log.isDebugEnabled();
		PropertyChangeListener pcl = (PropertyChangeListener) (obj instanceof PropertyChangeListener
				? obj : null);

		for (int a = 0, size = propertyNames.length; a < size; a++) {
			String propertyName = propertyNames[a];
			if (debugEnabled) {
				log.debug("Process PCE '" + propertyName + "' on " + obj);
			}
			Object oldValue = oldValues[a];
			Object currentValue = currentValues[a];
			if (oldValue == UNKNOWN_VALUE) {
				oldValue = null;
			}
			if (currentValue == UNKNOWN_VALUE) {
				currentValue = null;
			}
			PropertyChangeEvent evnt = null;
			if (pcl != null && (oldValue != null || currentValue != null)) {
				// called only in "non-technical" PCEs
				evnt = new PropertyChangeEvent(obj, propertyName, oldValue, currentValue);
				pcl.propertyChange(evnt);
			}
			if (propertyChangeSupport != null) {
				if (evnt != null) {
					propertyChangeSupport.firePropertyChange(evnt);
				}
				else {
					propertyChangeSupport.firePropertyChange(obj, propertyName, oldValue, currentValue);
				}
			}
			for (int b = 0, sizeB = extensions.size(); b < sizeB; b++) {
				extensions.get(b).propertyChanged(obj, propertyName, oldValue, currentValue);
			}
		}
	}

	@Override
	public void registerPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension,
			Class<?> entityType) {
		propertyChangeExtensions.register(propertyChangeExtension, entityType);
	}

	@Override
	public void unregisterPropertyChangeExtension(IPropertyChangeExtension propertyChangeExtension,
			Class<?> entityType) {
		propertyChangeExtensions.unregister(propertyChangeExtension, entityType);
	}

	@Override
	public void registerCollectionChangeExtension(
			ICollectionChangeExtension collectionChangeExtension, Class<?> entityType) {
		collectionChangeExtensions.register(collectionChangeExtension, entityType);
	}

	@Override
	public void unregisterCollectionChangeExtension(
			ICollectionChangeExtension collectionChangeExtension, Class<?> entityType) {
		collectionChangeExtensions.unregister(collectionChangeExtension, entityType);
	}
}
