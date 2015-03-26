package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;

public class AuditEntryWriterV1 implements IAuditEntryWriter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void writeAuditEntry(IAuditEntry auditEntry, DataOutputStream os)
	{
		writeProperty(IAuditEntry.UserIdentifier, auditEntry.getUserIdentifier(), os);
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
			IAuditedEntityRef ref = auditedEntity.getRef();
			writeProperty(IAuditedEntityRef.EntityType, ref.getEntityType(), os);
			writeProperty(IAuditedEntityRef.EntityId, ref.getEntityId(), os);
			writeProperty(IAuditedEntityRef.EntityVersion, ref.getEntityVersion(), os);
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
					IAuditedEntityRef itemRef = item.getRef();
					writeProperty(IAuditedEntityRef.EntityType, itemRef.getEntityType(), os);
					writeProperty(IAuditedEntityRef.EntityId, itemRef.getEntityId(), os);
					writeProperty(IAuditedEntityRef.EntityVersion, itemRef.getEntityVersion(), os);
					writeProperty(IAuditedEntityRelationPropertyItem.ChangeType, item.getChangeType(), os);
				}
			}
		}
	}

	protected void writeProperty(String name, IPrimitiveUpdateItem[] fullPUIs, IEntityMetaData metaData, DataOutputStream os)
	{
		writeProperty(name, fullPUIs[metaData.getIndexByPrimitiveName(name)], os);
	}

	@Override
	public void writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, DataOutputStream os) throws Throwable
	{
		IEntityMetaData auditEntryMetaData = entityMetaDataProvider.getMetaData(IAuditEntry.class);
		IEntityMetaData auditedServiceMetaData = entityMetaDataProvider.getMetaData(IAuditedService.class);
		IEntityMetaData auditedEntityMetaData = entityMetaDataProvider.getMetaData(IAuditedEntity.class);
		IEntityMetaData auditedEntityRefMetaData = entityMetaDataProvider.getMetaData(IAuditedEntityRef.class);
		IEntityMetaData auditedEntityPrimitiveMetaData = entityMetaDataProvider.getMetaData(IAuditedEntityPrimitiveProperty.class);
		IEntityMetaData auditedEntityRelationMetaData = entityMetaDataProvider.getMetaData(IAuditedEntityRelationProperty.class);
		IEntityMetaData auditedEntityRelationItemMetaData = entityMetaDataProvider.getMetaData(IAuditedEntityRelationPropertyItem.class);

		IPrimitiveUpdateItem[] auditEntryPUIs = auditEntry.getFullPUIs();
		writeProperty(IAuditEntry.UserIdentifier, auditEntryPUIs, auditEntryMetaData, os);
		writeProperty(IAuditEntry.Timestamp, auditEntryPUIs, auditEntryMetaData, os);
		writeProperty(IAuditEntry.Protocol, auditEntryPUIs, auditEntryMetaData, os);
		for (CreateOrUpdateContainerBuild auditedService : sortAuditServices(auditEntry, auditEntryMetaData, auditedServiceMetaData))
		{
			IPrimitiveUpdateItem[] auditedServicePUIs = auditedService.getFullPUIs();
			writeProperty(IAuditedService.ServiceType, auditedServicePUIs, auditedServiceMetaData, os);
			writeProperty(IAuditedService.MethodName, auditedServicePUIs, auditedServiceMetaData, os);
			writeProperty(IAuditedService.SpentTime, auditedServicePUIs, auditedServiceMetaData, os);
		}
		for (CreateOrUpdateContainerBuild auditedEntity : sortAuditEntities(auditEntry, auditEntryMetaData, auditedEntityMetaData))
		{
			IPrimitiveUpdateItem[] auditedEntityPUIs = auditedEntity.getFullPUIs();
			CreateOrUpdateContainerBuild ref = (CreateOrUpdateContainerBuild) ((IDirectObjRef) auditedEntity.getFullRUIs()[auditedEntityMetaData
					.getIndexByRelationName(IAuditedEntity.Ref)].getAddedORIs()[0]).getDirect();
			IPrimitiveUpdateItem[] auditedEntityRefPUIs = ref.getFullPUIs();
			writeProperty(IAuditedEntityRef.EntityType, auditedEntityRefPUIs, auditedEntityRefMetaData, os);
			writeProperty(IAuditedEntityRef.EntityId, auditedEntityRefPUIs, auditedEntityRefMetaData, os);
			writeProperty(IAuditedEntityRef.EntityVersion, auditedEntityRefPUIs, auditedEntityRefMetaData, os);
			writeProperty(IAuditedEntity.ChangeType, auditedEntityPUIs, auditedEntityMetaData, os);

			for (CreateOrUpdateContainerBuild primitiveProperty : sortAuditedEntityPrimitives(auditedEntity, auditedEntityMetaData,
					auditedEntityPrimitiveMetaData))
			{
				IPrimitiveUpdateItem[] primitivePropertyPUIs = primitiveProperty.getFullPUIs();
				writeProperty(IAuditedEntityPrimitiveProperty.Name, primitivePropertyPUIs, auditedEntityPrimitiveMetaData, os);
				writeProperty(IAuditedEntityPrimitiveProperty.NewValue, primitivePropertyPUIs, auditedEntityPrimitiveMetaData, os);
			}
			for (CreateOrUpdateContainerBuild relationProperty : sortAuditedEntityRelations(auditedEntity, auditedEntityMetaData, auditedEntityRelationMetaData))
			{
				IPrimitiveUpdateItem[] relationPUIs = relationProperty.getFullPUIs();
				writeProperty(IAuditedEntityRelationProperty.Name, relationPUIs, auditedEntityRelationMetaData, os);

				for (CreateOrUpdateContainerBuild item : sortAuditedEntityRelationItems(relationProperty, auditedEntityRelationMetaData,
						auditedEntityRelationItemMetaData))
				{
					IPrimitiveUpdateItem[] itemPUIs = item.getFullPUIs();
					CreateOrUpdateContainerBuild itemRef = (CreateOrUpdateContainerBuild) ((IDirectObjRef) item.getFullRUIs()[auditedEntityRelationItemMetaData
							.getIndexByRelationName(IAuditedEntityRelationPropertyItem.Ref)].getAddedORIs()[0]).getDirect();
					IPrimitiveUpdateItem[] itemRefPUIs = itemRef.getFullPUIs();
					writeProperty(IAuditedEntityRef.EntityType, itemRefPUIs, auditedEntityRefMetaData, os);
					writeProperty(IAuditedEntityRef.EntityId, itemRefPUIs, auditedEntityRefMetaData, os);
					writeProperty(IAuditedEntityRef.EntityVersion, itemRefPUIs, auditedEntityRefMetaData, os);
					writeProperty(IAuditedEntityRelationPropertyItem.ChangeType, itemPUIs, auditedEntityRelationItemMetaData, os);
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

	protected List<CreateOrUpdateContainerBuild> sortAuditEntities(CreateOrUpdateContainerBuild auditEntry, IEntityMetaData auditEntryMetaData,
			IEntityMetaData auditedEntityMetaData)
	{
		IRelationUpdateItem entitiesRUI = auditEntry.getFullRUIs()[auditEntryMetaData.getIndexByRelationName(IAuditEntry.Entities)];
		if (entitiesRUI == null)
		{
			return EmptyList.<CreateOrUpdateContainerBuild> getInstance();
		}
		IObjRef[] addedORIs = entitiesRUI.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> services = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			services.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		final int orderIndex = auditedEntityMetaData.getIndexByPrimitiveName(IAuditedService.Order);
		Collections.sort(services, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex]).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex]).intValue();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return services;
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

	protected List<CreateOrUpdateContainerBuild> sortAuditServices(CreateOrUpdateContainerBuild auditEntry, IEntityMetaData auditEntryMetaData,
			IEntityMetaData auditedServiceMetaData)
	{
		IRelationUpdateItem servicesRUI = auditEntry.getFullRUIs()[auditEntryMetaData.getIndexByRelationName(IAuditEntry.Services)];
		if (servicesRUI == null)
		{
			return EmptyList.<CreateOrUpdateContainerBuild> getInstance();
		}
		IObjRef[] addedORIs = servicesRUI.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> services = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			services.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		final int orderIndex = auditedServiceMetaData.getIndexByPrimitiveName(IAuditedService.Order);
		Collections.sort(services, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex]).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex]).intValue();
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

	protected List<CreateOrUpdateContainerBuild> sortAuditedEntityPrimitives(CreateOrUpdateContainerBuild auditedEntity, IEntityMetaData auditedEntityMetaData,
			IEntityMetaData auditedEntityPrimitiveMetaData)
	{
		IRelationUpdateItem primitivesRUI = auditedEntity.getFullRUIs()[auditedEntityMetaData.getIndexByRelationName(IAuditedEntity.Primitives)];
		if (primitivesRUI == null)
		{
			return EmptyList.<CreateOrUpdateContainerBuild> getInstance();
		}
		IObjRef[] addedORIs = primitivesRUI.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> primitives = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			primitives.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		final int orderIndex = auditedEntityPrimitiveMetaData.getIndexByPrimitiveName(IAuditedEntityPrimitiveProperty.Order);
		Collections.sort(primitives, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex]).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex]).intValue();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return primitives;
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

	protected List<CreateOrUpdateContainerBuild> sortAuditedEntityRelations(CreateOrUpdateContainerBuild auditedEntity, IEntityMetaData auditedEntityMetaData,
			IEntityMetaData auditedEntityRelationMetaData)
	{
		IRelationUpdateItem relationsRUI = auditedEntity.getFullRUIs()[auditedEntityMetaData.getIndexByRelationName(IAuditedEntity.Relations)];
		if (relationsRUI == null)
		{
			return EmptyList.<CreateOrUpdateContainerBuild> getInstance();
		}
		IObjRef[] addedORIs = relationsRUI.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> relations = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			relations.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		final int orderIndex = auditedEntityRelationMetaData.getIndexByPrimitiveName(IAuditedEntityRelationProperty.Order);
		Collections.sort(relations, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex]).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex]).intValue();
				if (order1 == order2)
				{
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		return relations;
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

	protected List<CreateOrUpdateContainerBuild> sortAuditedEntityRelationItems(CreateOrUpdateContainerBuild relation, IEntityMetaData relationMetaData,
			IEntityMetaData relationItemMetaData)
	{
		IRelationUpdateItem itemsRUI = relation.getFullRUIs()[relationMetaData.getIndexByRelationName(IAuditedEntityRelationProperty.Items)];
		if (itemsRUI == null)
		{
			return EmptyList.<CreateOrUpdateContainerBuild> getInstance();
		}
		IObjRef[] addedORIs = itemsRUI.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> items = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			items.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		final int orderIndex = relationItemMetaData.getIndexByPrimitiveName(IAuditedEntityRelationPropertyItem.Order);
		Collections.sort(items, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex]).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex]).intValue();
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
