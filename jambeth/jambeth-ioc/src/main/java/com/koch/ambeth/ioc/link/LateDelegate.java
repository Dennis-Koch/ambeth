package com.koch.ambeth.ioc.link;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.IDelegate;
import com.koch.ambeth.util.IDelegateFactory;

public class LateDelegate
{
	protected String methodName;

	protected Class<?> delegateType;

	protected IDelegateFactory delegateFactory;

	public LateDelegate(Class<?> delegateType, String methodName, IDelegateFactory delegateFactory)
	{
		this.methodName = methodName;
		this.delegateType = delegateType;
		this.delegateFactory = delegateFactory;
	}

	public IDelegate getDelegate(Class<?> delegateType, Object target)
	{
		if (this.delegateType != null)
		{
			delegateType = this.delegateType;
		}
		return delegateFactory.createDelegate(delegateType, target, methodName);
	}
}
