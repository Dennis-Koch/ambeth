package com.koch.ambeth.ioc.annotation;

/*-
 * #%L
 * jambeth-ioc-test
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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;

public class AutowiredTestBean
{
	@Autowired
	private IServiceContext beanContextPrivate;

	@Autowired
	protected IServiceContext beanContextProtected;

	@Autowired
	public IServiceContext beanContextPublic;

	private IServiceContext beanContextPrivateSetter;

	private IServiceContext beanContextProtectedSetter;

	private IServiceContext beanContextPublicSetter;

	private IServiceContext beanContextProtectedSetterAutowired;

	private IServiceContext beanContextPrivateSetterAutowired;

	@SuppressWarnings("unused")
	private void setBeanContextPrivateSetter(IServiceContext beanContextPrivateSetter)
	{
		this.beanContextPrivateSetter = beanContextPrivateSetter;
	}

	@Autowired
	protected void setBeanContextPrivateSetterAutowired(IServiceContext beanContextPrivateSetterAutowired)
	{
		this.beanContextPrivateSetterAutowired = beanContextPrivateSetterAutowired;
	}

	protected void setBeanContextProtectedSetter(IServiceContext beanContextProtectedSetter)
	{
		this.beanContextProtectedSetter = beanContextProtectedSetter;
	}

	@Autowired
	protected void setBeanContextProtectedSetterAutowired(IServiceContext beanContextProtectedSetterAutowired)
	{
		this.beanContextProtectedSetterAutowired = beanContextProtectedSetterAutowired;
	}

	public void setBeanContextPublicSetter(IServiceContext beanContextPublicSetter)
	{
		this.beanContextPublicSetter = beanContextPublicSetter;
	}

	public IServiceContext getBeanContextPrivate()
	{
		return beanContextPrivate;
	}

	public IServiceContext getBeanContextProtected()
	{
		return beanContextProtected;
	}

	public IServiceContext getBeanContextPublic()
	{
		return beanContextPublic;
	}

	public IServiceContext getBeanContextPrivateSetter()
	{
		return beanContextPrivateSetter;
	}

	public IServiceContext getBeanContextPrivateSetterAutowired()
	{
		return beanContextPrivateSetterAutowired;
	}

	public IServiceContext getBeanContextProtectedSetter()
	{
		return beanContextProtectedSetter;
	}

	public IServiceContext getBeanContextProtectedSetterAutowired()
	{
		return beanContextProtectedSetterAutowired;
	}

	public IServiceContext getBeanContextPublicSetter()
	{
		return beanContextPublicSetter;
	}
}
