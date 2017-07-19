package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.io.DataOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.model.IAuditedService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.audit.util.NullOutputStream;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;

public class AuditEntryWriterV1 implements IAuditEntryWriter {
	private static final String[] auditedPropertiesOfEntry = { IAuditEntry.Context, //
			IAuditEntry.HashAlgorithm, //
			IAuditEntry.Protocol, //
			IAuditEntry.Reason, //
			IAuditEntry.Timestamp, //
			IAuditEntry.UserIdentifier };

	private static final String[] auditedPropertiesOfService = { IAuditedService.Order, //
			IAuditedService.MethodName, //
			IAuditedService.ServiceType, //
			IAuditedService.SpentTime, //
			IAuditedService.Arguments };

	private static final String[] auditedPropertiesOfEntity = { IAuditedEntity.Order, //
			IAuditedEntity.ChangeType };

	private static final String[] auditedPropertiesOfRef = { IAuditedEntityRef.EntityType, //
			IAuditedEntityRef.EntityId, //
			IAuditedEntityRef.EntityVersion };

	private static final String[] auditedPropertiesOfPrimitive = {
			IAuditedEntityPrimitiveProperty.Order, //
			IAuditedEntityPrimitiveProperty.Name, //
			IAuditedEntityPrimitiveProperty.NewValue };

	private static final String[] auditedPropertiesOfRelation = {
			IAuditedEntityRelationProperty.Order, //
			IAuditedEntityRelationProperty.Name };

	private static final String[] auditedPropertiesOfRelationItem = {
			IAuditedEntityRelationPropertyItem.Order, //
			IAuditedEntityRelationPropertyItem.ChangeType };

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected String[] getAuditedPropertiesOfEntry() {
		return auditedPropertiesOfEntry;
	}

	protected String[] getAuditedPropertiesOfService() {
		return auditedPropertiesOfService;
	}

	protected String[] getAuditedPropertiesOfEntity() {
		return auditedPropertiesOfEntity;
	}

	protected String[] getAuditedPropertiesOfRef() {
		return auditedPropertiesOfRef;
	}

	protected String[] getAuditedPropertiesOfPrimitive() {
		return auditedPropertiesOfPrimitive;
	}

	protected String[] getAuditedPropertiesOfRelation() {
		return auditedPropertiesOfRelation;
	}

	protected String[] getAuditedPropertiesOfRelationItem() {
		return auditedPropertiesOfRelationItem;
	}

	protected void writeProperties(String[] props, IEntityMetaDataHolder obj, DataOutputStream os) {
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaData metaData = obj.get__EntityMetaData();
		if (obj instanceof IDataObject) {
			for (String prop : props) {
				Member member = metaData.getMemberByName(prop);
				Object value = member.getValue(obj);
				String sValue = conversionHelper.convertValueToType(String.class, value);
				writeProperty(prop, sValue, os);
			}
			return;
		}
		IPrimitiveUpdateItem[] fullPUIs = ((CreateOrUpdateContainerBuild) obj).getFullPUIs();
		for (String prop : props) {
			Object value = fullPUIs[metaData.getIndexByPrimitiveName(prop)].getNewValue();
			String sValue = conversionHelper.convertValueToType(String.class, value);
			writeProperty(prop, sValue, os);
		}
	}

