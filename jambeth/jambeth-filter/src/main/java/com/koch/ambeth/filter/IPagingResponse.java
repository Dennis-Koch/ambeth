package com.koch.ambeth.filter;

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPagingResponse<T> {
	int getSize();

	long getTotalSize();

	int getNumber();

	int getTotalNumber();

	List<IObjRef> getRefResult();

	void setRefResult(List<IObjRef> refResult);

	List<T> getResult();

	void setResult(List<T> result);
}
