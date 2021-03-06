package com.koch.ambeth.service;

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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.transfer.ITestService;
import com.koch.ambeth.util.ParamChecker;

@Service(ITestService.class)
public class TestService implements ITestService, IInitializingBean {
	protected ICache cache;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(cache, "Cache");
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}

	@Override
	public void noParamNoReturn() {
	}

	@Override
	public void primitiveParamNoReturn(int param) {
	}

	@Override
	public void dateParamNoReturn(Date param) {
	}

	@Override
	public void primitiveArrayParamNoReturn(int[] param) {
	}

	@Override
	public void primitiveListParamNoReturn(List<Integer> param) {
	}

	@Override
	public void entityParamNoReturn(MaterialGroup param) {
	}

	@Override
	public void entityWithRelationParamNoReturn(Material param) {
	}

	@Override
	public void mixedParamsNoReturn(int number, Material material1, String text,
			MaterialGroup materialGroup, Material material2, Date date) {
	}

	@Override
	public int noParamPrimitiveReturn() {
		return 1;
	}

	@Override
	public Date noParamDateReturn() {
		return new Date();
	}

	@Override
	public int[] noParamPrimitiveArrayReturn() {
		return new int[] { 1, 2, 34 };
	}

	@Override
	public List<Integer> noParamPrimitiveListReturn() {
		return Arrays.asList(12, 3, 4);
	}

	@Override
	public MaterialGroup noParamEntityReturn() {
		return cache.getObject(MaterialGroup.class, "1");
	}

	@Override
	public Material noParamEntityWithRelationReturn() {
		return cache.getObject(Material.class, 1);
	}
}
