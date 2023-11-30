package com.koch.ambeth.service.remote;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.service.IServiceByNameProvider;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class RemoteServiceContextImpl implements IInMemoryClientServiceResolver.RemoteServiceContext {

    final IServiceContext beanContext;

    @Override
    public Object getRemoteSourceIdentifier() {
        return beanContext.toString();
    }

    @SneakyThrows
    @Override
    public Object getServiceByName(String serviceName) {
        if ("EventService".equals(serviceName)) {
            var eventServiceType = beanContext.getClass().getClassLoader().loadClass("com.koch.ambeth.event.service.IEventService");
            return beanContext.getService(eventServiceType);
        }
        return beanContext.getService(IServiceByNameProvider.class).getService(serviceName);
    }

    @Override
    public void cleanupThreadLocal() {
        beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
    }
}
