package de.osthus.ambeth.ioc.proxy;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.WeakSmartCopyMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.typeinfo.FieldPropertyInfo;
import de.osthus.ambeth.typeinfo.FieldPropertyInfoASM;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.MethodPropertyInfoASM;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class CallingProxyPostProcessor extends WeakSmartCopyMap<Class<?>, Reference<IPropertyInfo[]>> implements IInitializingBean
{
	protected static final IPropertyInfo[] EMPTY_MEMBERS = new IPropertyInfo[0];

	protected static final SoftReference<IPropertyInfo[]> EMPTY_MEMBERS_R = new SoftReference<IPropertyInfo[]>(EMPTY_MEMBERS);

	protected final AnnotationCache<Self> logInstanceCache = new AnnotationCache<Self>(Self.class)
	{
		@Override
		protected boolean annotationEquals(Self left, Self right)
		{
			return left == right;
		}
	};

	protected IPropertyInfoProvider propertyInfoProvider;

	public CallingProxyPostProcessor()
	{
		super(0.5f);
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	public void beanPostProcessed(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			Object proxyBean, Object targetBean)
	{
		IPropertyInfo[] members = getMembers(targetBean.getClass());
		if (members.length == 0)
		{
			return;
		}
		Class<?> proxyBeanType = proxyBean.getClass();
		for (IPropertyInfo member : members)
		{
			if (member.getPropertyType().isAssignableFrom(proxyBeanType))
			{
				// Only if the proxy can be cast to the needed type we use it
				member.setValue(targetBean, proxyBean);
			}
			else
			{
				// Otherwise we choose the target bean itself for injection
				member.setValue(targetBean, targetBean);
			}
		}
	}

	protected IPropertyInfo[] getMembers(Class<?> type)
	{
		Reference<IPropertyInfo[]> membersR = get(type);
		IPropertyInfo[] members = null;
		if (membersR != null)
		{
			members = membersR.get();
		}
		if (members != null)
		{
			return members;
		}
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			membersR = get(type);
			if (membersR != null)
			{
				members = membersR.get();
			}
			if (members != null)
			{
				return members;
			}
			ArrayList<IPropertyInfo> targetMembers = new ArrayList<IPropertyInfo>();
			scanForCallingProxyField(type, type, targetMembers);
			members = targetMembers.toArray(IPropertyInfo.class);
			put(type, targetMembers.size() > 0 ? new SoftReference<IPropertyInfo[]>(members) : EMPTY_MEMBERS_R);
			return members;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void scanForCallingProxyField(Class<?> beanType, Class<?> type, List<IPropertyInfo> targetMembers)
	{
		if (type == null || Object.class.equals(type))
		{
			return;
		}
		scanForCallingProxyField(beanType, type.getSuperclass(), targetMembers);
		FieldAccess fieldAccess = null;
		Field[] fields = ReflectUtil.getDeclaredFields(type);
		for (int a = fields.length; a-- > 0;)
		{
			Field field = fields[a];
			if (!field.getType().isAssignableFrom(beanType))
			{
				continue;
			}
			Self callingProxy = logInstanceCache.getAnnotation(field);
			if (callingProxy == null)
			{
				continue;
			}
			field.setAccessible(true);
			IPropertyInfo member;
			if ((field.getModifiers() & Modifier.PRIVATE) != 0)
			{
				member = new FieldPropertyInfo(field.getDeclaringClass(), propertyInfoProvider.getPropertyNameFor(field), field);
			}
			else
			{
				if (fieldAccess == null)
				{
					fieldAccess = FieldAccess.get(field.getDeclaringClass());
				}
				member = new FieldPropertyInfoASM(field.getDeclaringClass(), propertyInfoProvider.getPropertyNameFor(field), field, null, fieldAccess);
			}
			targetMembers.add(member);
		}
		MethodAccess methodAccess = null;
		Method[] methods = ReflectUtil.getDeclaredMethods(type);
		for (int a = methods.length; a-- > 0;)
		{
			Method method = methods[a];
			Self callingProxy = logInstanceCache.getAnnotation(method);
			if (callingProxy == null)
			{
				continue;
			}
			if (method.getParameterTypes().length == 0)
			{
				// Methods without parameter can not be invoked here
				continue;
			}
			method.setAccessible(true);
			IPropertyInfo member;
			if ((method.getModifiers() & Modifier.PRIVATE) != 0)
			{
				member = new MethodPropertyInfo(method.getDeclaringClass(), propertyInfoProvider.getPropertyNameFor(method), null, method);
			}
			else
			{
				if (methodAccess == null)
				{
					methodAccess = MethodAccess.get(method.getDeclaringClass());
				}
				member = new MethodPropertyInfoASM(method.getDeclaringClass(), propertyInfoProvider.getPropertyNameFor(method), null, method, null,
						methodAccess);
			}
			targetMembers.add(member);
		}
	}
}
