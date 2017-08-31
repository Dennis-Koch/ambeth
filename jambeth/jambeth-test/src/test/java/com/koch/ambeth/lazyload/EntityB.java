package com.koch.ambeth.lazyload;

import com.koch.ambeth.util.annotation.PropertyChangeAspect;

@PropertyChangeAspect(includeNewValue = true, includeOldValue = true)
public interface EntityB {
	int getId();

	int getVersion();
}
