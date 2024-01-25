package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.util.IDisposable;
import lombok.Setter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

public class ContextCloseListener implements InitializingBean, DisposableBean {

    @Setter
    IDisposable disposable;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(disposable, "disposable must be valid");
    }

    @Override
    public void destroy() throws Exception {
        disposable.dispose();
    }
}
