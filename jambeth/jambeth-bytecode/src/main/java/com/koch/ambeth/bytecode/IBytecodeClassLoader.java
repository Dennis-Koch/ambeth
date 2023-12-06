package com.koch.ambeth.bytecode;

/*-
 * #%L
 * jambeth-bytecode
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

import com.koch.ambeth.merge.bytecode.IBytecodePrinter;

import java.io.Writer;

public interface IBytecodeClassLoader extends IBytecodePrinter {
    byte[] readTypeAsBinary(Class<?> type, ClassLoader classLoader);

    String getBytecodeTypeName(Class<?> type);

    byte[] buildTypeFromScratch(String newTypeName, Writer writer, IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader);

    byte[] buildTypeFromParent(String newTypeName, byte[] sourceContent, Writer writer, IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader);

    Class<?> loadClass(String typeName, byte[] content, ClassLoader classLoader);

    void verify(byte[] content, ClassLoader classLoader);
}
