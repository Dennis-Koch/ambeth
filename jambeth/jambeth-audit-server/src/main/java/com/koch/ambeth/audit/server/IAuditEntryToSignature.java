package com.koch.ambeth.audit.server;

import java.util.function.BiFunction;

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
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.security.model.ISignature;

/**
 * Encapsulates the complete signing and verification process using the audit information model. It
 * also internally selects the currently relevant prococol (see {@link IAuditEntryWriter} during
 * signing.
 */
public interface IAuditEntryToSignature {
	/**
	 * Signs a generated audit trail. Is internally called by the {@link AuditController} based on a
	 * pre-commit hook. The clear-text password of the user matching to the given signature handle is
	 * required to decrypt the private key of the signature necessary for the signing operation and
	 * therefore impersonate the user during this phase.
	 *
	 * @param auditEntry
	 *          The audit entry to sign
	 * @param clearTextPassword
	 *          The clear-text password of the user related by the given signature
	 * @param signature
	 *          The current live signature entity of the user used for sign operations
	 */
	void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword,
			ISignature signature);

	/**
	 * Creates a hash value of a given audit entry based on its configuration (specifically: stored
	 * hash algorithm and serialization protocol version). Is internally called by the
	 * {@link AuditEntryVerifier} during verification tasks.
	 *
	 * @param auditEntry
	 *          The audit entry to verify
	 * @param auditedEntityVerifyFunction
	 *          The verification function used to verify the digest of each audited entity during the
	 *          audit entry digest creation. Must return true if a verification is successful and
	 *          false if not
	 * @return The digest of the audit entry. May be null if the audit model was in an invalid state
	 *         or any internal audited entity verification - represented by the given verifyFunction -
	 *         failed.
	 */
	byte[] createVerifyDigest(IAuditEntry auditEntry,
			BiFunction<IAuditedEntity, byte[], Boolean> auditedEntityVerifyFunction);

	/**
	 * Creates a hash value of a given audited entity based on its configuration (specifically: stored
	 * hash algorithm & serialization protocol version of its parent audit entry). Is internally
	 * called by the {@link AuditEntryVerifier} during verification tasks.
	 *
	 * @param auditedEntity
	 * @return The digest of the audited entity. May be null if the audit model was in an invalid
	 *         state
	 */
	byte[] createVerifyDigest(IAuditedEntity auditedEntity);
}
