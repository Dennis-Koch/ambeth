package com.koch.ambeth.eclipse.databinding;

import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.Realm;

import com.koch.ambeth.eclipse.databinding.config.EclipseDatabindingConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;

public class RealmHolder implements IInitializingBean, IRealmHolder {
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport();

	@Autowired(optional = true)
	protected Realm realm;

	@Property(name = EclipseDatabindingConfigurationConstants.Realm, mandatory = false)
	protected Object realmProperty;

	@Override
	public void afterPropertiesSet() throws Throwable {
		Realm realm = getRealm();
		if (realm == null && realmProperty instanceof Realm) {
			setRealm((Realm) realmProperty);
		}
	}

	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
	public void setRealm(Realm realm) {
		if (this.realm == realm) {
			return;
		}
		Realm oldRealm = this.realm;
		this.realm = realm;
		pcs.firePropertyChange(this, IRealmHolder.BEANS_REALM, oldRealm, realm);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.add(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.remove(listener);
	}
}
