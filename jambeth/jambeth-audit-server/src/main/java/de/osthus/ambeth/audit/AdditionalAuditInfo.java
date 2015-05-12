package de.osthus.ambeth.audit;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.security.model.IUser;

public class AdditionalAuditInfo
{
	public final ArrayList<String> auditReasonContainer = new ArrayList<String>();

	public final ArrayList<String> auditContextContainer = new ArrayList<String>();

	public Boolean ownAuditMergeActive;

	public char[] clearTextPassword;

	public boolean doClearPassword;

	public IUser authorizedUser;
}