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
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.function.Function;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.model.IAuditedService;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.audit.util.NullOutputStream;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class AuditEntryWriterV1 extends AbstractAuditEntryWriter implements IAuditEntryWriter {
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
			IAuditedEntity.ChangeType, //
			IAuditedEntity.RefPreviousVersion };

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

	@Override
	public byte[] writeAuditEntry(IAuditEntry auditEntry, MessageDigest md) {
		md.reset();
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		if (!writeAuditEntryIntern((IEntityMetaDataHolder) auditEntry, dos)) {
			return null;
		}
		return md.digest();
	}

	@Override
	public byte[] writeAuditedEntity(IAuditedEntity auditedEntity, MessageDigest md) {
		md.reset();
		DigestOutputStream digestOS = new DigestOutputStream(NullOutputStream.INSTANCE, md);
		DataOutputStream dos = new DataOutputStream(digestOS);

		if (!writeAuditedEntityIntern((IEntityMetaDataHolder) auditedEntity, dos)) {
			return null;
		}
		return md.digest();
	}

	@Override
	public byte[] writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, MessageDigest md,
			Function<byte[], byte[]> signFunction) {
		md.reset();
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
				byte[] sign = signFunction.apply(digest);
				auditedEntity.ensurePrimitive(IAuditedEntity.Signature)
						.setNewValue(Base64.encodeBytes(sign).toCharArray());
			}
		}
		if (!writeAuditEntryIntern(auditEntry, dos)) {
			return null;
		}
		return md.digest();
	}

	protected boolean writeAuditEntryIntern(IEntityMetaDataHolder auditEntry, DataOutputStream os) {
		try {
			writeProperties(getAuditedPropertiesOfEntry(), auditEntry, os);
			List<? extends IEntityMetaDataHolder> auditedServices = sortOrderedMember(
					IAuditEntry.Services, IAuditedService.Order, auditEntry);
			if (auditedServices == null) {
				return false;
			}
			os.writeInt(auditedServices.size());
			for (int a = 0, size = auditedServices.size(); a < size; a++) {
				IEntityMetaDataHolder auditedService = auditedServices.get(a);
				writeProperties(getAuditedPropertiesOfService(), auditedService, os);
			}
			List<? extends IEntityMetaDataHolder> auditedEntities = sortOrderedMember(
					IAuditEntry.Entities, IAuditedEntity.Order, auditEntry);
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
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
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
}
