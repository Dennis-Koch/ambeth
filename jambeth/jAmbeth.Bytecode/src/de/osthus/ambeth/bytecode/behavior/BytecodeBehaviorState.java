package de.osthus.ambeth.bytecode.behavior;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.IValueResolveDelegate;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.MethodKeyOfType;
import de.osthus.ambeth.util.ReflectUtil;

public class BytecodeBehaviorState implements IBytecodeBehaviorState
{
	private static final ThreadLocal<IBytecodeBehaviorState> stateTL = new ThreadLocal<IBytecodeBehaviorState>();

	public static IBytecodeBehaviorState getState()
	{
		return stateTL.get();
	}

	public static <T> T setState(Class<?> originalType, Class<?> currentType, Type newType, IServiceContext beanContext, IEnhancementHint context,
			IResultingBackgroundWorkerDelegate<T> runnable)
	{
		IBytecodeBehaviorState oldState = stateTL.get();
		stateTL.set(new BytecodeBehaviorState(currentType, newType, originalType, beanContext, context));
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (oldState != null)
			{
				stateTL.set(oldState);
			}
			else
			{
				stateTL.remove();
			}
		}
	}

	private final Class<?> currentType;
	private final Type newType;
	private final Class<?> originalType;
	private final IServiceContext beanContext;
	private final IEnhancementHint context;

	private final HashMap<MethodKeyOfType, MethodInstance> implementedMethods = new HashMap<MethodKeyOfType, MethodInstance>();

	private final HashMap<String, PropertyInstance> implementedProperties = new HashMap<String, PropertyInstance>();

	private final HashMap<String, FieldInstance> implementedFields = new HashMap<String, FieldInstance>();

	private final HashMap<String, IValueResolveDelegate> initializeStaticFields = new HashMap<String, IValueResolveDelegate>();

	public BytecodeBehaviorState(Class<?> currentType, Type newType, Class<?> originalType, IServiceContext beanContext, IEnhancementHint context)
	{
		this.currentType = currentType;
		this.newType = newType;
		this.originalType = originalType;
		this.beanContext = beanContext;
		this.context = context;
	}

	@Override
	public Class<?> getCurrentType()
	{
		return currentType;
	}

	@Override
	public Type getNewType()
	{
		return newType;
	}

	@Override
	public Class<?> getOriginalType()
	{
		return originalType;
	}

	@Override
	public IServiceContext getBeanContext()
	{
		return beanContext;
	}

	@Override
	public IEnhancementHint getContext()
	{
		return context;
	}

	@Override
	public <T extends IEnhancementHint> T getContext(Class<T> contextType)
	{
		return context.unwrap(contextType);
	}

	public void methodImplemented(MethodInstance method)
	{
		if (!implementedMethods.putIfNotExists(new MethodKeyOfType(method.getName(), method.getParameters()), method))
		{
			throw new IllegalStateException("Method already implemented: " + method);
		}
	}

	public void fieldImplemented(FieldInstance field)
	{
		if (!implementedFields.putIfNotExists(field.getName(), field))
		{
			throw new IllegalStateException("Field already implemented: " + field);
		}
	}

	public void propertyImplemented(PropertyInstance property)
	{
		if (!implementedProperties.putIfNotExists(property.getName(), property))
		{
			throw new IllegalStateException("Property already implemented: " + property);
		}
	}

	public void queueFieldInitialization(String fieldName, IValueResolveDelegate value)
	{
		if (!initializeStaticFields.putIfNotExists(fieldName, value))
		{
			throw new IllegalStateException("Field already queued for initialization: " + fieldName);
		}
	}

	@Override
	public PropertyInstance getProperty(String propertyName)
	{
		PropertyInstance pi = implementedProperties.get(propertyName);
		if (pi != null)
		{
			return pi;
		}
		return PropertyInstance.findByTemplate(getCurrentType(), propertyName, true);
	}

	@Override
	public MethodInstance[] getAlreadyImplementedMethodsOnNewType()
	{
		return implementedMethods.toArray(MethodInstance.class);
	}

	@Override
	public FieldInstance getAlreadyImplementedField(String fieldName)
	{
		FieldInstance field = implementedFields.get(fieldName);
		if (field == null)
		{
			Field[] declaredFieldInHierarchy = ReflectUtil.getDeclaredFieldInHierarchy(getCurrentType(), fieldName);
			if (declaredFieldInHierarchy != null && declaredFieldInHierarchy.length > 0)
			{
				field = new FieldInstance(declaredFieldInHierarchy[0]);
			}
		}
		return field;
	}

	@Override
	public boolean hasMethod(MethodInstance method)
	{
		MethodInstance existingMethod = MethodInstance.findByTemplate(method, true);
		return existingMethod != null && getState().getNewType().equals(existingMethod.getOwner());
	}

	@Override
	public boolean isMethodAlreadyImplementedOnNewType(MethodInstance method)
	{
		return implementedMethods.containsKey(new MethodKeyOfType(method.getName(), method.getParameters()));
	}

	public void postProcessCreatedType(Class<?> newType)
	{
		for (Entry<String, IValueResolveDelegate> entry : initializeStaticFields)
		{
			Field field = ReflectUtil.getDeclaredField(newType, entry.getKey());
			if (field == null)
			{
				throw new IllegalStateException("Field not found: '" + newType.getName() + "." + entry.getKey());
			}
			Object value = entry.getValue().invoke(entry.getKey(), newType);
			try
			{
				field.set(null, value);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e, "Error occured while setting field: " + field);
			}
		}
		initializeStaticFields.clear();
	}
}
