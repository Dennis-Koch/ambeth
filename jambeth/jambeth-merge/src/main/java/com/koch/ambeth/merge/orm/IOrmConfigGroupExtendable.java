package com.koch.ambeth.merge.orm;

public interface IOrmConfigGroupExtendable {
	void registerOrmConfigGroup(IOrmConfigGroup ormConfigGroup);

	void unregisterOrmConfigGroup(IOrmConfigGroup ormConfigGroup);
}
