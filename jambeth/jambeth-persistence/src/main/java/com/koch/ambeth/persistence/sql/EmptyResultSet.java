package com.koch.ambeth.persistence.sql;

import java.util.Iterator;

public class EmptyResultSet implements IResultSet, Iterator<Object[]> {
	public static final IResultSet instance = new EmptyResultSet();

	@Override
	public void dispose() {
		// Intended blank
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Object[] next() {
		return null;
	}

	@Override
	public Iterator<Object[]> iterator() {
		return this;
	}
}
