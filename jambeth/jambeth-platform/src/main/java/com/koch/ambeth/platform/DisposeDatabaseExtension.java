package com.koch.ambeth.platform;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;

public class DisposeDatabaseExtension {
	public void disposeDatabase(IServiceContext beanContext) {
		IDatabaseProvider databaseProvider =
				beanContext.getService("databaseProvider", IDatabaseProvider.class, false);
		IDatabase database = databaseProvider != null ? databaseProvider.tryGetInstance() : null;
		if (database != null) {
			database.dispose();
		}
	}
}
