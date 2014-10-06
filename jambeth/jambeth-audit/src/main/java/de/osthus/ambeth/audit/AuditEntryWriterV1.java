package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class AuditEntryWriterV1 implements IAuditEntryWriter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void writeAuditEntry(IAuditEntry auditEntry, DataOutputStream os)
	{
		writeProperty(IAuditEntry.User, auditEntry.getUserIdentifier(), os);
		writeProperty(IAuditEntry.Timestamp, auditEntry.getTimestamp(), os);
		writeProperty(IAuditEntry.Protocol, auditEntry.getProtocol(), os);
		for (IAuditedService auditedService : sortAuditServices(auditEntry))
		{
			writeProperty(IAuditedService.ServiceType, auditedService.getServiceType(), os);
			writeProperty(IAuditedService.MethodName, auditedService.getMethodName(), os);
			writeProperty(IAuditedService.SpentTime, auditedService.getSpentTime(), os);
		}
		for (IAuditedEntity auditedEntity : sortAuditEntities(auditEntry))
		{
			writeProperty(IAuditedEntity.EntityType, auditedEntity.getEntityType(), os);
			writeProperty(IAuditedEntity.EntityId, auditedEntity.getEntityId(), os);
			writeProperty(IAuditedEntity.EntityVersion, auditedEntity.getEntityVersion(), os);
			writeProperty(IAuditedEntity.ChangeType, auditedEntity.getChangeType(), os);

			for (IAuditedEntityPrimitiveProperty property : sortAuditedEntityPrimitives(auditedEntity))
			{
				writeProperty(IAuditedEntityPrimitiveProperty.Name, property.getName(), os);
				writeProperty(IAuditedEntityPrimitiveProperty.NewValue, property.getNewValue(), os);
			}
			for (IAuditedEntityRelationProperty property : sortAuditedEntityRelations(auditedEntity))
			{
				writeProperty(IAuditedEntityRelationProperty.Name, property.getName(), os);

				for (IAuditedEntityRelationPropertyItem item : sortAuditedEntityRelationItems(property))
				{
					writeProperty(IAuditedEntityRelationPropertyItem.EntityType, item.getEntityType(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.EntityId, item.getEntityId(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.EntityVersion, item.getEntityVersion(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.ChangeType, item.getChangeType(), os);
				}
			}
		}
	}

	protected void writeProperty(String name, Object value, DataOutputStream os)
	{
		try
		{
			os.writeUTF(name);
			if (value == null)
			{
				os.writeBoolean(false);
			}
			else
			{
				os.writeBoolean(true);
				os.writeUTF(value.toString());
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected List<IAuditedEntity> sortAuditEntities(IAuditEntry auditEntry)
	{
		ArrayList<IAuditedEntity> entities = new ArrayList<IAuditedEntity>(auditEntry.getEntities());

		Collections.sort(entities, new Comparator<IAuditedEntity>()
		{
			@Override
			public int compare(IAuditedEntity o1, IAuditedEntity o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return entities;
	}

	protected List<IAuditedService> sortAuditServices(IAuditEntry auditEntry)
	{
		ArrayList<IAuditedService> services = new ArrayList<IAuditedService>(auditEntry.getServices());

		Collections.sort(services, new Comparator<IAuditedService>()
		{
			@Override
			public int compare(IAuditedService o1, IAuditedService o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return services;
	}

	protected List<IAuditedEntityPrimitiveProperty> sortAuditedEntityPrimitives(IAuditedEntity auditedEntity)
	{
		ArrayList<IAuditedEntityPrimitiveProperty> properties = new ArrayList<IAuditedEntityPrimitiveProperty>(auditedEntity.getPrimitives());

		Collections.sort(properties, new Comparator<IAuditedEntityPrimitiveProperty>()
		{
			@Override
			public int compare(IAuditedEntityPrimitiveProperty o1, IAuditedEntityPrimitiveProperty o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return properties;
	}

	protected List<IAuditedEntityRelationProperty> sortAuditedEntityRelations(IAuditedEntity auditedEntity)
	{
		ArrayList<IAuditedEntityRelationProperty> properties = new ArrayList<IAuditedEntityRelationProperty>(auditedEntity.getRelations());

		Collections.sort(properties, new Comparator<IAuditedEntityRelationProperty>()
		{
			@Override
			public int compare(IAuditedEntityRelationProperty o1, IAuditedEntityRelationProperty o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return properties;
	}

	protected List<IAuditedEntityRelationPropertyItem> sortAuditedEntityRelationItems(IAuditedEntityRelationProperty property)
	{
		ArrayList<IAuditedEntityRelationPropertyItem> items = new ArrayList<IAuditedEntityRelationPropertyItem>(property.getItems());

		Collections.sort(items, new Comparator<IAuditedEntityRelationPropertyItem>()
		{
			@Override
			public int compare(IAuditedEntityRelationPropertyItem o1, IAuditedEntityRelationPropertyItem o2)
			{
				int order1 = o1.getOrder();
				int order2 = o2.getOrder();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return items;
	}
}
