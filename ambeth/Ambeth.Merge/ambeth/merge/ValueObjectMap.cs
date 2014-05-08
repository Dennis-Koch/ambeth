using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Merge
{
    public class ValueObjectMap : SmartCopyMap<Type, List<Type>>, IMapExtendableContainer<Type, IValueObjectConfig>
    {
	    [LogInstance]
	    public ILogger Log { private get; set; }

	    protected readonly MapExtendableContainer<Type, IValueObjectConfig> typeToValueObjectConfig = new MapExtendableContainer<Type, IValueObjectConfig>(
			    "configuration", "value object class");

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }
        	    
	    public IList<IValueObjectConfig> GetExtensions(Type key)
	    {
		    return typeToValueObjectConfig.GetExtensions(key);
	    }
	    
	    public ILinkedMap<Type, IValueObjectConfig> GetExtensions()
	    {
		    return typeToValueObjectConfig.GetExtensions();
	    }

	    public IList<Type> GetValueObjectTypesByEntityType(Type entityType)
	    {
		    List<Type> valueObjectTypes = Get(entityType);
		    if (valueObjectTypes == null)
		    {
			    // Check if the entityType is really an entity type
			    if (EntityMetaDataProvider.GetMetaData(entityType, true) == null)
			    {
				    throw new Exception("'" + entityType + "' is no valid entity type");
			    }
			    return Type.EmptyTypes;
		    }
		    List<Type> resultList = new List<Type>(valueObjectTypes.Count);
		    for (int a = 0, size = valueObjectTypes.Count; a < size; a++)
		    {
			    Type valueObjectType = valueObjectTypes[a];
			    resultList.Add(valueObjectType);
		    }
		    return resultList;
	    }
	    
	    public IValueObjectConfig GetExtension(Type key)
	    {
		    return typeToValueObjectConfig.GetExtension(key);
	    }
	    
	    public void GetExtensions(IMap<Type, IValueObjectConfig> targetExtensionMap)
	    {
		    typeToValueObjectConfig.GetExtensions(targetExtensionMap);
	    }
	    
	    public void Register(IValueObjectConfig config, Type key)
	    {
		    Type entityType = config.EntityType;
		    Type valueType = config.ValueType;

		    Object writeLock = GetWriteLock();
		    lock (writeLock)
		    {
			    typeToValueObjectConfig.Register(config, valueType);

			    // Clone list because of SmartCopy behavior
			    List<Type> valueObjectTypes = Get(entityType);
			    if (valueObjectTypes == null)
			    {
				    valueObjectTypes = new List<Type>(1);
			    }
			    else
			    {
				    valueObjectTypes = new List<Type>(valueObjectTypes);
			    }
			    valueObjectTypes.Add(valueType);
			    Put(entityType, valueObjectTypes);
		    }
	    }
	    
	    public void Unregister(IValueObjectConfig config, Type key)
	    {
		    Type entityType = config.EntityType;
		    Type valueType = config.ValueType;

		    Object writeLock = GetWriteLock();
		    lock (writeLock)
		    {
			    typeToValueObjectConfig.Unregister(config, valueType);
			    List<Type> valueObjectTypes = Get(entityType);
			    valueObjectTypes.Remove(valueType);
			    if (valueObjectTypes.Count == 0)
			    {
				    Remove(entityType);
			    }
		    }
	    }
    }
}