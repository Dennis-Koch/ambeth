package com.koch.ambeth.lazyload;

import jakarta.persistence.Embeddable;

@Embeddable
public interface EmbeddedA {
    EntityB getEntityB();
}
