package de.osthus.ambeth.testutil.datagenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.util.ParamChecker;

public class TestDataGenerator implements IInitializingBean, ITestSetterExtendable, ITestDataGenerator
{
	@Autowired
	IEntityFactory entityFactory;

	protected final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();
	{
		wrapperPrimitiveMap.put(Boolean.class, Boolean.TYPE);
		wrapperPrimitiveMap.put(Byte.class, Byte.TYPE);
		wrapperPrimitiveMap.put(Character.class, Character.TYPE);
		wrapperPrimitiveMap.put(Short.class, Short.TYPE);
		wrapperPrimitiveMap.put(Integer.class, Integer.TYPE);
		wrapperPrimitiveMap.put(Long.class, Long.TYPE);
		wrapperPrimitiveMap.put(Double.class, Double.TYPE);
		wrapperPrimitiveMap.put(Float.class, Float.TYPE);
		wrapperPrimitiveMap.put(Void.TYPE, Void.TYPE);
	}

	protected final IExtendableContainer<ITestSetter> testSetter = new DefaultExtendableContainer<ITestSetter>(ITestSetter.class, "testSetter");

	protected Class<?> wrapperToPrimitive(final Class<?> cls)
	{
		return wrapperPrimitiveMap.get(cls);
	}

	@Override
	public void registerTestSetter(ITestSetter extension)
	{
		testSetter.register(extension);
	}

