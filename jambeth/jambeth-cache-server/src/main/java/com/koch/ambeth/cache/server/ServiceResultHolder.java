package com.koch.ambeth.cache.server;

/*-
 * #%L
 * jambeth-cache-server
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

import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class ServiceResultHolder implements IServiceResultHolder, IThreadLocalCleanupBean {
	public static class ServiceResultHolderItem {
		public boolean expectORIResult;

		public IServiceResult serviceResult;
	}

	protected final ThreadLocal<ServiceResultHolderItem> valueTL = new SensitiveThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		valueTL.remove();
	}

	@Override
	public boolean isExpectServiceResult() {
		ServiceResultHolderItem item = valueTL.get();
		if (item == null) {
			return false;
		}
		return item.expectORIResult;
	}

	@Override
	public void setExpectServiceResult(boolean expectServiceResult) {
		ServiceResultHolderItem item = valueTL.get();
		if (item == null) {
			if (!expectServiceResult) {
				return;
			}
			item = new ServiceResultHolderItem();
			valueTL.set(item);
		}
		item.serviceResult = null;
		item.expectORIResult = expectServiceResult;
	}

	@Override
	public IServiceResult getServiceResult() {
		ServiceResultHolderItem item = valueTL.get();
		if (item == null) {
			return null;
		}
		return item.serviceResult;
	}

	@Override
	public void setServiceResult(IServiceResult serviceResult) {
		ServiceResultHolderItem item = valueTL.get();
		if (item == null) {
			item = new ServiceResultHolderItem();
			valueTL.set(item);
		}
		item.serviceResult = serviceResult;
	}

	@Override
	public void clearResult() {
		ServiceResultHolderItem item = valueTL.get();
		if (item == null) {
			return;
		}
		item.expectORIResult = false;
		item.serviceResult = null;
	}
}
