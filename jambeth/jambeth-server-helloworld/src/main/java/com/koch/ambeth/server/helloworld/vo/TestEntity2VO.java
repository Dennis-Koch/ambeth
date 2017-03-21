package com.koch.ambeth.server.helloworld.vo;

/*-
 * #%L
 * jambeth-server-helloworld
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

public class TestEntity2VO extends AbstractEntityVO
{
	protected int myValue2;

	public void setMyValue2(int myValue2)
	{
		this.myValue2 = myValue2;
	}

	public int getMyValue2()
	{
		return myValue2;
	}
}
