package com.koch.ambeth.audit.server.config;

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

import com.koch.ambeth.audit.server.AuditInfo;
import com.koch.ambeth.audit.server.AuditReasonRequired;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.audit.model.AuditedArg;
import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class AuditConfigurationConstants {
	/**
	 * Defines if audit should be enabled or not. If true all beans required to record audit entries
	 * are registered, otherwise only mandatory configuration provider. Valid values are "true" and
	 * "false", default is "false".
	 */
	public static final String AuditActive = "audit.active";

	/**
	 * Defines the default behavior for entities for auditing. If set to "true" for all entities which
	 * are not annotated with {@link Audited} audit is automatically enabled, if set to false they are
	 * ignored during auditing. Valid values are "true" and "false", default is "false".
	 */
	public static final String AuditedEntityDefaultModeActive = "audit.entity.defaultmode.active";

	/**
	 * Defines whether by default an audit reason is required or not. If no
	 * {@link AuditReasonRequired} annotation is added to the entity / member this property defines
	 * whether a reason is necessary. Valid values are "true" and "false", default is "false".
	 */
	public static final String AuditReasonRequiredDefault = "audit.reason.required.default";

	/**
	 * Defines the default behavior for member of entities for auditing. If set to "true" all members
	 * of audited entities which are not annotated with {@link Audited} audit is automatically
	 * enabled, if set to false they are ignored during auditing. Valid values are "true" and "false",
	 * default is "false".
	 */
	public static final String AuditedEntityPropertyDefaultModeActive =
			"audit.entity.property.defaultmode.active";

	/**
	 * Defines the default behavior for auditing service calls if the service is not annotated with
	 * {@link AuditInfo}. If set to "true" for all service invocations audit entries are generated.
	 * Valid values are "true" and "false", default value is "true".
	 */
	public static final String AuditedServiceDefaultModeActive = "audit.service.defaultmode.active";

	/**
	 * Defines the default behavior for handling the arguments in audit entries of service calls if
	 * the argument is not annotated with {@link AuditedArg}. If set to "true" the arguments of
	 * service calls are added to the audit entry by default, otherwise the text "n/a" is set. Valid
	 * values are "true" and "false", default is "false".
	 *
	 */
	public static final String AuditedServiceArgDefaultModeActive =
			"audit.servicearg.defaultmode.active";

	/**
	 * The hash algorithm used to sign the audit entry. Valid values are all provided hash-algorithms,
	 * default value is "SHA-256".
	 */
	public static final String AuditedInformationHashAlgorithm = "audit.hashalgorithm.name";

	@ConfigurationConstantDescription("TODO")
	public static final String VerifyEntitiesOnLoad = "audit.verify.onload";

	/**
	 * Defines the maximum time (in ms) to keep the asynchronous transaction open during high
	 * verification load. If synchronous verification is activated this property has no effect.
	 * Default value is 30000.
	 */
	public static final String VerifyEntitiesMaxTransactionTime = "audit.verify.maxtransaction.time";

	@ConfigurationConstantDescription("TODO")
	public static final String VerifierCrontab = "audit.verify.crontab";

	/**
	 * The version of the audit protocol to use. Valid values are all positive integers, default is
	 * "1".
	 */
	public static final String ProtocolVersion = "audit.protocol.version";

	private AuditConfigurationConstants() {
		// intended blank
	}
}
