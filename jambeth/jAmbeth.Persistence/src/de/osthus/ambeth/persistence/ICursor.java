package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface ICursor extends IDisposable
{
	IField[] getFields();

	IField getFieldByMemberName(String memberName);

	int getFieldIndexByMemberName(String memberName);

	int getFieldIndexByName(String fieldName);

	boolean moveNext();

	ICursorItem getCurrent();
}
