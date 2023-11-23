package com.koch.ambeth.util.proxy;

import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.TypeUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class ProxyFactory extends SmartCopyMap<ProxyTypeKey, Class<? extends Factory>> implements IProxyFactory {
    protected static final Callback[] emptyCallbacks = new Callback[] { NoOp.INSTANCE };

    protected IClassLoaderProvider classLoaderProvider;

    public void setClassLoaderProvider(IClassLoaderProvider classLoaderProvider) {
        this.classLoaderProvider = classLoaderProvider;
    }

    @SneakyThrows
    protected Object createProxyIntern(Class<? extends Factory> proxyType, Callback[] callbacks) {
        var proxy = proxyType.getConstructor().newInstance();
        proxy.setCallbacks(callbacks);
        return proxy;
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public <T> T createProxy(Class<T> type, MethodInterceptor... interceptors) {
        var key = new ProxyTypeKey(type, TypeUtil.EMPTY_TYPES);
        var proxyType = get(key);
        if (proxyType != null) {
            return (T) createProxyIntern(proxyType, interceptors);
        }
        DynamicType.Unloaded unloadedType;
        if (type.isInterface()) {
            unloadedType = (DynamicType.Unloaded) FactoryMixin.weave(
                    new ByteBuddy().subclass(Object.class).implement(type).method(ElementMatchers.any()).intercept(MethodDelegation.to(MethodInterceptorMixin.class))).make();
        } else {
            unloadedType = (DynamicType.Unloaded) FactoryMixin.weave(new ByteBuddy().subclass(type).method(ElementMatchers.any()).intercept(MethodDelegation.to(MethodInterceptorMixin.class))).make();
        }
        proxyType = unloadedType.load(classLoaderProvider.getClassLoader()).getLoaded();
        var callbacks = emptyCallbacks;
        if (interceptors.length > 0) {
            callbacks = interceptors;
        }
        var proxy = createProxyIntern(proxyType, callbacks);
        put(key, proxyType);
        return (T) proxy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createProxy(Class<T> type, Class<?>[] interfaces, MethodInterceptor... interceptors) {
        var key = new ProxyTypeKey(type, interfaces);
        var proxyType = get(key);
        if (proxyType != null) {
            return (T) createProxyIntern(proxyType, interceptors);
        }
        if (type.isInterface()) {
            var allInterfaces = LinkedHashSet.<Class<?>>create(interfaces.length + 1);
            allInterfaces.add(type);
            allInterfaces.addAll(interfaces);

            return (T) createProxy(allInterfaces.toArray(Class.class), interceptors);
        }
        var tempList = new ArrayList<Class<?>>();
        for (int a = interfaces.length; a-- > 0; ) {
            var potentialNewInterfaces = interfaces[a];
            if (!potentialNewInterfaces.isInterface()) {
                continue;
            }
            for (int b = tempList.size(); b-- > 0; ) {
                var existingInterface = tempList.get(b);
                if (existingInterface.isAssignableFrom(potentialNewInterfaces)) {
                    tempList.set(b, potentialNewInterfaces);
                    potentialNewInterfaces = null;
                    break;
                }
            }
            if (potentialNewInterfaces != null) {
                tempList.add(potentialNewInterfaces);
            }
        }
        var interfaceArray = tempList != null ? tempList.toArray(new Class<?>[tempList.size()]) : interfaces;
        var unloadedType = (DynamicType.Unloaded) FactoryMixin.weave(new ByteBuddy().subclass(type).implement(interfaceArray)).make();
        proxyType = unloadedType.load(classLoaderProvider.getClassLoader()).getLoaded();
        var proxy = createProxyIntern(proxyType, interceptors);
        put(key, proxyType);
        return (T) proxy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object createProxy(Class<?>[] interfaces, MethodInterceptor... interceptors) {
        var key = new ProxyTypeKey(Object.class, interfaces);
        var proxyType = get(key);
        if (proxyType != null) {
            return createProxyIntern(proxyType, interceptors);
        }
        for (int a = 0, size = interfaces.length; a < size; a++) {
            Class<?> interfaceType = interfaces[a];
            if (!interfaceType.isInterface()) {
                Class<?>[] newInterfaces = new Class<?>[interfaces.length - 1];
                System.arraycopy(interfaces, 0, newInterfaces, 0, a);
                if (interfaces.length - a > 1) {
                    System.arraycopy(interfaces, a + 1, newInterfaces, a, interfaces.length - a - 1);
                }
                return createProxy(interfaceType, newInterfaces, interceptors);
            }
        }
        var unloadedType = (DynamicType.Unloaded) FactoryMixin.weave(
                new ByteBuddy().subclass(Object.class).implement(interfaces).method(ElementMatchers.any()).intercept(MethodDelegation.to(MethodInterceptorMixin.class))).make();
        proxyType = unloadedType.load(classLoaderProvider.getClassLoader()).getLoaded();
        var proxy = createProxyIntern(proxyType, interceptors);
        put(key, proxyType);
        return proxy;

        //		Enhancer enhancer = new Enhancer();
        //		enhancer.setClassLoader(classLoaderProvider.getClassLoader());
        //		enhancer.setInterfaces(interfaces);
        //		enhancer.setCallbacks(interceptors);
        //
        //		Object proxy;
        //		try {
        //			proxy = enhancer.create();
        //		}
        //		catch (RuntimeException | Error e) {
        //			if (interceptors.length != 1) {
        //				throw e;
        //			}
        //			final MethodInterceptor interceptor = interceptors[0];
        //			if (interceptor instanceof InvocationHandler) {
        //				return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces,
        //						(InvocationHandler) interceptor);
        //			}
        //			return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces,
        //					new InvocationHandler() {
        //						@Override
        //						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //							return interceptor.intercept(proxy, method, args, null);
        //						}
        //					});
        //		}
        //		put(key, (Class<? extends Factory>) proxy.getClass());
    }

    @Override
    public ICascadedInterceptor wrap(Object target, ICascadedInterceptor interceptor) {
        interceptor.setTarget(target);
        return interceptor;
    }
}
