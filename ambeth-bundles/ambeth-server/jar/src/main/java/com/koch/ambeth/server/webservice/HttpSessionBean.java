package com.koch.ambeth.server.webservice;

/*-
 * #%L
 * jambeth-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.proxy.MethodInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.state.IStateRollback;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;

public class HttpSessionBean implements IFactoryBean, MethodInterceptor, IHttpSessionProvider, IThreadLocalCleanupBean {
    @Forkable
    protected final ThreadLocal<Object[]> httpSessionStackTL = new ThreadLocal<>();
    @Autowired
    protected IProxyFactory proxyFactory;
    protected Object obj;

    @Override
    public void cleanupThreadLocal() {
        // intended blank
    }

    @Override
    public Object getObject() throws Exception {
        if (obj != null) {
            return obj;
        }
        obj = proxyFactory.createProxy(HttpSession.class, new Class<?>[] { IHttpSessionProvider.class }, this);
        return obj;
    }

    @Override
    public HttpSession getCurrentHttpSession() {
        Object[] entry = httpSessionStackTL.get();
        if (entry != null) {
            return (HttpSession) entry[0];
        }
        return null;
    }

    @Override
    public HttpServletRequest getCurrentHttpRequest() {
        Object[] entry = httpSessionStackTL.get();
        if (entry != null) {
            return (HttpServletRequest) entry[1];
        }
        return null;
    }

    @Override
    public IStateRollback pushCurrentHttpSession(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        var oldEntry = httpSessionStackTL.get();
        httpSessionStackTL.set(new Object[] { httpSession, httpServletRequest });
        return () -> httpSessionStackTL.set(oldEntry);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
            return null;
        }
        if (IHttpSessionProvider.class.isAssignableFrom(method.getDeclaringClass())) {
            return proxy.invoke(this, args);
        }
        var httpSession = getCurrentHttpSession();
        if (httpSession == null) {
            throw new IllegalStateException("No http session bound to this thread");
        }
        return proxy.invoke(httpSession, args);
    }
}
