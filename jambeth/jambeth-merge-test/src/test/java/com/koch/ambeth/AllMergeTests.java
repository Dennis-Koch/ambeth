package com.koch.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.copy.ObjectCopierTest;
import com.koch.ambeth.util.XmlConfigUtilTest;

@RunWith(Suite.class)
@SuiteClasses({ ObjectCopierTest.class, XmlConfigUtilTest.class })
public class AllMergeTests
{
}
