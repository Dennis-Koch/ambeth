package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.util.StringBuilderUtil;

public class DefaultServiceFilter implements IServiceFilter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType, IUserHandle userHandle)
	{
		if (userHandle == null || !userHandle.isValid())
		{
			if (SecurityContextType.NOT_REQUIRED.equals(securityContextType))
			{
				return CallPermission.ALLOWED;
			}
			return CallPermission.FORBIDDEN;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		ArrayList<String> methodSignatures = new ArrayList<String>();

		// Class<?>[] parameters = method.getParameterTypes();
		// Type interfaceType = method.DeclaringType;
		// Type[] interfaces = service.GetType().GetInterfaces();
		// for (int a = interfaces.Length; a-- > 0; )
		// {
		// Type oneInterface = interfaces[a];
		// MethodInfo oneMethod = oneInterface.GetMethod(method.Name, parameters);
		// if (oneMethod != null)
		// {
		// methodSignatures.Add(interfaces[a].ToString() + "." + method.Name);
		// }
		// }

		methodSignatures.add(StringBuilderUtil.concat(tlObjectCollector, method.getDeclaringClass().getName(), ".", method.getName()));

		CallPermission callPermission = CallPermission.FORBIDDEN;
		for (IUseCase usecase : userHandle.getUseCases())
		{
			for (Pattern pattern : usecase.getPatterns())
			{
				for (String methodSignature : methodSignatures)
				{
					if (pattern.matcher(methodSignature).matches())
					{
						switch (usecase.getApplyType())
						{
							case ALLOW:
								callPermission = CallPermission.ALLOWED;
								break;
							case DENY:
								return CallPermission.FORBIDDEN;
							default:
								throw new IllegalArgumentException(UsecaseApplyType.class.getName() + " not supported: " + usecase.getApplyType());
						}
					}
				}
			}
		}
		return callPermission;
	}
}