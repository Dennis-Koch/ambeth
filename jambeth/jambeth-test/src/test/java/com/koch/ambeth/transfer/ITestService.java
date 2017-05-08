package com.koch.ambeth.transfer;

/*-
 * #%L
 * jambeth-test
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

import java.util.Date;
import java.util.List;

import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ITestService {
	void noParamNoReturn();

	void primitiveParamNoReturn(int param);

	void dateParamNoReturn(Date param);

	void primitiveArrayParamNoReturn(int[] param);

	void primitiveListParamNoReturn(List<Integer> param);

	void entityParamNoReturn(MaterialGroup param);

	void entityWithRelationParamNoReturn(Material param);

	void mixedParamsNoReturn(int number, Material material1, String text, MaterialGroup materialGroup,
			Material material2, Date date);

	int noParamPrimitiveReturn();

	Date noParamDateReturn();

	int[] noParamPrimitiveArrayReturn();

	List<Integer> noParamPrimitiveListReturn();

	MaterialGroup noParamEntityReturn();

	Material noParamEntityWithRelationReturn();
}
