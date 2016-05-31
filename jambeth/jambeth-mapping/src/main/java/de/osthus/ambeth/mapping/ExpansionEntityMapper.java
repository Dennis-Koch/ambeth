package de.osthus.ambeth.mapping;

import java.util.Arrays;
import java.util.List;

import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;

public class ExpansionEntityMapper implements IDedicatedMapper, IPropertyExpansionExtendable
{
	protected Tuple2KeyHashMap<Class<?>, String, PropertyPath> extensions = new Tuple2KeyHashMap<Class<?>, String, PropertyPath>();
	@Autowired
	IEntityMetaDataProvider entityMetaDataProvider;

	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	IPropertyExpansionProvider propertyExpansionProvider;

	@Autowired
	IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void applySpecialMapping(Object businessObject, Object valueObject, CopyDirection direction)
	{

		Class<? extends Object> transferClass = valueObject.getClass();
		// load properties of transferClass
		List<IPropertyInfo> properties = Arrays.asList(propertyInfoProvider.getProperties(transferClass));
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		IEntityMetaData metaData = ((IEntityMetaDataHolder) businessObject).get__EntityMetaData();

		try
		{
			for (int i = 0; i < properties.size(); i++)
			{
				IPropertyInfo propertyInfo = properties.get(i);

				// is there a mapping?
				PropertyPath mapping = extensions.get(transferClass, propertyInfo.getName());
				String nestedPath = mapping != null ? mapping.getPropetyPath() : null;
				// if there is not mapping set with link to with, then find by annotation
				if (mapping == null && propertyInfo.isAnnotationPresent(MapEntityNestProperty.class))
				{
					MapEntityNestProperty mapEntityNestProperty = propertyInfo.getAnnotation(MapEntityNestProperty.class);
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

				if (nestedPath != null)
				{
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
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	@Override
	public void registerEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName)
	{
		extensions.put(transferClass, propertyName, expansionPath);
	}

	@Override
	public void unregisterEntityExpansionExtension(PropertyPath expansionPath, Class<?> transferClass, String propertyName)
	{
		extensions.remove(transferClass, propertyName);
	}
}
