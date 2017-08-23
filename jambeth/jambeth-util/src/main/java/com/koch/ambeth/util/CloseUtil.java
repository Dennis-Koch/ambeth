package com.koch.ambeth.util;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public final class CloseUtil {
	public static void close(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private CloseUtil() {
		// Intended blank
	}
}
