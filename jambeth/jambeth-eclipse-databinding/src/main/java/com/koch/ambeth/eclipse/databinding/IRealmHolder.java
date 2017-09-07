package com.koch.ambeth.eclipse.databinding;

import java.beans.Introspector;

import org.eclipse.core.databinding.observable.Realm;

import com.koch.ambeth.util.model.INotifyPropertyChanged;

public interface IRealmHolder extends INotifyPropertyChanged {
	String P_REALM = "Realm";

	String BEANS_REALM = Introspector.decapitalize(P_REALM);

	Realm getRealm();

	void setRealm(Realm realm);
}
