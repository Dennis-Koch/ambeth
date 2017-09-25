package com.koch.ambeth.query.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class QueryBuilderProxyInterceptor implements MethodInterceptor {
	private static final Class<?>[] INTERFACES = new Class<?>[] {IPropertyPath.class};

	public static interface IPropertyPath {
		String getPropertyPath();
	}

	public static String getPropertyPath(Object propertyProxy,
			ThreadLocal<String> lastPropertyPathTL) {
		if (propertyProxy instanceof IPropertyPath) {
			return ((IPropertyPath) propertyProxy).getPropertyPath();
		}
		if (propertyProxy instanceof CharSequence) {
			return ((CharSequence) propertyProxy).toString();
		}
		String lastPropertyPath = lastPropertyPathTL.get();
		lastPropertyPathTL.set(null);
		return lastPropertyPath;
	}

	public static <T> T createProxy(Class<T> type, IEntityMetaData metaData, String propertyPath,
			ThreadLocal<String> lastPropertyPathTL, IPropertyInfoProvider propertyInfoProvider,
			IEntityMetaDataProvider entityMetaDataProvider, IProxyFactory proxyFactory) {
		return proxyFactory.createProxy(type, INTERFACES,
				new QueryBuilderProxyInterceptor(metaData, type, propertyPath, lastPropertyPathTL,
						propertyInfoProvider, entityMetaDataProvider, proxyFactory));
	}

	private final IEntityMetaData currMetaData;

	private final Class<?> baseEntityType;

	private final String propertyPath;

	private final IPropertyInfoProvider propertyInfoProvider;

	private final IEntityMetaDataProvider entityMetaDataProvider;

	private final IProxyFactory proxyFactory;

	private final ThreadLocal<String> lastPropertyPathTL;

	public QueryBuilderProxyInterceptor(IEntityMetaData currMetaData, Class<?> baseEntityType,
			String propertyPath, ThreadLocal<String> lastPropertyPathTL,
			IPropertyInfoProvider propertyInfoProvider, IEntityMetaDataProvider entityMetaDataProvider,
			IProxyFactory proxyFactory) {
		this.currMetaData = currMetaData;
		this.baseEntityType = baseEntityType;
		this.propertyPath = propertyPath;
		this.lastPropertyPathTL = lastPropertyPathTL;
		this.propertyInfoProvider = propertyInfoProvider;
		this.entityMetaDataProvider = entityMetaDataProvider;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
			return null;
		}
		if (IPropertyPath.class.equals(method.getDeclaringClass())) {
			return propertyPath.toString();
		}
		String propertyName = propertyInfoProvider.getPropertyNameFor(method);
		if (propertyName == null) {
			return null;
		}
		Member member = currMetaData.getMemberByName(propertyName);
		Class<?> elementType = member.getElementType();
		IEntityMetaData targetMetaData = entityMetaDataProvider.getMetaData(elementType, true);

		Object childPlan;

		if (elementType.isPrimitive()) {
			elementType = Integer.class;
		}
		String propertyPath = this.propertyPath != null ? this.propertyPath + '.' + propertyName
				: propertyName;
		lastPropertyPathTL.set(propertyPath);
		if (elementType.isEnum()) {
			Object[] enumConstants = elementType.getEnumConstants();
			childPlan = enumConstants.length > 0 ? enumConstants[0] : null;
		}
		else if (Number.class.isAssignableFrom(elementType)) {
			childPlan = elementType.getConstructor(String.class).newInstance("0");
		}
		else if (String.class.isAssignableFrom(elementType)) {
			childPlan = propertyPath;
		}
		else {
			childPlan = proxyFactory.createProxy(elementType, INTERFACES,
					new QueryBuilderProxyInterceptor(targetMetaData, baseEntityType, propertyPath,
							lastPropertyPathTL, propertyInfoProvider, entityMetaDataProvider, proxyFactory));
		}
		if (!member.isToMany()) {
			if (Optional.class.isAssignableFrom(member.getRealType())) {
				return Optional.ofNullable(childPlan);
			}
			// to-one relation
			return childPlan;
		}
		Collection<Object> list = ListUtil.createCollectionOfType(member.getRealType(), 1);
		list.add(childPlan);
		return list;
	}
}
