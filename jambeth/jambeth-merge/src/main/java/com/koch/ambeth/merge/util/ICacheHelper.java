package com.koch.ambeth.merge.util;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface ICacheHelper
{
	Object createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType);

	Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType, Class<?> elementType);

	Object[] extractPrimitives(IEntityMetaData metaData, Object obj);

	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj);

	/**
	 * 
	 * @param metaData
	 *            Meta data of obj
	 * @param obj
	 *            Entity to extract the relations from
	 * @param relationValues
	 *            Actual relation values (out param)
	 * @return ORI array for the relations
	 */
	IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues);
}
