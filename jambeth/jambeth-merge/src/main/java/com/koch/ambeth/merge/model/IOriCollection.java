package com.koch.ambeth.merge.model;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IOriCollection
{
	List<IObjRef> getAllChangeORIs();

	List<IObjRef> getChangeRefs(Class<?> type);

	Long getChangedOn();

	String getChangedBy();

	Long[] getAllChangedOn();

	String[] getAllChangedBy();
}
