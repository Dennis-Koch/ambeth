package com.koch.ambeth.audit.server;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

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

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;

/**
 * Used to serialize in-memory a given audit entry or audited entity in order to generate a hash
 * value based on a hash algorithm which therefore completely encapsulates the "serialization
 * protocol". This allows to use internally the same implementation for generating the prequisite
 * byte sequence of the sign process as well as the verify process.<br>
 * <br>
 * It is also possible to implement any custom IAuditEntryWriter and to register it via
 * {@link IAuditEntryWriterExtendable} - associated it with a custom protocol version. By
 * configuring the global context then with a defined
 * {@link AuditConfigurationConstants#ProtocolVersion} you can activate your custom protocol
 * implementation for all upcoming sign processes. Note that all already existing signatures remain
 * valid and unchanged because their verification process will refer to the "old" protocol used
 * during each of their signing process in the past.
 */
public interface IAuditEntryWriter {
	/**
	 * Called during the verification process by {@link IAuditEntryToSignature} in order to get the
	 * hashed value of a given audit entry
	 *
	 * @param auditEntry
	 *          The given audit entry to serialize and to generate the hash from
	 * @param md
	 *          Message digest to use when preparing the serialized audit entry for verification. Must
	 *          not be null
	 * @return the hashed value of the given audit entry. May be null if any invalid state of the
	 *         audit model is discovered during the serialization
	 */
	byte[] writeAuditEntry(IAuditEntry auditEntry, MessageDigest md);

	/**
	 * Called during the verification process by {@link IAuditEntryToSignature} in order to get the
	 * hashed value of a given audited entity
	 *
	 * @param auditedEntity
	 *          The given audited entity to serialize and to generate the hash from
	 * @param md
	 *          Message digest to use when preparing the serialized audited entity for verification.
	 *          Must not be null
	 * @return the hashed value of the given audited entity. May be null if any invalid state of the
	 *         audit model is discovered during the serialization
	 */
	byte[] writeAuditedEntity(IAuditedEntity auditedEntity, MessageDigest md);

	/**
	 * Called during the sign process by {@link IAuditEntryToSignature}
	 *
	 * @param auditEntry
	 *          The audit entry to serialize, to hash its byte stream representation and to generate
	 *          the signed value of it. Must not be null
	 * @param md
	 *          Message digest to use when preparing the serialized audit model for signing. Signing
	 *          only the hash value of a data of arbitrary length greatly reduces the CPU efforts of
	 *          the sign operation. Must not be null
	 * @param signFunction
	 *          Function to be called in order to individually sign the nested audited entity
	 *          information of the given audit entry. Must not be null and must not return null when
	 *          invoked
	 * @return the hashed value of the given audit entry. May be null if any invalid state of the
	 *         audit model is discovered during the serialization
	 * @throws SignatureException
	 *           Thrown if any issue arises during the interaction with the given signature
	 */
	byte[] writeAuditEntry(CreateOrUpdateContainerBuild auditEntry, MessageDigest md,
			Function<byte[], byte[]> signFunction);
}
