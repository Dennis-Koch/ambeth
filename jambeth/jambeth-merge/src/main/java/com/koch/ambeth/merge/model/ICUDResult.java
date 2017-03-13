package com.koch.ambeth.merge.model;

import java.util.List;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface ICUDResult
{
	List<IChangeContainer> getAllChanges();

	List<IChangeContainer> getChanges(Class<?> type);

	List<Object> getOriginalRefs();
}
