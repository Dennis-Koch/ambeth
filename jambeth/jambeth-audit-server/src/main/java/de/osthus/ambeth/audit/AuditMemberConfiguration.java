package de.osthus.ambeth.audit;

public enum AuditMemberConfiguration implements IAuditMemberConfiguration
{
	ACTIVE(true), INACTIVE(false);

	private final boolean auditActive;

	private AuditMemberConfiguration(boolean auditActive)
	{
		this.auditActive = auditActive;
	}

	@Override
	public boolean isAuditActive()
	{
		return auditActive;
	}
}
