package com.koch.ambeth.persistence.api;

import com.koch.ambeth.util.IDisposable;

public interface ICursor extends IDisposable
{
	IFieldMetaData[] getFields();

	IFieldMetaData getFieldByMemberName(String memberName);

	int getFieldIndexByMemberName(String memberName);

	int getFieldIndexByName(String fieldName);

	boolean moveNext();

	ICursorItem getCurrent();
}
