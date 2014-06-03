package de.osthus.ambeth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.bytecode.PublicConstructorVisitorTest;
import de.osthus.ambeth.merge.independent.IndependentEntityMetaDataClient20Test;
import de.osthus.ambeth.merge.independent.IndependentEntityMetaDataClientTest;

@RunWith(Suite.class)
@SuiteClasses({ IndependentEntityMetaDataClientTest.class, IndependentEntityMetaDataClient20Test.class, PublicConstructorVisitorTest.class })
public class AllMergeBytecodeTests
{
}
