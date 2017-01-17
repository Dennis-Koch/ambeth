package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Collection;
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
import de.osthus.ambeth.audit.util.NullOutputStream;
import de.osthus.ambeth.codec.Base64;
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
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.util.IConversionHelper;

public class AuditEntryWriterV1 implements IAuditEntryWriter
{
	private static final String[] auditedPropertiesOfEntry = { IAuditEntry.Context,//
			IAuditEntry.HashAlgorithm,//
			IAuditEntry.Protocol,//
			IAuditEntry.Reason,//
			IAuditEntry.Timestamp,//
			IAuditEntry.UserIdentifier };

	private static final String[] auditedPropertiesOfService = { IAuditedService.Order,//
			IAuditedService.MethodName,//
			IAuditedService.ServiceType,//
			IAuditedService.SpentTime,//
			IAuditedService.Arguments };

	private static final String[] auditedPropertiesOfEntity = { IAuditedEntity.Order,//
			IAuditedEntity.ChangeType };

	private static final String[] auditedPropertiesOfRef = { IAuditedEntityRef.EntityType,//
			IAuditedEntityRef.EntityId,//
			IAuditedEntityRef.EntityVersion };

	private static final String[] auditedPropertiesOfPrimitive = { IAuditedEntityPrimitiveProperty.Order,//
			IAuditedEntityPrimitiveProperty.Name,//
			IAuditedEntityPrimitiveProperty.NewValue };

	private static final String[] auditedPropertiesOfRelation = { IAuditedEntityRelationProperty.Order,//
			IAuditedEntityRelationProperty.Name };

	private static final String[] auditedPropertiesOfRelationItem = { IAuditedEntityRelationPropertyItem.Order,//
			IAuditedEntityRelationPropertyItem.ChangeType };

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected String[] getAuditedPropertiesOfEntry()
	{
		return auditedPropertiesOfEntry;
	}

	protected String[] getAuditedPropertiesOfService()
	{
		return auditedPropertiesOfService;
	}

	protected String[] getAuditedPropertiesOfEntity()
	{
		return auditedPropertiesOfEntity;
	}

	protected String[] getAuditedPropertiesOfRef()
	{
		return auditedPropertiesOfRef;
	}

	protected String[] getAuditedPropertiesOfPrimitive()
	{
		return auditedPropertiesOfPrimitive;
	}

	protected String[] getAuditedPropertiesOfRelation()
	{
		return auditedPropertiesOfRelation;
	}

	protected String[] getAuditedPropertiesOfRelationItem()
	{
		return auditedPropertiesOfRelationItem;
	}

