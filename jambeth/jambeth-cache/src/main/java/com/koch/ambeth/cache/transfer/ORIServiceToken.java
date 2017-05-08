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

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.transfer.IServiceToken;

// TODO [DataContract(IsReference = true)]
public class ORIServiceToken extends ServiceToken<List<IObjRef>>
		implements IServiceToken<List<IObjRef>> {

	// TODO [DataMember]
	@Override
	public List<IObjRef> getValue() {
		return super.value;
	}

	@Override
	public void setValue(List<IObjRef> value) {
		super.value = value;
	}

}
