package de.osthus.ambeth.orm20;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.orm20.independent.Orm20IndependentMetaDataTest;
import de.osthus.ambeth.orm20.independent.a22b.Orm20A22BTest;
import de.osthus.ambeth.orm20.independent.a2b.Orm20A2BTest;
import de.osthus.ambeth.orm20.independent.a2b2a.Orm20A2B2ATest;
import de.osthus.ambeth.orm20.independent.a3b.Orm20A3BTest;
import de.osthus.ambeth.orm20.independent.a3b2a.Orm20A3B2ATest;

@RunWith(Suite.class)
@SuiteClasses({ Orm20IndependentMetaDataTest.class, Orm20A22BTest.class, Orm20A2BTest.class, Orm20A2B2ATest.class, Orm20A3BTest.class, Orm20A3B2ATest.class })
public class AllOrm20Tests
{
}
