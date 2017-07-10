package com.koch.ambeth.query.jdbc.sql;

public class TableAliasHolder implements ITableAliasHolder {
	private String tableAlias;

	@Override
	public String getTableAlias() {
		return tableAlias;
	}

	@Override
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
}
