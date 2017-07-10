package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.persistence.TableMetaData;
import com.koch.ambeth.util.ParamChecker;

public class SqlTableMetaData extends TableMetaData {
	protected String fullqualifiedEscapedName;

	protected Object initialVersion;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(initialVersion, "initialVersion");
		ParamChecker.assertNotNull(fullqualifiedEscapedName, "fullqualifiedEscapedName");
	}

	@Override
	public Object getInitialVersion() {
		return initialVersion;
	}

	@Override
	public void setInitialVersion(Object initialVersion) {
		this.initialVersion = initialVersion;
	}

	@Override
	public String getFullqualifiedEscapedName() {
		return fullqualifiedEscapedName;
	}

	public void setFullqualifiedEscapedName(String fullqualifiedEscapedName) {
		this.fullqualifiedEscapedName = fullqualifiedEscapedName;
	}
}
