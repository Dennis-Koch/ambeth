package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.copy.ObjectCopierTest;
import de.osthus.ambeth.util.XmlConfigUtilTest;

@RunWith(Suite.class)
@SuiteClasses({ ObjectCopierTest.class, XmlConfigUtilTest.class })
public class AllMergeTests
{
}
