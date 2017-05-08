package com.koch.ambeth.shell.core.resulttype;

/*-
 * #%L
 * jambeth-shell
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.ArrayList;

public class ListResult<T> extends CommandResult {

	private java.util.List<T> list;

	/**
	 * add a record to the instance
	 *
	 * @param record instance of class that extends {@link CommandResult}
	 */
	public void addRecord(T record) {
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(record);
	}

	/**
	 * remove a record from the instance
	 *
	 * @param record instance of class that extends {@link CommandResult}
	 */
	public void removeRecord(T record) {
		if (list != null) {
			list.remove(record);
		}
	}

	/**
	 * get all the records from the instance
	 * <p>
	 * the result is a instance of java.util.List, which contains instances of class that extends
	 * {@link CommandResult}.
	 * </p>
	 * <p>
	 * the {@link CommandResult} instances can be mixture of all its subClass.
	 * </p>
	 *
	 * @return {@link ListResult}
	 */
	public java.util.List<T> getAllRecords() {
		return list;
	}

	@Override
	public String toString() {
		if (list == null) {
			return "";
		}
		StringBuffer strBuf = new StringBuffer();
		for (T commandResult : list) {
			strBuf.append(commandResult.toString()).append(System.lineSeparator());
		}
		return strBuf.toString();
	}
}
