package com.koch.ambeth.relations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.relations.many.lazy.fk.forward.none.ManyLazyFkForwardNoneRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyFkNoReverseRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyNoReverseRelationsTest.class,
		com.koch.ambeth.relations.many.lazy.link.reverse.none.ManyLazyNoLinkReverseRelationsTest.class,
		com.koch.ambeth.relations.one.lazy.fk.forward.none.OneLazyFkNoForwardRelationsTest.class,
		com.koch.ambeth.relations.one.lazy.fk.reverse.none.OneLazyFkNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneEagerNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneEagerVersionNoReverseRelationsTest.class,
		com.koch.ambeth.relations.one.fk.reverse.none.OneLazyNoReverseRelationsTest.class })
public class AllRelationTests
{

}
