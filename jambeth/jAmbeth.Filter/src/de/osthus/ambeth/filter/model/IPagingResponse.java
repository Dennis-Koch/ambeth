package de.osthus.ambeth.filter.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlType
public interface IPagingResponse<T>
{
	int getSize();

	int getTotalSize();

	int getNumber();

	int getTotalNumber();

	List<IObjRef> getRefResult();

	void setRefResult(List<IObjRef> refResult);

	List<T> getResult();

	void setResult(List<T> result);
}