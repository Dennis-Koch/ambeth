package de.osthus.ambeth.testutil.datagenerator;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.datagenerator.setter.BooleanTestSetter;
import de.osthus.ambeth.testutil.datagenerator.setter.DateTestSetter;
import de.osthus.ambeth.testutil.datagenerator.setter.IntegerTestSetter;
import de.osthus.ambeth.testutil.datagenerator.setter.StringTestSetter;
import de.osthus.ambeth.testutil.datagenerator.setter.XmlCalendarTestSetter;

public class TestDataModule implements IInitializingModule
{

	public static final String TEST_DATA_SETTER_STRING = "TestDataSetterString";
	public static final String TEST_DATA_SETTER_BOOLEAN = "TestDataSetterBoolean";
	public static final String TEST_DATA_SETTER_DATE = "TestDataSetterDate";
	public static final String TEST_DATA_SETTER_INT = "TestDataSetterInt";
	public static final String TEST_DATA_SETTER_CALENDAR = "TestDataSetterCalendar";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(TestDataGenerator.class).autowireable(ITestDataGenerator.class, ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_STRING, StringTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_STRING).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_BOOLEAN, BooleanTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_BOOLEAN).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_DATE, DateTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_DATE).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_INT, IntegerTestSetter.class);
		beanContextFactory.link(TEST_DATA_SETTER_INT).to(ITestSetterExtendable.class);

		beanContextFactory.registerBean(TEST_DATA_SETTER_CALENDAR, XmlCalendarTestSetter.class).propertyRefs(TEST_DATA_SETTER_DATE);
		beanContextFactory.link(TEST_DATA_SETTER_CALENDAR).to(ITestSetterExtendable.class);

	}

}
