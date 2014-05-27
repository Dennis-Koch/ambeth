package de.osthus.ambeth.objectcollector;

import java.lang.reflect.Constructor;

import de.osthus.ambeth.util.ReflectUtil;

public class DefaultCollectableController implements ICollectableController
{
	protected static final Object[] nullParams = new Object[0];

	protected final IObjectCollector objectCollector;
	protected final Constructor<?> constructor;
	protected final boolean isCollectable;
	protected final boolean isCollectorAware;

	public DefaultCollectableController(Class<?> type, IObjectCollector objectCollector) throws NoSuchMethodException
	{
		if (!ICollectable.class.isAssignableFrom(type))
		{
			throw new IllegalStateException("Class " + type.getName() + " neither does implement interface '" + ICollectable.class.getName()
					+ "' nor is an instance of '" + ICollectableController.class.getName() + "' defined to handle this type");
		}
		Constructor<?>[] constructors = ReflectUtil.getConstructors(type);
		Constructor<?> constructorFound = null;
		for (Constructor<?> constructor : constructors)
		{
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 0 && constructorFound == null)
			{
				constructorFound = constructor;
			}
			else if (parameterTypes.length == 1 && IObjectCollector.class.equals(parameterTypes[0]))
			{
				constructorFound = constructor;
				break;
			}
		}
		this.objectCollector = objectCollector;
		this.constructor = constructorFound;
		isCollectable = ICollectable.class.isAssignableFrom(type);
		isCollectorAware = constructor.getParameterTypes().length == 1;
	}

	@Override
	public Object createInstance() throws Throwable
	{
		try
		{
			if (isCollectorAware)
			{
				return constructor.newInstance(objectCollector);
			}
			return constructor.newInstance(nullParams);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Error occured while instantiating type " + constructor.getDeclaringClass(), e);
		}
	}

	@Override
	public void initObject(Object object) throws Throwable
	{
		if (isCollectable)
		{
			((ICollectable) object).initInternDoNotCall();
		}
	}

	@Override
	public void disposeObject(Object object) throws Throwable
	{
		if (isCollectable)
		{
			((ICollectable) object).disposeInternDoNotCall();
		}
	}
}
