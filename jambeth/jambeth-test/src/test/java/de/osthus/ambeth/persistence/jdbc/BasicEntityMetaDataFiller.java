package de.osthus.ambeth.persistence.jdbc;

import java.util.Map;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.EntityMetaDataFake;
import de.osthus.ambeth.merge.IEntityMetaDataFiller;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;

public class BasicEntityMetaDataFiller implements IEntityMetaDataFiller
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IPropertyInfoProvider propertyInfoProvider;

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void fillMetaData(EntityMetaDataFake emd)
	{
		Map<String, IPropertyInfo> properties;

		properties = propertyInfoProvider.getPropertyMap(Material.class);
		emd.addMetaData(Material.class, new PropertyInfoItem(properties.get("Id")), new PropertyInfoItem(properties.get("Version")),
				new ITypeInfoItem[] { new PropertyInfoItem(properties.get("Name")) },
				new IRelationInfoItem[] { new PropertyInfoItem(properties.get("MaterialGroup")), new PropertyInfoItem(properties.get("Unit")) });

		properties = propertyInfoProvider.getPropertyMap(MaterialGroup.class);
		emd.addMetaData(MaterialGroup.class, new PropertyInfoItem(properties.get("Id")), new PropertyInfoItem(properties.get("Version")),
				new ITypeInfoItem[] { new PropertyInfoItem(properties.get("Name")) }, new IRelationInfoItem[] {});

		properties = propertyInfoProvider.getPropertyMap(Unit.class);
		emd.addMetaData(Unit.class, new PropertyInfoItem(properties.get("Id")), new PropertyInfoItem(properties.get("Version")),
				new ITypeInfoItem[] { new PropertyInfoItem(properties.get("Name")) }, new IRelationInfoItem[] {});
	}
}
