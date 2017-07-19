package com.koch.ambeth.audit;

/*-
 * #%L
 * jambeth-audit
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

import java.util.List;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;

public interface IAuditEntryVerifier {
	/**
	 * Cryptographically verifies all <code>IAuditEntry</code> instances related to a given set of
	 * entities. The verification fails if the <code>Signature</code> property does not correlate with
	 * the user <code>ISignature</code> handle associated with any <code>IAuditEntry</code>.
	 * Essentially the complete history of each entity is verified starting with its first creation.
	 * If this verification fails an entry has been tampered either accidentally or intentionally
	 * after its creation.<br>
	 * <br>
	 *
	 * - The signature of the entry may have been changed after the initial creation<br>
	 * - A property of the recent (HEAD) version of a given entity may have been changed - At least
	 * one property of the AuditEntry or its relevant relationships may have been changed (e.g.
	 * signature, associated user handle, <code>ISignature</code> of the associated user handle,
	 * related <code>IAuditedEntity</code> <br>
	 * Very important: In the common case (success scenario) this methods returns an array of TRUE
	 * flags where the array size and indices correlate to the given list of <code>IAuditEntry</code>
	 * instances.
	 *
	 * @return Result of the verification. FALSE if at least one verification failure occurred on any
	 *         resolved <code>IAuditEntry</code>.
	 */
	boolean verifyEntities(List<?> entities);

	/**
	 * Cryptographically verifies a given set of <code>IAuditEntry</code> instances. The verification
	 * fails if the <code>Signature</code> property does not correlate with the user
	 * <code>ISignature</code> handle associated with this entry. If this verification fails the entry
	 * has been tampered either accidentally or intentionally after its creation.<br>
	 * <br>
	 *
	 * - The signature of the entry may have been changed after the initial creation<br>
	 * - At least one property of the AuditEntry or its relevant relationships may have been changed
	 * (e.g. signature, associated user handle, <code>ISignature</code> of the associated user handle,
	 * related <code>IAuditedEntity</code> or <code>IAuditedService</code> objects, timestamps, ...)
	 * <br>
	 * <br>
	 * Very important: In the common case (success scenario) this methods returns an array of TRUE
	 * flags where the array size and indices correlate to the given list of <code>IAuditEntry</code>
	 * instances.
	 *
	 * @return Array of verification result flags. Each FALSE indicates a verification error. TRUE
	 *         values imply successful verifications.
	 */
	boolean[] verifyAuditEntries(List<? extends IAuditEntry> auditEntries);

	/**
	 * Cryptographically verifies a given set of <code>IAuditedEntity</code> instances. The
	 * verification fails if the <code>Signature</code> property of the related
	 * <code>IAuditEntry</code> does not correlate with the user <code>ISignature</code> handle
	 * associated with this entry. If this verification fails the entry has been tampered either
	 * accidentally or intentionally after its creation. This methods exists for performance reasons
	 * compared to the verification of full AuditEntry instances and it does NOT verify the related
	 * full AuditEntries.<br>
	 * <br>
	 *
	 * - The signature of the entry may have been changed after the initial creation<br>
	 * - At least one property of the AuditedEntity or its relevant relationships may have been
	 * changed (e.g. AuditEntry, signature, associated user handle, <code>ISignature</code> of the
	 * associated user handle, ...<br>
	 * <br>
	 * Very important: In the common case (success scenario) this methods returns an array of TRUE
	 * flags where the array size and indices correlate to the given list of <code>IAuditEntry</code>
	 * instances.
	 *
	 * @return Array of verification result flags. Each FALSE indicates a verification error. TRUE
	 *         values imply successful verifications.
	 */
	boolean[] verifyAuditedEntities(List<? extends IAuditedEntity> auditedEntities);
}
