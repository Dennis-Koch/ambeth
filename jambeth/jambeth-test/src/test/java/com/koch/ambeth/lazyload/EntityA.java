package com.koch.ambeth.lazyload;

import java.util.List;

import jakarta.persistence.Embedded;

import com.koch.ambeth.util.annotation.PropertyChangeAspect;

@PropertyChangeAspect(includeNewValue = true, includeOldValue = true)
public interface EntityA {
    int getId();

    int getVersion();

    @Embedded
    EmbeddedA getEmbeddedA();

    List<EntityC> getEntityCs();
}