	protected void writeProperties(String[] props, IEntityMetaDataHolder obj, DataOutputStream os)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaData metaData = obj.get__EntityMetaData();
		if (obj instanceof IDataObject)
		{
			for (String prop : props)
			{
				Member member = metaData.getMemberByName(prop);
				Object value = member.getValue(obj);
				String sValue = conversionHelper.convertValueToType(String.class, value);
				writeProperty(prop, sValue, os);
			}
			return;
		}
		IPrimitiveUpdateItem[] fullPUIs = ((CreateOrUpdateContainerBuild) obj).getFullPUIs();
		for (String prop : props)
		{
			Object value = fullPUIs[metaData.getIndexByPrimitiveName(prop)].getNewValue();
			String sValue = conversionHelper.convertValueToType(String.class, value);
			writeProperty(prop, sValue, os);
		}
	}

	protected Object getPrimitiveValue(String memberName, Object obj)
	{
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject)
		{
			Member member = metaData.getMemberByName(memberName);
			return member.getValue(obj);
		}
		PrimitiveUpdateItem pui = ((CreateOrUpdateContainerBuild) obj).findPrimitive(memberName);
		if (pui == null)
		{
			return null;
		}
		return pui.getNewValue();
	}

	protected IEntityMetaDataHolder getRelationValue(String memberName, Object obj)
	{
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject)
		{
			Member member = metaData.getMemberByName(memberName);
			return (IEntityMetaDataHolder) member.getValue(obj);
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null)
		{
			return null;
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		return (CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORIs[0]).getDirect();
	}

	@Override
	public byte[] writeAuditEntry(IAuditEntry auditEntry, String hashAlgorithm) throws Throwable
	{
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		writeAuditEntryIntern((IEntityMetaDataHolder) auditEntry, dos);
		return md.digest();
	}

	@Override
	public byte[] writeAuditedEntity(IAuditedEntity auditedEntity, String hashAlgorithm) throws Throwable
	{
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		writeAuditedEntityIntern((IEntityMetaDataHolder) auditedEntity, dos);
		return md.digest();
	}

	@Override
	public void writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, String hashAlgorithm, Signature signature) throws Throwable
	{
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		IRelationUpdateItem rui = auditEntry.findRelation(IAuditEntry.Entities);
		if (rui != null)
		{
			for (IObjRef addedORI : rui.getAddedORIs())
			{
				CreateOrUpdateContainerBuild auditedEntity = (CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect();
				writeAuditedEntityIntern(auditedEntity, dos);
				signature.update(md.digest());
				byte[] sign = signature.sign();
				auditedEntity.ensurePrimitive(IAuditedEntity.Signature).setNewValue(Base64.encodeBytes(sign).toCharArray());
			}
		}
		writeAuditEntryIntern(auditEntry, dos);

		signature.update(md.digest());
		byte[] sign = signature.sign();

		auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(Base64.encodeBytes(sign).toCharArray());
	}

	protected void writeAuditEntryIntern(IEntityMetaDataHolder auditEntry, DataOutputStream os) throws Throwable
	{
		writeProperties(getAuditedPropertiesOfEntry(), auditEntry, os);
		for (IEntityMetaDataHolder auditedService : sortOrderedMember(IAuditEntry.Services, IAuditedService.Order, auditEntry))
		{
			writeProperties(getAuditedPropertiesOfService(), auditedService, os);
		}
		List<? extends IEntityMetaDataHolder> auditedEntities = sortOrderedMember(IAuditEntry.Entities, IAuditedEntity.Order, auditEntry);
		os.writeInt(auditedEntities.size());
		for (IEntityMetaDataHolder auditedEntity : auditedEntities)
		{
			char[] signatureOfAuditedEntity = (char[]) getPrimitiveValue(IAuditedEntity.Signature, auditedEntity);
			if (signatureOfAuditedEntity == null)
			{
				throw new IllegalStateException("Signature of " + IAuditedEntity.class.getName() + " is null");
			}
			for (char item : signatureOfAuditedEntity)
			{
				os.writeChar(item);
			}
		}
	}

	protected void writeAuditedEntityIntern(IEntityMetaDataHolder auditedEntity, DataOutputStream os)
	{
		IEntityMetaDataHolder ref = getRelationValue(IAuditedEntity.Ref, auditedEntity);
		writeProperties(getAuditedPropertiesOfRef(), ref, os);
		writeProperties(getAuditedPropertiesOfEntity(), auditedEntity, os);

		for (IEntityMetaDataHolder property : sortOrderedMember(IAuditedEntity.Primitives, IAuditedEntityPrimitiveProperty.Order, auditedEntity))
		{
			writeProperties(getAuditedPropertiesOfPrimitive(), property, os);
		}
		for (IEntityMetaDataHolder property : sortOrderedMember(IAuditedEntity.Relations, IAuditedEntityRelationProperty.Order, auditedEntity))
		{
			writeProperties(getAuditedPropertiesOfRelation(), property, os);

			for (IEntityMetaDataHolder item : sortOrderedMember(IAuditedEntityRelationProperty.Items, IAuditedEntityRelationPropertyItem.Order, property))
			{
				IEntityMetaDataHolder itemRef = getRelationValue(IAuditedEntityRelationPropertyItem.Ref, auditedEntity);

				writeProperties(getAuditedPropertiesOfRef(), itemRef, os);
				writeProperties(getAuditedPropertiesOfRelationItem(), item, os);
			}
		}
	}

	protected void writeProperty(String name, String value, DataOutputStream os)
	{
		try
		{
			for (int a = 0, size = name.length(); a < size; a++)
			{
				os.writeShort(name.charAt(a));
			}
			if (value == null)
			{
				os.writeBoolean(false);
			}
			else
			{
				os.writeBoolean(true);
				for (int a = 0, size = value.length(); a < size; a++)
				{
					os.writeShort(value.charAt(a));
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected List<? extends IEntityMetaDataHolder> sortOrderedMember(String memberName, String orderName, Object obj)
	{
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		Member member = metaData.getMemberByName(memberName);

		if (obj instanceof IDataObject)
		{
			Collection<? extends IEntityMetaDataHolder> coll = (Collection<? extends IEntityMetaDataHolder>) member.getValue(obj);
			if (coll.size() == 0)
			{
				return EmptyList.<IEntityMetaDataHolder> getInstance();
			}
			IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
			final Member orderMember = itemMetaData.getMemberByName(orderName);

			ArrayList<IEntityMetaDataHolder> items = new ArrayList<IEntityMetaDataHolder>(coll);

			Collections.sort(items, new Comparator<IEntityMetaDataHolder>()
			{
				@Override
				public int compare(IEntityMetaDataHolder o1, IEntityMetaDataHolder o2)
				{
					int order1 = orderMember.getIntValue(o1);
					int order2 = orderMember.getIntValue(o2);
					if (order1 == order2)
					{
						return 0;
					}
					return order1 < order2 ? -1 : 1;
				}
			});
			return items;
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null)
		{
			return EmptyList.<IEntityMetaDataHolder> getInstance();
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> items = new ArrayList<CreateOrUpdateContainerBuild>(addedORIs.length);
		for (IObjRef addedORI : addedORIs)
		{
			items.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		final int orderIndex = itemMetaData.getIndexByPrimitiveName(orderName);

		Collections.sort(items, new Comparator<CreateOrUpdateContainerBuild>()
		{
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2)
			{
				int order1 = ((Number) o1.getFullPUIs()[orderIndex].getNewValue()).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex].getNewValue()).intValue();
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
