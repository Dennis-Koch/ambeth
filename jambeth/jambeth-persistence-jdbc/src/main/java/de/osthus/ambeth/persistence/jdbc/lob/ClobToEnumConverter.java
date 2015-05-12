package de.osthus.ambeth.persistence.jdbc.lob;

import java.sql.Clob;

import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.EntityMetaDataRemovedEvent;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;

public class ClobToEnumConverter extends ClobToAnythingConverter
{
	public static final String HANDLE_ENTITY_META_DATA_ADDED_EVENT = "handleEntityMetaDataAddedEvent";

	public static final String HANDLE_ENTITY_META_DATA_REMOVED_EVENT = "handleEntityMetaDataRemovedEvent";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDedicatedConverterExtendable dedicatedConverterExtendable;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected final SmartCopyMap<Class<?>, Integer> propertyTypeToUsageCountMap = new SmartCopyMap<Class<?>, Integer>(0.5f);

	public void handleEntityMetaDataAddedEvent(EntityMetaDataAddedEvent evnt)
	{
		for (Class<?> entityType : evnt.getEntityTypes())
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			for (PrimitiveMember member : metaData.getPrimitiveMembers())
			{
				Class<?> elementType = member.getElementType();
				if (!elementType.isEnum())
				{
					continue;
				}
				Integer usageCount = propertyTypeToUsageCountMap.get(elementType);
				if (usageCount == null)
				{
					usageCount = Integer.valueOf(1);
					dedicatedConverterExtendable.registerDedicatedConverter(this, Clob.class, elementType);
					dedicatedConverterExtendable.registerDedicatedConverter(this, elementType, Clob.class);
				}
				else
				{
					usageCount = Integer.valueOf(usageCount.intValue() + 1);
				}
				propertyTypeToUsageCountMap.put(elementType, usageCount);
			}
		}
	}

	public void handleEntityMetaDataRemovedEvent(EntityMetaDataRemovedEvent evnt)
	{
		for (Class<?> entityType : evnt.getEntityTypes())
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			for (PrimitiveMember member : metaData.getPrimitiveMembers())
			{
				Class<?> elementType = member.getElementType();
				if (!elementType.isEnum())
				{
					continue;
				}
				Integer usageCount = propertyTypeToUsageCountMap.get(elementType);
				if (usageCount == null)
				{
					throw new IllegalStateException("Must never happen");
				}
				usageCount = Integer.valueOf(usageCount.intValue() - 1);
				if (usageCount.intValue() > 0)
				{
					dedicatedConverterExtendable.unregisterDedicatedConverter(this, Clob.class, elementType);
					dedicatedConverterExtendable.unregisterDedicatedConverter(this, elementType, Clob.class);
				}
				propertyTypeToUsageCountMap.put(elementType, usageCount);
			}
		}
	}
}
