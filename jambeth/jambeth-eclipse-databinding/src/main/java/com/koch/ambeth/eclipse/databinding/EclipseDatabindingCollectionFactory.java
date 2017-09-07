package com.koch.ambeth.eclipse.databinding;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.WritableSet;

import com.koch.ambeth.cache.util.IRelationalCollectionFactory;
import com.koch.ambeth.ioc.annotation.Autowired;

public class EclipseDatabindingCollectionFactory implements IRelationalCollectionFactory {
	@Autowired
	protected IRealmHolder realmHolder;

	@Override
	public List<?> createList(Class<?> expectedType, Class<?> elementType) {
		Realm realm = realmHolder.getRealm();
		if (realm == null) {
			return null;
		}
		return new WritableList(realm);
	}

	@Override
	public Set<?> createSet(Class<?> expectedType, Class<?> elementType) {
		Realm realm = realmHolder.getRealm();
		if (realm == null) {
			return null;
		}
		return new WritableSet(realm);
	}
}
