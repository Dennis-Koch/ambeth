package de.osthus.ambeth.relations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.relations.many.lazy.fk.forward.none.ManyLazyFkForwardNoneRelationsTest.class,
		de.osthus.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyFkNoReverseRelationsTest.class,
		de.osthus.ambeth.relations.many.lazy.fk.reverse.none.ManyLazyNoReverseRelationsTest.class,
		de.osthus.ambeth.relations.many.lazy.link.reverse.none.ManyLazyNoLinkReverseRelationsTest.class,
		de.osthus.ambeth.relations.one.lazy.fk.reverse.none.OneLazyFkNoReverseRelationsTest.class,
		de.osthus.ambeth.relations.one.fk.reverse.none.OneEagerNoReverseRelationsTest.class,
		de.osthus.ambeth.relations.one.fk.reverse.none.OneEagerVersionNoReverseRelationsTest.class,
		de.osthus.ambeth.relations.one.fk.reverse.none.OneLazyNoReverseRelationsTest.class })
public class AllRelationTests
{

}
