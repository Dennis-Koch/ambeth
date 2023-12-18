package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.util.state.IStateRollback;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpringLinkManager implements DisposableBean, ApplicationListener {

    protected final AtomicInteger linkCounter = new AtomicInteger();

    protected final List<ILinkContainer> links = new ArrayList<>();

    protected final List<IStateRollback> rollbacks = new ArrayList<>();

    @Override
    public void destroy() throws Exception {
        synchronized (rollbacks) {
            for (int a = rollbacks.size(); a-- > 0; ) {
                rollbacks.get(a).rollback();
            }
            rollbacks.clear();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            synchronized (links) {
                for (var link : links) {
                    link.link();
                    rollbacks.add(() -> link.unlink());
                }
            }
        }
        if (event instanceof ContextClosedEvent) {
            synchronized (links) {
                for (var link : rollbacks) {
                    link.rollback();
                }
            }
        }
    }

    public IStateRollback registerLink(ILinkContainer linkContainer) {
        if (!linkContainer.link()) {
            throw new IllegalStateException("Link expected to work at this point");
        }
        IStateRollback rollback = () -> linkContainer.unlink();
        synchronized (rollbacks) {
            rollbacks.add(rollback);
        }
        return rollback;
    }
}
