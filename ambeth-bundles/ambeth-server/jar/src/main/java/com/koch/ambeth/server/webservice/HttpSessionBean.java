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

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class HttpSessionBean
		implements IFactoryBean, MethodInterceptor, IHttpSessionProvider, IThreadLocalCleanupBean {

	public static final String P_CURRENT_HTTP_SESSION = "CurrentHttpSession";

	@Autowired
	protected IProxyFactory proxyFactory;

	@Forkable
	protected final ThreadLocal<HttpSession> httpSessionStackTL = new ThreadLocal<>();

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
		obj = proxyFactory.createProxy(HttpSession.class, new Class<?>[] { IHttpSessionProvider.class },
				this);
		return obj;
	}

	@Override
	public HttpSession getCurrentHttpSession() {
		return httpSessionStackTL.get();
	}

	@Override
	public void setCurrentHttpSession(HttpSession httpSession) {
		httpSessionStackTL.set(httpSession);
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
			return null;
		}
		if (IHttpSessionProvider.class.isAssignableFrom(method.getDeclaringClass())) {
			return proxy.invoke(this, args);
		}
		HttpSession httpSession = getCurrentHttpSession();
		if (httpSession == null) {
			throw new IllegalStateException("No http session bound to this thread");
		}
		return proxy.invoke(httpSession, args);
	}
}
