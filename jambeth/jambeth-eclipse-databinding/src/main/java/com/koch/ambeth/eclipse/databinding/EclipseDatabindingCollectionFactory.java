package com.koch.ambeth.eclipse.databinding;

import java.util.ArrayList;
import java.util.HashSet;
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public List<?> createList(Class<?> expectedType, Class<?> elementType) {
		Realm realm = realmHolder.getRealm();
		if (realm == null) {
			return null;
		}
		return new WritableList(realm, new ArrayList<>(), elementType);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Set<?> createSet(Class<?> expectedType, Class<?> elementType) {
		Realm realm = realmHolder.getRealm();
		if (realm == null) {
			return null;
		}
		return new WritableSet(realm, new HashSet<>(), elementType);
	}
}
