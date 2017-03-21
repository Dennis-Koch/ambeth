package com.koch.ambeth;

/*-
 * #%L
 * jambeth-ioc-test
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.ioc.link.LinkContainerTest;
import com.koch.ambeth.ioc.performance.IocPerformanceTest;
import com.koch.ambeth.testutil.AmbethIocRunnerTest;
import com.koch.ambeth.typeinfo.PropertyInfoProviderTest;
import com.koch.ambeth.typeinfo.PropertyInfoTest;
import com.koch.ambeth.util.ClassTupleExtendableContainerPerformanceTest;
import com.koch.ambeth.util.DelegatingConversionHelperTest;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.ioc.AllTests.class, AmbethIocRunnerTest.class, ClassTupleExtendableContainerPerformanceTest.class, LinkContainerTest.class,
		IocPerformanceTest.class, DelegatingConversionHelperTest.class, PropertyInfoProviderTest.class, PropertyInfoTest.class })
public class AllIocTests
{
}
