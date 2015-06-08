using System;
using System.Collections.Generic;
using System.Xml.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Orm;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;

namespace De.Osthus.Ambeth.Merge.Config
{
public class IndependentEntityMetaDataReader : IStartingBean, IDisposableBean
{
	[LogInstance]
    public ILogger Log { private get; set; }

	[Autowired]
	public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

	[Autowired]
	public IEntityMetaDataExtendable EntityMetaDataExtendable { protected get; set; }

	[Autowired]
	public IEventDispatcher EventDispatcher { protected get; set; }

	[Autowired]
	public IEntityMetaDataReader EntityMetaDataReader { protected get; set; }

	[Autowired]
	public IOrmConfigGroupProvider OrmConfigGroupProvider { protected get; set; }
	
	protected readonly LinkedHashSet<IEntityMetaData> managedEntityMetaData = new LinkedHashSet<IEntityMetaData>();

	protected String xmlFileName = null;

	public void AfterStarted()
	{
		if (xmlFileName != null)
		{
			IOrmConfigGroup ormConfigGroup = OrmConfigGroupProvider.GetOrmConfigGroup(xmlFileName);
			ReadConfig(ormConfigGroup);
		}
	}

	public void Destroy()
	{
		foreach (IEntityMetaData entityMetaData in managedEntityMetaData)
		{
			EntityMetaDataExtendable.UnregisterEntityMetaData(entityMetaData);
		}
	}

	[Property(ServiceConfigurationConstants.MappingFile, Mandatory = false)]
	public String FileName
	{
		set
        {
			if (xmlFileName != null)
			{
				throw new ArgumentException("XmlDatabaseMapper already configured! Tried to set the config file '" + value
						+ "'. File name is already set to '" + xmlFileName + "'");
			}
			xmlFileName = value;
        }
	}

	protected void ReadConfig(IOrmConfigGroup ormConfigGroup)
	{
		LinkedHashSet<IEntityConfig> entities = new LinkedHashSet<IEntityConfig>();
		entities.AddAll(ormConfigGroup.GetLocalEntityConfigs());
		entities.AddAll(ormConfigGroup.GetExternalEntityConfigs());

		foreach (IEntityConfig entityConfig in entities)
		{
			Type entityType = entityConfig.EntityType;
			if (EntityMetaDataProvider.GetMetaData(entityType, true) != null)
			{
				continue;
			}
			Type realType = entityConfig.RealType;

			EntityMetaData metaData = new EntityMetaData();
			metaData.EntityType = entityType;
			metaData.RealType = realType;
			metaData.LocalEntity = entityConfig.Local;

			EntityMetaDataReader.AddMembers(metaData, entityConfig);

			managedEntityMetaData.Add(metaData);
			lock (EntityMetaDataExtendable)
			{
                EntityMetaDataExtendable.RegisterEntityMetaData(metaData);
			}
		}
	}
}
}