	protected Object getPrimitiveValue(String memberName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject) {
			Member member = metaData.getMemberByName(memberName);
			return member.getValue(obj);
		}
		PrimitiveUpdateItem pui = ((CreateOrUpdateContainerBuild) obj).findPrimitive(memberName);
		if (pui == null) {
			return null;
		}
		return pui.getNewValue();
	}

	protected IEntityMetaDataHolder getRelationValue(String memberName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (obj instanceof IDataObject) {
			Member member = metaData.getMemberByName(memberName);
			return (IEntityMetaDataHolder) member.getValue(obj);
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null) {
			return null;
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		return (CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORIs[0]).getDirect();
	}

	@Override
	public byte[] writeAuditEntry(IAuditEntry auditEntry, String hashAlgorithm) throws Exception {
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		if (!writeAuditEntryIntern((IEntityMetaDataHolder) auditEntry, dos)) {
			return null;
		}
		return md.digest();
	}

	@Override
	public byte[] writeAuditedEntity(IAuditedEntity auditedEntity, String hashAlgorithm)
			throws Exception {
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		if (!writeAuditedEntityIntern((IEntityMetaDataHolder) auditedEntity, dos)) {
			return null;
		}
		return md.digest();
	}

	@Override
	public byte[] writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, String hashAlgorithm,
			Signature signature) throws Exception {
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		IRelationUpdateItem rui = auditEntry.findRelation(IAuditEntry.Entities);
		if (rui != null) {
			for (IObjRef addedORI : rui.getAddedORIs()) {
				CreateOrUpdateContainerBuild auditedEntity = (CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI)
						.getDirect();
				if (!writeAuditedEntityIntern(auditedEntity, dos)) {
					return null;
				}
				byte[] digest = md.digest();
				signature.update(digest);
				byte[] sign = signature.sign();
				auditedEntity.ensurePrimitive(IAuditedEntity.Signature)
						.setNewValue(Base64.encodeBytes(sign).toCharArray());
			}
		}
		if (!writeAuditEntryIntern(auditEntry, dos)) {
			return null;
		}

		byte[] digest = md.digest();
		signature.update(digest);
		return signature.sign();
	}

	protected boolean writeAuditEntryIntern(IEntityMetaDataHolder auditEntry, DataOutputStream os)
			throws Exception {
		writeProperties(getAuditedPropertiesOfEntry(), auditEntry, os);
		for (IEntityMetaDataHolder auditedService : sortOrderedMember(IAuditEntry.Services,
				IAuditedService.Order, auditEntry)) {
			writeProperties(getAuditedPropertiesOfService(), auditedService, os);
		}
		List<? extends IEntityMetaDataHolder> auditedEntities = sortOrderedMember(IAuditEntry.Entities,
				IAuditedEntity.Order, auditEntry);
		if (auditedEntities == null) {
			return false;
		}
		os.writeInt(auditedEntities.size());
		for (int a = 0, size = auditedEntities.size(); a < size; a++) {
			IEntityMetaDataHolder auditedEntity = auditedEntities.get(a);
			char[] signatureOfAuditedEntity = (char[]) getPrimitiveValue(IAuditedEntity.Signature,
					auditedEntity);
			if (signatureOfAuditedEntity == null) {
				throw new IllegalStateException(
						"Signature of " + IAuditedEntity.class.getName() + " is null");
			}
			for (char item : signatureOfAuditedEntity) {
				os.writeChar(item);
			}
		}
		return true;
	}

	protected boolean writeAuditedEntityIntern(IEntityMetaDataHolder auditedEntity,
			DataOutputStream os) {
		IEntityMetaDataHolder ref = getRelationValue(IAuditedEntity.Ref, auditedEntity);
		writeProperties(getAuditedPropertiesOfRef(), ref, os);
		writeProperties(getAuditedPropertiesOfEntity(), auditedEntity, os);
		{
			List<? extends IEntityMetaDataHolder> primitives = sortOrderedMember(
					IAuditedEntity.Primitives, IAuditedEntityPrimitiveProperty.Order, auditedEntity);
			if (primitives == null) {
				return false;
			}
			for (int a = 0, size = primitives.size(); a < size; a++) {
				IEntityMetaDataHolder property = primitives.get(a);
				writeProperties(getAuditedPropertiesOfPrimitive(), property, os);
			}
		}
		List<? extends IEntityMetaDataHolder> relations = sortOrderedMember(IAuditedEntity.Relations,
				IAuditedEntityRelationProperty.Order, auditedEntity);
		if (relations == null) {
			return false;
		}
		for (int a = 0, size = relations.size(); a < size; a++) {
			IEntityMetaDataHolder property = relations.get(a);
			writeProperties(getAuditedPropertiesOfRelation(), property, os);

			List<? extends IEntityMetaDataHolder> items = sortOrderedMember(
					IAuditedEntityRelationProperty.Items, IAuditedEntityRelationPropertyItem.Order, property);
			if (items == null) {
				return false;
			}
			for (int b = 0, sizeB = items.size(); b < sizeB; b++) {
				IEntityMetaDataHolder item = items.get(b);
				IEntityMetaDataHolder itemRef = getRelationValue(IAuditedEntityRelationPropertyItem.Ref,
						item);

				writeProperties(getAuditedPropertiesOfRef(), itemRef, os);
				writeProperties(getAuditedPropertiesOfRelationItem(), item, os);
			}
		}
		return true;
	}

	protected void writeProperty(String name, String value, DataOutputStream os) {
		try {
			for (int a = 0, size = name.length(); a < size; a++) {
				os.writeShort(name.charAt(a));
			}
			if (value == null) {
				os.writeBoolean(false);
			}
			else {
				os.writeBoolean(true);
				for (int a = 0, size = value.length(); a < size; a++) {
					os.writeShort(value.charAt(a));
				}
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected List<? extends IEntityMetaDataHolder> sortOrderedMember(String memberName,
			String orderName, Object obj) {
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		Member member = metaData.getMemberByName(memberName);

		if (obj instanceof IDataObject) {
			Object value = member.getValue(obj);
			ArrayList<IEntityMetaDataHolder> items;
			if (value instanceof Collection) {
				Collection<? extends IEntityMetaDataHolder> coll = (Collection<? extends IEntityMetaDataHolder>) member
						.getValue(obj);
				if (coll.isEmpty()) {
					return EmptyList.<IEntityMetaDataHolder>getInstance();
				}
				items = new ArrayList<>(coll);
			}
			else if (value != null) {
				items = new ArrayList<>(new IEntityMetaDataHolder[] { (IEntityMetaDataHolder) value });
			}
			else {
				return EmptyList.<IEntityMetaDataHolder>getInstance();
			}
			if (orderName == null) {
				return items;
			}
			IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
			final Member orderMember = itemMetaData.getMemberByName(orderName);

			Collections.sort(items, new Comparator<IEntityMetaDataHolder>() {
				@Override
				public int compare(IEntityMetaDataHolder o1, IEntityMetaDataHolder o2) {
					int order1 = orderMember.getIntValue(o1);
					int order2 = orderMember.getIntValue(o2);
					if (order1 == order2) {
						return 0;
					}
					return order1 < order2 ? -1 : 1;
				}
			});
			for (int a = items.size(); a-- > 0;) {
				IEntityMetaDataHolder item = items.get(a);
				int order = orderMember.getIntValue(item);
				if (order != a + 1) {
					return null;
				}
			}
			return items;
		}
		IRelationUpdateItem rui = ((CreateOrUpdateContainerBuild) obj).findRelation(memberName);
		if (rui == null) {
			return EmptyList.<IEntityMetaDataHolder>getInstance();
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		ArrayList<CreateOrUpdateContainerBuild> items = new ArrayList<>(addedORIs.length);
		for (IObjRef addedORI : addedORIs) {
			items.add((CreateOrUpdateContainerBuild) ((IDirectObjRef) addedORI).getDirect());
		}
		if (orderName == null) {
			return items;
		}
		IEntityMetaData itemMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		final int orderIndex = itemMetaData.getIndexByPrimitiveName(orderName);

		Collections.sort(items, new Comparator<CreateOrUpdateContainerBuild>() {
			@Override
			public int compare(CreateOrUpdateContainerBuild o1, CreateOrUpdateContainerBuild o2) {
				int order1 = ((Number) o1.getFullPUIs()[orderIndex].getNewValue()).intValue();
				int order2 = ((Number) o2.getFullPUIs()[orderIndex].getNewValue()).intValue();
				if (order1 == order2) {
					return 0;
				}
				return order1 < order2 ? -1 : 1;
			}
		});
		for (int a = items.size(); a-- > 0;) {
			CreateOrUpdateContainerBuild item = items.get(a);
			int order = ((Number) item.getFullPUIs()[orderIndex].getNewValue()).intValue();
			if (order != a + 1) {
				return null;
			}
		}
		return items;
	}
}