	@Override
	public void unregisterTestSetter(ITestSetter extension)
	{
		testSetter.unregister(extension);
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(testSetter, "testSetter");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.basf.ap.soa.services.rnd.logistic.delivery.bservice.testdata.ITestDataGenerator#generateTestClass(java.lang .Class)
	 */
	@Override
	public <T> T generateTestClass(Class<T> type, String... toIgnore) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException
	{
		return generateTestClass(type, null, toIgnore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.basf.ap.soa.services.rnd.logistic.delivery.bservice.testdata.ITestDataGenerator#generateTestClass(java.lang .Class, java.util.Map)
	 */
	@Override
	public <T> T generateTestClass(Class<T> type, Map<Object, Object> arguments, String... toIgnore) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{

		Set<String> toIgnoreSet = getToIgnoreSet(toIgnore);

		T instance = entityFactory.createEntity(type);

		if (instance != null && instance instanceof IEntityMetaDataHolder)
		{
			Set<Member> members = getMembers((IEntityMetaDataHolder) instance);

			for (Member member : members)
			{
				String name = member.getName();
				if (toIgnoreSet.contains(name))
				{
					continue;
				}

				Class<?> parameterType = member.getRealType();

				for (ITestSetter setter : testSetter.getExtensions())
				{
					if (setter.isApplicable(parameterType))
					{
						Object parameter = setter.createParameter(name, arguments);
						member.setValue(instance, parameter);
						continue;
					}
				}
			}
			return instance;
		}
		else
		{
			throw new IllegalArgumentException("Entity creation for given type failed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.basf.ap.soa.services.rnd.logistic.delivery.bservice.testdata.ITestDataGenerator#checkTestInstance(java.lang .Object, java.lang.String)
	 */
	@Override
	public void checkTestInstance(Object instance, String... toIgnore) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException
	{
		checkTestInstance(instance, null, toIgnore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.basf.ap.soa.services.rnd.logistic.delivery.bservice.testdata.ITestDataGenerator#checkTestInstance(java.lang .Object, java.util.Map,
	 * java.lang.String)
	 */
	@Override
	public void checkTestInstance(Object instance, Map<Object, Object> arguments, String... toIgnore) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException
	{
		Assert.assertNotNull(instance);
		Assert.assertTrue(instance instanceof IEntityMetaDataHolder);

		Set<String> toIgnoreSet = getToIgnoreSet(toIgnore);

		Set<Member> members = getMembers((IEntityMetaDataHolder) instance);

		for (Member member : members)
		{
			String name = member.getName();
			if (toIgnoreSet.contains(name))
			{
				continue;
			}

			Class<?> parameterType = member.getRealType();

			for (ITestSetter setter : testSetter.getExtensions())
			{

				if (setter.isApplicable(parameterType))
				{
					Object content = member.getValue(instance);
					setter.compareResult(name, arguments, content);
				}
			}
		}
	}

	/**
	 * Get all properties from an entity (excluding Ambeth enhancements)
	 * 
	 * @param instance
	 *            "enhanced" entity containing entity meta data
	 * @return a {@link Set} of all publicly accessible properties ({@link Member}s)
	 */
	private Set<Member> getMembers(IEntityMetaDataHolder instance)
	{
		Set<Member> members = new HashSet<Member>();
		IEntityMetaData entityMetaData = instance.get__EntityMetaData();
		members.addAll(Arrays.asList(entityMetaData.getPrimitiveMembers()));
		members.addAll(Arrays.asList(entityMetaData.getRelationMembers()));
		return members;
	}

	/**
	 * Extends the set of attributes to ignore with the technical attributes.
	 * 
	 * @param toIgnore
	 * @return
	 */
	protected Set<String> getToIgnoreSet(String... toIgnore)
	{
		Set<String> toIgnoreSet = new HashSet<String>();
		toIgnoreSet.addAll(Arrays.asList(toIgnore));
		// Technical Attributes are always ignored!
		toIgnoreSet.addAll(Arrays.asList(EntityData.TECHNICAL_ATTRIBUTES));
		return toIgnoreSet;
	}

	@Override
	public void compareTestInstance(Object expected, Object instance, boolean recursive, String... toIgnore) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		compareTestInstance(expected, instance, recursive, new HashSet<Object>(), getToIgnoreSet(toIgnore));
	}

	protected void compareTestInstance(Object expected, Object instance, boolean recursive, Set<Object> alreadyCompared, Set<String> toIgnoreSet)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{

		if (expected == instance)
		{
			return;
		}

		toIgnoreSet.add("AssignableFrom");
		toIgnoreSet.add("Class");

		Assert.assertNotNull(instance.toString(), expected);
		Assert.assertNotNull(expected.toString(), instance);

		Class<? extends Object> type = expected.getClass();
		if (Collection.class.isAssignableFrom(type))
		{
			// Special behavior for Collections:
			Collection<?> expectedCol = (Collection<?>) expected;
			Collection<?> instanceCol = (Collection<?>) instance;

			Assert.assertEquals(expectedCol.size(), instanceCol.size());
			for (Iterator<?> instanceIt = instanceCol.iterator(), expectedIt = expectedCol.iterator(); instanceIt.hasNext() && expectedIt.hasNext();)
			{
				Object expectedItem = expectedIt.next();
				Object instanceItem = instanceIt.next();
				if (expectedItem == null && instanceItem == null)
				{
					continue;
				}
				Assert.assertNotNull(instance.getClass().getName(), expectedItem);
				Assert.assertNotNull(instance.getClass().getName(), instanceItem);
				Class<?> clazz = expectedItem.getClass();
				if (wrapperToPrimitive(clazz) != null || clazz.isPrimitive() || String.class.isAssignableFrom(clazz))
				{
					Assert.assertEquals(instance.getClass().getName(), expectedItem, instanceItem);
				}
				else if (recursive && !alreadyCompared.contains(expectedItem))
				{
					alreadyCompared.add(expectedItem);
					compareTestInstance(expectedItem, instanceItem, recursive, alreadyCompared, toIgnoreSet);
				}
			}
		}
		else
		{
			Set<Member> members = getMembers((IEntityMetaDataHolder) instance);

			for (Member member : members)
			{
				String name = member.getName();
				if (toIgnoreSet.contains(name))
				{
					continue;
				}

				Object expectedContent = member.getValue(expected);
				Object content = member.getValue(instance);
				Class<?> parameterType = member.getRealType();
				if (wrapperToPrimitive(parameterType) != null || parameterType.isPrimitive() || String.class.isAssignableFrom(parameterType))
				{
					Assert.assertEquals(name, expectedContent, content);
				}
				else if (recursive && !alreadyCompared.contains(expectedContent))
				{
					alreadyCompared.add(expectedContent);
					compareTestInstance(expectedContent, content, recursive, alreadyCompared, toIgnoreSet);
				}
			}
		}
	}
}
