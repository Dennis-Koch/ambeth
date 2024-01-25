package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IServiceLookup;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.ioc.link.LinkContainer;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.Setter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringLinkContainer extends LinkContainer implements InitializingBean, DisposableBean {
    @Setter(onMethod = @__(@Autowired))
    protected SpringLinkManager springLinkManager;

    IStateRollback rollback;

    @Autowired
    @Override
    public void setBeanLookup(IServiceLookup beanLookup) {
        super.setBeanLookup(beanLookup);
    }

    @Autowired
    @Override
    public void setExtendableRegistry(IExtendableRegistry extendableRegistry) {
        super.setExtendableRegistry(extendableRegistry);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        rollback = springLinkManager.registerLink(this);
    }

    @Override
    public void destroy() throws Exception {
        rollback.rollback();
    }
}
