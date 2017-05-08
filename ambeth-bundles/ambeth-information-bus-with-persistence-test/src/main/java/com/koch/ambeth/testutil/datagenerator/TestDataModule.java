package com.koch.ambeth.testutil.datagenerator;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.testutil.datagenerator.setter.BooleanTestSetter;
import com.koch.ambeth.testutil.datagenerator.setter.DateTestSetter;
import com.koch.ambeth.testutil.datagenerator.setter.IntegerTestSetter;
import com.koch.ambeth.testutil.datagenerator.setter.StringTestSetter;
import com.koch.ambeth.testutil.datagenerator.setter.XmlCalendarTestSetter;

public class TestDataModule implements IInitializingModule {

	public static final String TEST_DATA_SETTER_STRING = "TestDataSetterString";
	public static final String TEST_DATA_SETTER_BOOLEAN = "TestDataSetterBoolean";
	public static final String TEST_DATA_SETTER_DATE = "TestDataSetterDate";
	public static final String TEST_DATA_SETTER_INT = "TestDataSetterInt";
	public static final String TEST_DATA_SETTER_CALENDAR = "TestDataSetterCalendar";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerAnonymousBean(TestDataGenerator.class)
				.autowireable(ITestDataGenerator.class, ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_STRING, StringTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_STRING).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_BOOLEAN, BooleanTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_BOOLEAN).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_DATE, DateTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_DATE).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_INT, IntegerTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_INT).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_CALENDAR, XmlCalendarTestSetter.class)
				.propertyRefs(TEST_DATA_SETTER_DATE);
		beanContextFactory.link(TEST_DATA_SETTER_CALENDAR).to(ITestSetterExtendable.class);

	}

}
