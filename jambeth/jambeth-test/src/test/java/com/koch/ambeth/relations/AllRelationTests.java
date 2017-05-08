package com.koch.ambeth.relations;

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

@RunWith(Suite.class)
@SuiteClasses({
		com.koch.ambeth.relations.many.lazy.fk.forward.none.ManyLazyFkForwardNoneRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyFkNoReverseRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyNoReverseRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.link.reverse.none.ManyLazyNoLinkReverseRelationsTest.class,
		com.koch.ambeth.relations.one.lazy.fk.forward.none.OneLazyFkNoForwardRelationsTest.class,
		com.koch.ambeth.relations.one.lazy.fk.reverse.none.OneLazyFkNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneEagerNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneEagerVersionNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneLazyNoReverseRelationsTest.class})
public class AllRelationTests {

}
