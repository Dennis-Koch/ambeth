package com.koch.ambeth.mapping;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.AbstractTuple2KeyHashMap;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class ExpansionEntityMapper implements IDedicatedMapper, IPropertyExpansionExtendable
{
	@LogInstance
	private ILogger log;

	private static final String NO_PROPERTY_PATH = "";

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyExpansionProvider propertyExpansionProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	protected Tuple2KeyHashMap<Class<?>, String, PropertyPath> extensions = new Tuple2KeyHashMap<Class<?>, String, PropertyPath>();

	protected Tuple2KeyHashMap<Class<?>, String, String> transparentPropertyPath = new Tuple2KeyHashMap<Class<?>, String, String>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void applySpecialMapping(Object businessObject, Object valueObject, CopyDirection direction)
	{
		Class<? extends Object> transferClass = valueObject.getClass();
		// load properties of transferClass
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(transferClass);
		StringBuilder sb = null;
		IEntityMetaData metaData = ((IEntityMetaDataHolder) businessObject).get__EntityMetaData();
		try
		{
			for (IPropertyInfo propertyInfo : properties)
			{
				// is there a mapping?
				String nestedPath = resolveNestedPath(transferClass, propertyInfo);
				if (nestedPath == null)
				{
					continue;
				}
				// get propertyExpansion for the business object
				Class<?> entityType = metaData.getRealType();
				PropertyExpansion propertyExpansion = propertyExpansionProvider.getPropertyExpansion(entityType, nestedPath);
				// apply mapping
				switch (direction)
				{
					case BO_TO_VO:
						Object convertValueToType = conversionHelper.convertValueToType(propertyInfo.getPropertyType(),
								propertyExpansion.getValue(businessObject));

						propertyInfo.setValue(valueObject, convertValueToType);
						break;
					case VO_TO_BO:
						// find out if the value was specified and we need to write it back

						if (sb == null)
						{
							sb = objectCollector.create(StringBuilder.class);
						}
						sb.setLength(0);
						String voSpecifiedName = sb.append(propertyInfo.getName()).append("Specified").toString();

						IPropertyInfo voSpecifiedMember = propertyInfoProvider.getProperty(transferClass, voSpecifiedName);
						if (voSpecifiedMember != null && !Boolean.TRUE.equals(voSpecifiedMember.getValue(valueObject)))
						{
							continue;
						}

						propertyExpansion.setValue(businessObject, propertyInfo.getValue(valueObject));
						break;
					default:
						throw new IllegalArgumentException("Cannot handel dopy direction " + direction);
				}
			}
		}
		finally
		{
			if (sb != null)
			{
				objectCollector.dispose(sb);
			}
		}
	}

	private String resolveNestedPath(Class<? extends Object> transferClass, IPropertyInfo propertyInfo)
	{
		PropertyPath mapping = extensions.get(transferClass, propertyInfo.getName());
		String nestedPath = mapping != null ? mapping.getPropertyPath() : null;
		if (nestedPath != null)
		{
			return nestedPath;
		}
		nestedPath = transparentPropertyPath.get(transferClass, propertyInfo.getName());
		if (nestedPath != null)
		{
			if (nestedPath != NO_PROPERTY_PATH)
			{
				return nestedPath;
			}
			return null;
		}
		// if there is no explicit mapping done via link-API, find annotation based mappings
		MapEntityNestProperty mapEntityNestProperty = propertyInfo.getAnnotation(MapEntityNestProperty.class);
		if (mapEntityNestProperty == null)
		{
			nestedPath = NO_PROPERTY_PATH;
		}
		else
		{
			String[] nestPathes = mapEntityNestProperty.value();
			StringBuilder nestPathSB = new StringBuilder();
			for (int a = 0, size = nestPathes.length; a < size; a++)
			{
				if (a > 0)
				{
					nestPathSB.append('.');
				}
				nestPathSB.append(nestPathes[a]);
			}
			nestedPath = nestPathSB.toString();
		}

		writeLock.lock();
		try
		{
			transparentPropertyPath = addExtension(nestedPath, transferClass, propertyInfo.getName(), transparentPropertyPath);
		}
		finally
		{
			writeLock.unlock();
		}
		if (nestedPath != NO_PROPERTY_PATH)
		{
			return nestedPath;
		}
		return null;
	}

	protected <V> Tuple2KeyHashMap<Class<?>, String, V> addExtension(V expansionPath, Class<?> transferClass, String propertyName,
			Tuple2KeyHashMap<Class<?>, String, V> givenExtensions)
	{
		// here: COPY-ON-WRITE pattern to be threadsafe with reads without a lock
		Tuple2KeyHashMap<Class<?>, String, V> extensions = new Tuple2KeyHashMap<Class<?>, String, V>(
				(int) (givenExtensions.size() / AbstractTuple2KeyHashMap.DEFAULT_LOAD_FACTOR) + 2);
		extensions.putAll(givenExtensions);
		if (!extensions.putIfNotExists(transferClass, propertyName, expansionPath))
		{
			throw new IllegalStateException("Another extension already registered with the same key");
		}
		return extensions;
	}

	@Override
	public void registerEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName)
	{
		// here: COPY-ON-WRITE pattern to be threadsafe with reads without a lock
		writeLock.lock();
		try
		{
			extensions = addExtension(expansionPath, transferClass, propertyName, extensions);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName)
	{
		// here: COPY-ON-WRITE pattern to be threadsafe with reads without a lock
		writeLock.lock();
		try
		{
			Tuple2KeyHashMap<Class<?>, String, PropertyPath> extensions = new Tuple2KeyHashMap<Class<?>, String, PropertyPath>(
					(int) (this.extensions.size() / AbstractTuple2KeyHashMap.DEFAULT_LOAD_FACTOR) + 2);
			extensions.putAll(this.extensions);
			if (!extensions.removeIfValue(transferClass, propertyName, expansionPath))
			{
				throw new IllegalStateException("Extension not registered with given key");
			}
			this.extensions = extensions;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
