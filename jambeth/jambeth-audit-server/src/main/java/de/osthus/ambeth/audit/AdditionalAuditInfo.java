package de.osthus.ambeth.audit;

import de.osthus.ambeth.collections.ArrayList;

public class AdditionalAuditInfo
{
	public final ArrayList<String> auditReasonContainer = new ArrayList<String>();

	public final ArrayList<String> auditContextContainer = new ArrayList<String>();

	public Boolean ownAuditMergeActive;

	public char[] clearTextPassword;
}