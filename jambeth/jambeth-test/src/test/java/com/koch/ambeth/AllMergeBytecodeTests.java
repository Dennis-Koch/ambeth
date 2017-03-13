package com.koch.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.koch.ambeth.merge.independent.IndependentEntityMetaDataClient20Test;
import com.koch.ambeth.merge.independent.IndependentEntityMetaDataClientTest;

@RunWith(Suite.class)
@SuiteClasses({ IndependentEntityMetaDataClientTest.class, IndependentEntityMetaDataClient20Test.class })
public class AllMergeBytecodeTests
{
}
