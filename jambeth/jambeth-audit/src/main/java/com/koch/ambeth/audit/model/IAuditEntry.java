package com.koch.ambeth.audit.model;

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

import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;

@Audited(false)
public interface IAuditEntry {
	public static final String Protocol = "Protocol";

	public static final String Timestamp = "Timestamp";

	public static final String User = "User";

	public static final String UserIdentifier = "UserIdentifier";

	public static final String Services = "Services";

	public static final String SignatureOfUser = "SignatureOfUser";

	public static final String Entities = "Entities";

	public static final String Reason = "Reason";

	public static final String Context = "Context";

	public static final String HashAlgorithm = "HashAlgorithm";

	public static final String Signature = "Signature";

	int getProtocol();

	long getTimestamp();

	IUser getUser();

	String getUserIdentifier();

	String getReason();

	String getContext();

	ISignature getSignatureOfUser();

	char[] getSignature();

	String getHashAlgorithm();

	List<? extends IAuditedService> getServices();

	List<? extends IAuditedEntity> getEntities();
}
