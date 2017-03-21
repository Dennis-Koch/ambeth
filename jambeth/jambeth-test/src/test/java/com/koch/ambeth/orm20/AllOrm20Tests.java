package com.koch.ambeth.orm20;

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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.orm20.independent.Orm20IndependentMetaDataTest;
import com.koch.ambeth.orm20.independent.a22b.Orm20A22BTest;
import com.koch.ambeth.orm20.independent.a2b.Orm20A2BTest;
import com.koch.ambeth.orm20.independent.a2b2a.Orm20A2B2ATest;
import com.koch.ambeth.orm20.independent.a3b.Orm20A3BTest;
import com.koch.ambeth.orm20.independent.a3b2a.Orm20A3B2ATest;

@RunWith(Suite.class)
@SuiteClasses({Orm20IndependentMetaDataTest.class, Orm20A22BTest.class, Orm20A2BTest.class,
		Orm20A2B2ATest.class, Orm20A3BTest.class, Orm20A3B2ATest.class})
public class AllOrm20Tests {
}
