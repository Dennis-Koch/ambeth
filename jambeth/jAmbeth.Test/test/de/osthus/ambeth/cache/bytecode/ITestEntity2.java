package de.osthus.ambeth.cache.bytecode;

import java.util.List;

public interface ITestEntity2
{
	int getId();

	void setId(int id);

	int getVersion();

	void setVersion(int version);

	List<ITestEntity2> getChildrenWithProtectedField();

	void setChildrenWithProtectedField(List<ITestEntity2> childrenWithProtectedField);
}
