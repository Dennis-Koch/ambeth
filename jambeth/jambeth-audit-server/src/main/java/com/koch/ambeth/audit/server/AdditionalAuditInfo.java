package com.koch.ambeth.audit.server;

import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.collections.ArrayList;

public class AdditionalAuditInfo
{
	public final ArrayList<String> auditReasonContainer = new ArrayList<String>();

	public final ArrayList<String> auditContextContainer = new ArrayList<String>();

	public Boolean ownAuditMergeActive;

	public char[] clearTextPassword;

	public boolean doClearPassword;

	public IUser authorizedUser;
}