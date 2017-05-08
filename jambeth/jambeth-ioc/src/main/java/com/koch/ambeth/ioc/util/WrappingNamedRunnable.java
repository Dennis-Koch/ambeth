package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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


public class WrappingNamedRunnable implements INamedRunnable {
	protected final Runnable runnable;

	protected final String name;

	public WrappingNamedRunnable(Runnable runnable, String name) {
		this.runnable = runnable;
		this.name = name;
	}

	@Override
	public void run() {
		runnable.run();
	}

	@Override
	public String getName() {
		return name;
	}
}
