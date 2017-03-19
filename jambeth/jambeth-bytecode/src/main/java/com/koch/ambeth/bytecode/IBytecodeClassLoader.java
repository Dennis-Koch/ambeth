package com.koch.ambeth.bytecode;

import java.io.Writer;

import com.koch.ambeth.merge.bytecode.IBytecodePrinter;

public interface IBytecodeClassLoader extends IBytecodePrinter {
	byte[] readTypeAsBinary(Class<?> type, ClassLoader classLoader);

	String getBytecodeTypeName(Class<?> type);

	byte[] buildTypeFromScratch(String newTypeName, Writer writer,
			IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader);

	byte[] buildTypeFromParent(String newTypeName, byte[] sourceContent, Writer writer,
			IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader);

	Class<?> loadClass(String typeName, byte[] content, ClassLoader classLoader);

	void verify(byte[] content, ClassLoader classLoader);
}
