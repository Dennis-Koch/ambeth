package com.koch.ambeth.ioc;

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

import com.koch.ambeth.ioc.annotation.AutowiredTest;
import com.koch.ambeth.ioc.beanruntime.BeanRuntimeTest;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainerTest;
import com.koch.ambeth.ioc.extendable.ExtendableBeanTest;
import com.koch.ambeth.ioc.injection.InjectionTest;
import com.koch.ambeth.util.ClassTupleExtendableContainerTest;

@RunWith(Suite.class)
@SuiteClasses({AutowiredTest.class, ServiceContextTest.class, ClassExtendableContainerTest.class,
		ExtendableBeanTest.class, ClassTupleExtendableContainerTest.class, BeanRuntimeTest.class,
		InjectionTest.class})
public class AllTests {

}
