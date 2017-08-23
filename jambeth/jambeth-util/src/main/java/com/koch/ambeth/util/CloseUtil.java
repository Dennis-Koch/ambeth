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

	public static void close(IDisposable disposable) {
		if (disposable == null) {
			return;
		}
		disposable.dispose();
	}

	private CloseUtil() {
		// Intended blank
	}
}
