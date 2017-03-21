package com.koch.ambeth.relations.one.fk.reverse.none;

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

import org.junit.Assert;

import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "LAZY")
public class OneLazyNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest {
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName) {
		super.assertBeforePrefetch(entityB, propertyName);

		int relationIndex =
				((IObjRefContainer) entityB).get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertNull(((IObjRefContainer) entityB).get__ObjRefs(relationIndex));
	}
}
