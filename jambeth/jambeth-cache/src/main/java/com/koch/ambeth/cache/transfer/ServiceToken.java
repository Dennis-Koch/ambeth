package com.koch.ambeth.cache.transfer;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.util.transfer.IServiceToken;
import com.koch.ambeth.util.transfer.IToken;

// TODO [DataContract(IsReference = true)]
public class ServiceToken<T> implements IServiceToken<T>
{

	// TODO [DataMember]
	protected IToken token;

	// TODO [DataMember]
	protected T value;

	@Override
	public IToken getToken()
	{
		return token;
	}

	@Override
	public void setToken(IToken token)
	{
		this.token = token;
	}

	@Override
	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

}
