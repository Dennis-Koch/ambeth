package com.koch.ambeth.lazyload;

import com.koch.ambeth.util.annotation.PropertyChangeAspect;

@PropertyChangeAspect(includeNewValue = true, includeOldValue = true)
public interface EntityC {
	int getId();

	int getVersion();
}
