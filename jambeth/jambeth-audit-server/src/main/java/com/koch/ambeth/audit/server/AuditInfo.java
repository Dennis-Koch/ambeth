package com.koch.ambeth.audit.server;

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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.audit.model.AuditedArg;

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
