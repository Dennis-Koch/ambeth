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

import java.security.Signature;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.IPrivateKeyProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class AuditEntryToSignature implements IAuditEntryToSignature, IAuditEntryWriterExtendable {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrivateKeyProvider privateKeyProvider;

	@Property(name = AuditConfigurationConstants.ProtocolVersion, defaultValue = "1")
	protected int protocol;

	@Property(name = AuditConfigurationConstants.AuditedInformationHashAlgorithm, defaultValue = "SHA-256")
	protected String hashAlgorithm;

	protected final MapExtendableContainer<Integer, IAuditEntryWriter> auditEntryWriters = new MapExtendableContainer<>(
			"auditEntryWriter", "auditEntryProtocol");

	@Override
	public void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword,
			ISignature signature) {
		java.security.Signature signatureHandle = privateKeyProvider.getSigningHandle(signature,
				clearTextPassword);
		if (signatureHandle == null) {
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(null);
			return;
		}
		try {
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(hashAlgorithm);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(protocol);

			if (signature != null) {
				auditEntry.ensureRelation(IAuditEntry.SignatureOfUser)
						.addObjRef(objRefHelper.entityToObjRef(signature));
			}
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null) {
				throw new IllegalArgumentException(
						"No instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
								+ protocol + "' of " + auditEntry);
			}
			auditEntryWriter.writeAuditEntry(auditEntry, hashAlgorithm, signatureHandle);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditEntry auditEntry, Signature signature) {
		try {
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null) {
				throw new IllegalArgumentException(
						"No instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
								+ protocol + "' of " + auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0) {
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			for (IAuditedEntity auditedEntity : auditEntry.getEntities()) {
				byte[] auditedEntityDigest = auditEntryWriter.writeAuditedEntity(auditedEntity,
						hashAlgorithm);
				signature.update(auditedEntityDigest);
				if (!signature.verify(Base64.decode(auditedEntity.getSignature()))) {
					return null;
				}
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the
			// audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much
			// stronger than e.g. MD5 as well...

			return auditEntryWriter.writeAuditEntry(auditEntry, hashAlgorithm);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditedEntity auditedEntity) {
		try {
			IAuditEntry auditEntry = auditedEntity.getEntry();
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null) {
				throw new IllegalArgumentException(
						"No instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
								+ protocol + "' of " + auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0) {
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the
			// audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much
			// stronger than e.g. MD5 as well...

			return auditEntryWriter.writeAuditedEntity(auditedEntity, hashAlgorithm);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion) {
		auditEntryWriters.register(auditEntryWriter, Integer.valueOf(protocolVersion));
	}

	@Override
	public void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion) {
		auditEntryWriters.unregister(auditEntryWriter, Integer.valueOf(protocolVersion));
	}
}
