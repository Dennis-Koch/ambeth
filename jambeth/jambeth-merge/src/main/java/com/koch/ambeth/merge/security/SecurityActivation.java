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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;

import java.util.Set;

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
        if (securityActiveTL.get() != null || entityActiveTL.get() != null || serviceActiveTL.get() != null) {
            throw new IllegalStateException("Must be null at this point");
        }
    }

    @Override
    public boolean isSecured() {
        var value = securityActiveTL.get();
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
        var value = securityActiveTL.get();
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
        var value = securityActiveTL.get();
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
    public IStateRollback pushWithoutFiltering() {
        var oldFilterActive = entityActiveTL.get();
        entityActiveTL.set(Boolean.FALSE);
        return () -> entityActiveTL.set(oldFilterActive);
    }

    @Override
    public IStateRollback pushWithoutSecurity() {
        var oldSecurityActive = securityActiveTL.get();
        securityActiveTL.set(Boolean.FALSE);
        return () -> securityActiveTL.set(oldSecurityActive);
    }

    @Override
    public IStateRollback pushWithSecurityDirective(Set<SecurityDirective> securityDirective) {
        return StateRollback.chain(chain -> {
            var securityActive = securityDirective.contains(SecurityDirective.DISABLE_SECURITY) ? Boolean.FALSE : securityDirective.contains(SecurityDirective.ENABLE_SECURITY) ? Boolean.TRUE : null;
            if (securityActive != null) {
                var oldSecurityActive = securityActiveTL.get();
                securityActiveTL.set(securityActive);
                chain.append(() -> securityActiveTL.set(oldSecurityActive));
            }
            var entityActive =
                    securityDirective.contains(SecurityDirective.DISABLE_ENTITY_CHECK) ? Boolean.FALSE : securityDirective.contains(SecurityDirective.ENABLE_ENTITY_CHECK) ? Boolean.TRUE : null;
            if (entityActive != null) {
                var oldEntityActive = entityActiveTL.get();
                entityActiveTL.set(entityActive);
                chain.append(() -> entityActiveTL.set(oldEntityActive));
            }
            var serviceActive =
                    securityDirective.contains(SecurityDirective.DISABLE_SERVICE_CHECK) ? Boolean.FALSE : securityDirective.contains(SecurityDirective.ENABLE_SERVICE_CHECK) ? Boolean.TRUE : null;
            if (serviceActive != null) {
                var oldServiceActive = serviceActiveTL.get();
                serviceActiveTL.set(serviceActive);
                chain.append(() -> serviceActiveTL.set(oldServiceActive));
            }
        });
    }
}
