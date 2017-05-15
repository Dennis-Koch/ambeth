package com.koch.ambeth.util.io;

import java.io.ByteArrayOutputStream;

public class FastByteArrayOutputStream extends ByteArrayOutputStream {

	public FastByteArrayOutputStream() {
		super();
	}

	public FastByteArrayOutputStream(int size) {
		super(size);
	}

	public byte[] getRawByteArray() {
		return buf;
	}
}
