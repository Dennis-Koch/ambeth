package com.koch.ambeth.persistence.xml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EmbeddedTypeTest.class, IndependentMetaDataComparisonTest.class, QueryBuilderTest.class, SelectWhereTest.class, ValueObjectTest.class })
public class AllPersistenceXmlTests
{

}
