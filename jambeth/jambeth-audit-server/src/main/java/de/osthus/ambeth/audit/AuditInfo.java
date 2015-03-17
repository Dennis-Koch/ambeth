package de.osthus.ambeth.audit;

public class AuditInfo
{
	protected Audited audited;
	protected AuditedArg[] auditedArgs;

	public AuditInfo()
	{
		this(null);
	}

	public AuditInfo(Audited audited)
	{
		this.audited = audited;
	}

	public void setAudited(Audited audited)
	{
		this.audited = audited;
	}

	public Audited getAudited()
	{
		return audited;
	}

	public void setAuditedArgs(AuditedArg[] auditedArgs)
	{
		this.auditedArgs = auditedArgs;
	}

	public AuditedArg[] getAuditedArgs()
	{
		return auditedArgs;
	}
}
