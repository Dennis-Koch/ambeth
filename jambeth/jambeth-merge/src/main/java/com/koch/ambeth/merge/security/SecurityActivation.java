package com.koch.ambeth.merge.security;

/*-
 * #%L
 * jambeth-merge
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

import java.util.Set;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class SecurityActivation implements ISecurityActivation, IThreadLocalCleanupBean {
	@Forkable
	protected final ThreadLocal<Boolean> serviceActiveTL = new ThreadLocal<>();

	@Forkable
	protected final ThreadLocal<Boolean> securityActiveTL = new ThreadLocal<>();

	@Forkable
	protected final ThreadLocal<Boolean> entityActiveTL = new ThreadLocal<>();

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public void cleanupThreadLocal() {
		if (securityActiveTL.get() != null || entityActiveTL.get() != null
				|| serviceActiveTL.get() != null) {
			throw new IllegalStateException("Must be null at this point");
		}
	}

	@Override
	public boolean isSecured() {
		Boolean value = securityActiveTL.get();
		if (value == null) {
			return securityActive;
		}
		return value.booleanValue();
	}

	@Override
	public boolean isFilterActivated() {
		return isEntitySecurityEnabled();
	}

	public boolean isEntitySecurityEnabled() {
		if (!securityActive) {
			return false;
		}
		Boolean value = securityActiveTL.get();
		if (Boolean.FALSE.equals(value)) {
			return false;
		}
		value = entityActiveTL.get();
		if (value != null) {
			return value.booleanValue();
		}
		return true;
	}

	@Override
	public boolean isServiceSecurityEnabled() {
		if (!securityActive) {
			return false;
		}
		Boolean value = securityActiveTL.get();
		if (Boolean.FALSE.equals(value)) {
			return false;
		}
		value = serviceActiveTL.get();
		if (value != null) {
			return value.booleanValue();
		}
		return true;
	}

	public boolean isServiceOrEntitySecurityEnabled() {
		return isEntitySecurityEnabled() || isServiceSecurityEnabled();
	}

	@Override
	public <R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable)
			throws Exception {
		Boolean oldSecurityActive = securityActiveTL.get();
		securityActiveTL.set(Boolean.FALSE);
		try {
			return pausedSecurityRunnable.invoke();
		}
		finally {
			securityActiveTL.set(oldSecurityActive);
		}
	}

	@Override
	public IStateRollback pushWithoutFiltering(IStateRollback... rollbacks) {
		final Boolean oldFilterActive = entityActiveTL.get();
		entityActiveTL.set(Boolean.FALSE);
		try {
			return new AbstractStateRollback(rollbacks) {
				@Override
				protected void rollbackIntern() throws Exception {
					entityActiveTL.set(oldFilterActive);
				}
			};
		}
		finally {
			entityActiveTL.set(oldFilterActive);
		}
	}

	@Override
	public IStateRollback pushWithoutSecurity(IStateRollback... rollbacks) {
		final Boolean oldSecurityActive = securityActiveTL.get();
		securityActiveTL.set(Boolean.FALSE);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				securityActiveTL.set(oldSecurityActive);
			}
		};
	}

	@Override
	public IStateRollback pushWithSecurityDirective(Set<SecurityDirective> securityDirective,
			IStateRollback... rollbacks) {
		final Boolean securityActive = securityDirective.contains(SecurityDirective.DISABLE_SECURITY)
				? Boolean.FALSE
				: securityDirective.contains(SecurityDirective.ENABLE_SECURITY) ? Boolean.TRUE : null;
		final Boolean entityActive = securityDirective.contains(SecurityDirective.DISABLE_ENTITY_CHECK)
				? Boolean.FALSE
				: securityDirective.contains(SecurityDirective.ENABLE_ENTITY_CHECK) ? Boolean.TRUE : null;
		final Boolean serviceActive = securityDirective
				.contains(SecurityDirective.DISABLE_SERVICE_CHECK) ? Boolean.FALSE
						: securityDirective.contains(SecurityDirective.ENABLE_SERVICE_CHECK) ? Boolean.TRUE
								: null;

		boolean success = false;

		Boolean oldSecurityActive = null, oldEntityActive = null, oldServiceActive = null;
		if (securityActive != null) {
			oldSecurityActive = securityActiveTL.get();
			securityActiveTL.set(securityActive);
		}
		try {
			if (entityActive != null) {
				oldEntityActive = entityActiveTL.get();
				entityActiveTL.set(entityActive);
			}
			try {
				if (serviceActive != null) {
					oldServiceActive = serviceActiveTL.get();
					serviceActiveTL.set(serviceActive);
				}
				try {
					final Boolean fOldSecurityActive = oldSecurityActive;
					final Boolean fOldEntityActive = oldEntityActive;
					final Boolean fOldServiceActive = oldServiceActive;
					IStateRollback rollback = new AbstractStateRollback(rollbacks) {
						@Override
						protected void rollbackIntern() throws Exception {
							if (serviceActive != null) {
								serviceActiveTL.set(fOldServiceActive);
							}
							if (entityActive != null) {
								entityActiveTL.set(fOldEntityActive);
							}
							if (securityActive != null) {
								securityActiveTL.set(fOldSecurityActive);
							}
						}
					};
					success = true;
					return rollback;
				}
				finally {
					if (!success && serviceActive != null) {
						serviceActiveTL.set(oldServiceActive);
					}
				}
			}
			finally {
				if (!success && entityActive != null) {
					entityActiveTL.set(oldEntityActive);
				}
			}
		}
		finally {
			if (!success && securityActive != null) {
				securityActiveTL.set(oldSecurityActive);
			}
		}
	}
}
