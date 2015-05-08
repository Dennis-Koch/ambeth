package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class AuditConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String AuditActive = "audit.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedEntityDefaultModeActive = "audit.entity.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditReasonRequiredDefault = "audit.reason.required.default";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedEntityPropertyDefaultModeActive = "audit.entity.property.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedServiceDefaultModeActive = "audit.service.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedServiceArgDefaultModeActive = "audit.servicearg.defaultmode.active";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditedInformationHashAlgorithm = "audit.hashalgorithm.name";

	@ConfigurationConstantDescription("TODO")
	public static final String AuditVerifyExpectSignature = "audit.verify.expectsignature";

	@ConfigurationConstantDescription("TODO")
	public static final String VerifyEntitiesOnLoadActive = "audit.verify.onload.active";

	@ConfigurationConstantDescription("TODO")
	public static final String VerifierCrontab = "audit.verify.crontab";

	@ConfigurationConstantDescription("TODO")
	public static final String ProtocolVersion = "audit.protocol.version";

	private AuditConfigurationConstants()
	{
		// intended blank
	}
}
