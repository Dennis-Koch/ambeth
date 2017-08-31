package com.koch.ambeth.lazyload;

import javax.persistence.Embeddable;

@Embeddable
public interface EmbeddedA {
	EntityB getEntityB();
}
