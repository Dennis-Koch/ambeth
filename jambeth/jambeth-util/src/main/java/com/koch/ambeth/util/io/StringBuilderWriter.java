package com.koch.ambeth.util.io;

import java.io.IOException;
import java.io.Writer;

public class StringBuilderWriter extends Writer {
	private final StringBuilder sb;

	private StringBuilderWriter(StringBuilder sb) {
		this.sb = sb;
	}

	@Override
	public void write(int c) throws IOException {
		sb.append(c);
	}

	@Override
	public void write(String str) throws IOException {
		sb.append(str);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		sb.append(cbuf, off, len);
	}

	@Override
	public Writer append(char c) throws IOException {
		sb.append(c);
		return this;
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		sb.append(csq);
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		sb.append(csq, start, end);
		return this;
	}

	@Override
	public void flush() throws IOException {
		// intended blank
	}

	@Override
	public void close() throws IOException {
		// intended blank
	}
}
