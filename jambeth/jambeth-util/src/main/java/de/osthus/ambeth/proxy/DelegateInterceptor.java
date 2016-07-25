package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;

public class DelegateInterceptor extends AbstractSimpleInterceptor
{
	protected final Object target;

	protected final MethodAccess methodAccess;

	protected final IMap<Method, Object> methodMap;

	public DelegateInterceptor(Object target, IMap<Method, Method> methodMap)
	{
		this.target = target;
		methodAccess = MethodAccess.get(target.getClass());
		this.methodMap = HashMap.create(methodMap.size(), 0.5f);
		for (Entry<Method, Method> entry : methodMap)
		{
			Method method = entry.getKey();
			Method mappedMethod = entry.getValue();
			try
			{
				// first try with "reflect asm"
				int indexOfMethod = methodAccess.getIndex(mappedMethod.getName(), mappedMethod.getParameterTypes());
				this.methodMap.put(method, Integer.valueOf(indexOfMethod));
			}
			catch (Throwable e)
			{
				// fallback with "plain old reflection"
				this.methodMap.put(method, mappedMethod);
			}
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		try
		{
			Object mapping = methodMap.get(method);
			if (mapping == null)
			{
				return method.invoke(target, args);
			}
			if (mapping instanceof Integer)
			{
				int index = ((Integer) mapping).intValue();
				return methodAccess.invoke(target, index, args);
			}
			else
			{
				Method mappedMethod = (Method) mapping;
				int expectedArgsLength = mappedMethod.getParameterTypes().length;
				if (expectedArgsLength != args.length)
				{
					Object[] newArgs = new Object[expectedArgsLength];
					System.arraycopy(args, 0, newArgs, 0, expectedArgsLength);
					args = newArgs;
				}
				return mappedMethod.invoke(target, args);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}
}
