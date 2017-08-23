package com.koch.ambeth.ioc.threadlocal;

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

public class ForkStateEntry {
	public static final ForkStateEntry[] EMPTY_ENTRIES = new ForkStateEntry[0];

	public final IThreadLocalCleanupBean tlBean;

	public final String fieldName;

	public final ThreadLocal<?> valueTL;

	public final ForkableType forkableType;

	public final IForkProcessor forkProcessor;

	public ForkStateEntry(IThreadLocalCleanupBean tlBean, String fieldName, ThreadLocal<?> valueTL,
			ForkableType forkableType, IForkProcessor forkProcessor) {
		this.tlBean = tlBean;
		this.fieldName = fieldName;
		this.valueTL = valueTL;
		this.forkableType = forkableType;
		this.forkProcessor = forkProcessor;
	}
}
