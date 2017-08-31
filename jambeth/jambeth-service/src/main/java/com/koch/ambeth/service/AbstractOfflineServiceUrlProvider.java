package com.koch.ambeth.service;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

public abstract class AbstractOfflineServiceUrlProvider
		implements IServiceUrlProvider, IOfflineListenerExtendable, IInitializingBean {
	protected boolean isOffline;

	@Override
	public boolean isOffline() {
		return isOffline;
	}

	@Override
	@Property(name = ServiceConfigurationConstants.OfflineMode, defaultValue = "false")
	public void setOffline(boolean isOffline) {
		if (this.isOffline == isOffline) {
			return;
		}
		this.isOffline = isOffline;

		isOfflineChanged();
	}

	protected final IExtendableContainer<IOfflineListener> offlineListeners =
			new DefaultExtendableContainer<>(IOfflineListener.class, "offlineListener");

	@Override
	public void afterPropertiesSet() {
		// Intended blank
	}

	@Override
	public void lockForRestart(boolean offlineAfterRestart) {
		IOfflineListener[] listeners = offlineListeners.getExtensionsShared();

		for (IOfflineListener offlineListener : listeners) {
			if (offlineAfterRestart) {
				offlineListener.beginOffline();
			}
			else {
				offlineListener.beginOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners) {
			if (offlineAfterRestart) {
				offlineListener.handleOffline();
			}
			else {
				offlineListener.handleOnline();
			}
		}
	}

	protected void isOfflineChanged() {
		IOfflineListener[] listeners = offlineListeners.getExtensionsShared();

		for (IOfflineListener offlineListener : listeners) {
			if (isOffline) {
				offlineListener.beginOffline();
			}
			else {
				offlineListener.beginOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners) {
			if (isOffline) {
				offlineListener.handleOffline();
			}
			else {
				offlineListener.handleOnline();
			}
		}
		for (IOfflineListener offlineListener : listeners) {
			if (isOffline) {
				offlineListener.endOffline();
			}
			else {
				offlineListener.endOnline();
			}
		}
	}

	@Override
	public abstract String getServiceURL(Class<?> serviceInterface, String serviceName);

	@Override
	public void addOfflineListener(IOfflineListener offlineListener) {
		offlineListeners.register(offlineListener);
	}

	@Override
	public void removeOfflineListener(IOfflineListener offlineListener) {
		offlineListeners.unregister(offlineListener);
	}
}
