package com.koch.ambeth.bytecode;

import java.io.Writer;

import com.koch.ambeth.merge.bytecode.IBytecodePrinter;

public interface IBytecodeClassLoader extends IBytecodePrinter
{
	byte[] readTypeAsBinary(Class<?> type);

	String getBytecodeTypeName(Class<?> type);

	byte[] buildTypeFromScratch(String newTypeName, Writer writer, IBuildVisitorDelegate buildVisitorDelegate);

	byte[] buildTypeFromParent(String newTypeName, byte[] sourceContent, Writer writer, IBuildVisitorDelegate buildVisitorDelegate);

	Class<?> loadClass(String typeName, byte[] content);

	void verify(byte[] content);
}
