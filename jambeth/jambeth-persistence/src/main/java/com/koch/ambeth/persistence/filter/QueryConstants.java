package com.koch.ambeth.persistence.filter;

/*-
 * #%L
 * jambeth-persistence
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

public final class QueryConstants
{
	public static final Object USE_TABLE_ALIAS = new Object();

	public static final Object PAGING_INDEX_OBJECT = new Object();

	public static final Object PAGING_SIZE_OBJECT = new Object();

	public static final Object FIRST_ORDER_BY_STATE = new Object();

	public static final Object ADDITIONAL_SELECT_SQL_SB = new Object();

	public static final Object PRE_VALUE_KEY = new Object();

	public static final Object POST_VALUE_KEY = new Object();

	public static final Object REMAINING_LEFT_OPERAND_HANDLE = new Object();

	public static final Object REMAINING_RIGHT_OPERAND_HANDLE = new Object();

	public static final Object CONSUME_RIGHT_OPERAND_HANDLE = new Object();

	public static final Object EXPECTED_TYPE_HINT = new Object();

	public static final String LIMIT_VALUE = "#limitValue";

	private QueryConstants()
	{
		// Intended blank
	}
}
