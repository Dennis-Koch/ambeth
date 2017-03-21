package com.koch.ambeth.filter;

/*-
 * #%L
 * jambeth-filter
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
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPagingResponse<T> {
	int getSize();

	long getTotalSize();

	int getNumber();

	int getTotalNumber();

	List<IObjRef> getRefResult();

	void setRefResult(List<IObjRef> refResult);

	List<T> getResult();

	void setResult(List<T> result);
}
