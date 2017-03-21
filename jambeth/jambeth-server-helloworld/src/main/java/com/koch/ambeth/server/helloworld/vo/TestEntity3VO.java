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


public class TestEntity3VO extends AbstractEntityVO
{
	protected TestEntityVO test;

	protected String myValue3;

	public TestEntityVO getTest()
	{
		return test;
	}

	public void setTest(TestEntityVO test)
	{
		this.test = test;
	}

	public void setMyValue3(String myValue3)
	{
		this.myValue3 = myValue3;
	}

	public String getMyValue3()
	{
		return myValue3;
	}
}
