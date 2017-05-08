package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

public final class JdbcUtil {
	private JdbcUtil() {
		// Intended blank
	}

	public static void close(Statement stm, ResultSet rs) {
		close(rs);
		close(stm);
	}

	public static void close(ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		try {
			resultSet.close();
		}
		catch (Throwable e) {
			// Intended blank
		}
	}

	public static void close(Statement stm) {
		if (stm == null) {
			return;
		}
		try {
			stm.close();
		}
		catch (Throwable e) {
			// Intended blank
		}
	}

	public static void close(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			if (!connection.isClosed()) {
				connection.close();
			}
		}
		catch (Throwable e) {
			// Intended blank
		}
	}

	public static void close(Array array) {
		if (array == null) {
			return;
		}
		try {
			array.free();
		}
		catch (Throwable e) {
			// Intended blank
		}
	}

	public static Class<?> getJavaTypeFromJdbcType(int jdbcTypeIndex, int scaleHint, int digitsHint) {
		switch (jdbcTypeIndex) {
			case Types.BOOLEAN:
			case Types.BIT: {
				return Boolean.class;
			}
			case Types.DISTINCT: {
				return Object.class;
			}
			case Types.CHAR:
			case Types.VARCHAR: {
				return String.class;
			}
			case Types.DATE: {
				return java.sql.Date.class;
			}
			case Types.TIME: {
				return Time.class;
			}
			case Types.TIMESTAMP: {
				return Timestamp.class;
			}
			case Types.DOUBLE:
			case Types.FLOAT: // Float is intentionally a DOUBLE here. That's true since SQL92
			{
				return Double.class;
			}
			case Types.BIGINT: {
				return Long.class;
			}
			case Types.INTEGER: {
				return Integer.class;
			}
			case Types.NUMERIC:
			case Types.DECIMAL: {
				if (digitsHint <= 0) {
					if (scaleHint > 0) {
						if (scaleHint <= 2) {
							// MaxValue = 127, 2 full digits
							return Byte.class;
						}
						else if (scaleHint <= 4) {
							// MaxValue = 32767, 4 full digits
							return Short.class;
						}
						else if (scaleHint <= 9) {
							// MaxValue = 2147483647, 9 full digits
							return Integer.class;
						}
						else if (scaleHint <= 18) {
							// MaxValue = 9223372036854775807, 18 full digits
							return Long.class;
						}
					}
					// Anything else without fractional precision. But some databases do not support
					// BigInteger
					// return BigInteger.class;
					return BigDecimal.class;
				}
				else if (scaleHint <= 4 && digitsHint <= 4) {
					return Float.class;
				}
				else if (scaleHint <= 9 && digitsHint <= 9) {
					return Double.class;
				}
				return BigDecimal.class;
			}
			case Types.SMALLINT: {
				return Short.class;
			}
			case Types.TINYINT: {
				return Byte.class;
			}
			case Types.REAL: {
				return Float.class;
			}
			case Types.BLOB:
			case Types.BINARY: {
				return Blob.class;
			}
			case Types.CLOB: {
				return Clob.class;
			}
			case Types.ROWID: {
				return RowId.class;
			}
			case Types.ARRAY:
			case Types.OTHER: {
				return Array.class;
			}
			default:
				throw new UnsupportedOperationException(
						"Type index " + jdbcTypeIndex + " from " + Types.class.getName() + " not supported");
		}
	}
}
