package de.osthus.ambeth.mixin;

import java.lang.reflect.InvocationTargetException;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EmbeddedMemberMixin
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	protected final SmartCopyMap<Class<?>, FastConstructor> typeToEmbbeddedParamConstructorMap = new SmartCopyMap<Class<?>, FastConstructor>(0.5f);

	public Object createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String memberPath)
	{
		Class<?> enhancedEmbeddedType = bytecodeEnhancer.getEnhancedType(embeddedType, new EmbeddedEnhancementHint(entityType, parentObject.getClass(),
				memberPath));
		FastConstructor embeddedConstructor = getEmbeddedParamConstructor(enhancedEmbeddedType, parentObject.getClass());
		Object[] constructorArgs = new Object[] { parentObject };
		try
		{
			return embeddedConstructor.newInstance(constructorArgs);
		}
		catch (InvocationTargetException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected <T> FastConstructor getEmbeddedParamConstructor(Class<T> embeddedType, Class<?> parentObjectType)
	{
		FastConstructor constructor = typeToEmbbeddedParamConstructorMap.get(embeddedType);
		if (constructor == null)
		{
			try
			{
				FastClass fastEmbeddedType = FastClass.create(embeddedType);
				constructor = fastEmbeddedType.getConstructor(new Class<?>[] { parentObjectType });
			}
			catch (Throwable e)
			{
				if (bytecodePrinter != null)
				{
					throw RuntimeExceptionUtil.mask(e, bytecodePrinter.toPrintableBytecode(embeddedType));
				}
				throw RuntimeExceptionUtil.mask(e);
			}
			typeToEmbbeddedParamConstructorMap.put(embeddedType, constructor);
		}
		return constructor;
	}
}
