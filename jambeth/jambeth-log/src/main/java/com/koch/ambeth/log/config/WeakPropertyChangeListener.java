package com.koch.ambeth.log.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakPropertyChangeListener extends WeakReference<PropertyChangeListener>
		implements PropertyChangeListener {
	public WeakPropertyChangeListener(PropertyChangeListener referent,
			ReferenceQueue<? super PropertyChangeListener> q) {
		super(referent, q);
	}

	public WeakPropertyChangeListener(PropertyChangeListener referent) {
		super(referent);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		PropertyChangeListener target = get();
		if (target == null) {
			Properties sourceProps = (Properties) evt.getSource();
			sourceProps.removePropertyChangeListener(this);
			return;
		}
		target.propertyChange(evt);
	}
}
