package com.koch.ambeth.bytecode.core;

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

import com.koch.ambeth.bytecode.IBuildVisitorDelegate;
import com.koch.ambeth.bytecode.IBytecodeClassLoader;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceToClassVisitor;
import com.koch.ambeth.bytecode.visitor.LogImplementationsClassVisitor;
import com.koch.ambeth.bytecode.visitor.PublicConstructorVisitor;
import com.koch.ambeth.bytecode.visitor.SuppressLinesClassVisitor;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LogWriter;
import com.koch.ambeth.merge.proxy.IEnhancedType;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.collections.WeakSmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.ClassLoaderAwareClassWriter;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BytecodeClassLoader implements IBytecodeClassLoader, IEventListener {
    protected final WeakSmartCopyMap<ClassLoader, ClassLoaderEntry> typeToContentMap = new WeakSmartCopyMap<>();
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected IClassLoaderProvider classLoaderProvider;
    @LogInstance
    private ILogger log;

    @Override
    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
        if (!(eventObject instanceof ClearAllCachesEvent)) {
            return;
        }
        typeToContentMap.clear();
    }

    protected ClassLoaderEntry ensureEntry(ClassLoader classLoader) {
        // if (classLoader == null) {
        classLoader = classLoaderProvider.getClassLoader();
        // }
        ClassLoaderEntry entry = typeToContentMap.get(classLoader);
        if (entry == null) {
            entry = new ClassLoaderEntry(new AmbethClassLoader(classLoader));
            typeToContentMap.put(classLoader, entry);
        }
        return entry;
    }

    @Override
    public Class<?> loadClass(String typeName, byte[] content, ClassLoader classLoader) {
        typeName = typeName.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement("."));
        Class<?> type = ensureEntry(classLoader).ambethClassLoader.defineClass(typeName, content);
        type.getDeclaredConstructors(); // helps to get some early verification errors
        return type;
    }

    @Override
    public byte[] readTypeAsBinary(Class<?> type, ClassLoader classLoader) {
        ClassLoaderEntry entry = ensureEntry(classLoader);
        Reference<byte[]> contentR = entry.typeToContentMap.get(type);
        byte[] content = null;
        if (contentR != null) {
            content = contentR.get();
        }
        if (content != null) {
            return content;
        }
        AmbethClassLoader ambethClassLoader = entry.ambethClassLoader;
        try {
            content = ambethClassLoader.getContent(type);
            if (content != null) {
                entry.typeToContentMap.put(type, new WeakReference<>(content));
                return content;
            }
            String bytecodeTypeName = getBytecodeTypeName(type);
            InputStream is = ambethClassLoader.getResourceAsStream(bytecodeTypeName + ".class");
            if (is == null) {
                throw new IllegalArgumentException("No class found with name '" + type.getName() + "'");
            }
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int oneByte;
                while ((oneByte = is.read()) != -1) {
                    bos.write(oneByte);
                }
                content = bos.toByteArray();
                entry.typeToContentMap.put(type, new WeakReference<>(content));
                return content;
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public void verify(byte[] content, ClassLoader classLoader) {
        CheckClassAdapter.verify(new ClassReader(content), ensureEntry(classLoader).ambethClassLoader, false, new PrintWriter(new LogWriter(log)));
    }

    @Override
    public byte[] buildTypeFromScratch(String newTypeName, Writer writer, IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader) {
        newTypeName = getBytecodeTypeName(newTypeName);
        var objContent = readTypeAsBinary(Object.class, classLoader);

        return buildTypeFromParent(newTypeName, objContent, writer, buildVisitorDelegate, classLoader);
    }

    @SneakyThrows
    @Override
    public byte[] buildTypeFromParent(String newTypeName, byte[] sourceContent, Writer writer, IBuildVisitorDelegate buildVisitorDelegate, ClassLoader classLoader) {
        newTypeName = getBytecodeTypeName(newTypeName);
        var cr = new ClassReader(new ByteArrayInputStream(sourceContent));
        var cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.EXPAND_FRAMES);

        var cw = new ClassLoaderAwareClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, classLoader);
        var pw = new PrintWriter(writer);

        ClassVisitor visitor = new SuppressLinesClassVisitor(cw);
        visitor = beanContext.registerWithLifecycle(new LogImplementationsClassVisitor(visitor)).finish();
        visitor = new TraceClassVisitor(visitor, pw);

        var wrappedVisitor = visitor;
        int originalModifiers = BytecodeBehaviorState.getState().getOriginalType().getModifiers();
        if (Modifier.isInterface(originalModifiers) || Modifier.isAbstract(originalModifiers)) {
            wrappedVisitor = new InterfaceToClassVisitor(wrappedVisitor);
        }
        if (!PublicConstructorVisitor.hasValidConstructor()) {
            wrappedVisitor = new PublicConstructorVisitor(wrappedVisitor);
        }
        wrappedVisitor = buildVisitorDelegate.build(wrappedVisitor);

        if (wrappedVisitor == visitor) {
            // there seem to be no custom action to be done with the new type. So we skip type
            // enhancement
            return null;
        }
        visitor = wrappedVisitor;

        visitor.visit(cn.version, cn.access, newTypeName, null, cn.name, new String[0]);

        visitor.visitEnd();

        // visitor = new ClassDeriver(visitor, newTypeName);
        // cr.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] content = cw.toByteArray();
        verify(content, classLoader);
        return content;
    }

    @Override
    public String toPrintableBytecode(Class<?> type) {
        if (type == null) {
            return "<null>";
        }
        try {
            var sb = new StringBuilder();

            toPrintableByteCodeIntern(type, sb, type.getClassLoader());
            return sb.toString();
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected void toPrintableByteCodeIntern(Class<?> type, StringBuilder sb, ClassLoader classLoader) {
        if (type.getSuperclass() != null && IEnhancedType.class.isAssignableFrom(type.getSuperclass())) {
            // write parent classes first
            toPrintableByteCodeIntern(type.getSuperclass(), sb, classLoader);
            sb.append('\n');
        }
        {
            try {
                var content = ensureEntry(classLoader).ambethClassLoader.getContent(type);
                if (content == null) {
                    content = readTypeAsBinary(type, classLoader);
                }
                var cr = new ClassReader(new ByteArrayInputStream(content));

                var writer = new StringWriter();
                var pw = new PrintWriter(writer);
                var visitor = new TraceClassVisitor(pw);
                cr.accept(visitor, ClassReader.EXPAND_FRAMES);
                sb.append(writer.toString());
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @Override
    public String getBytecodeTypeName(Class<?> type) {
        return getBytecodeTypeName(type.getName());
    }

    protected String getBytecodeTypeName(String typeName) {
        return typeName.replaceAll(Pattern.quote("."), "/");
    }

    public static class ClassLoaderEntry {
        protected final AmbethClassLoader ambethClassLoader;

        protected final WeakSmartCopyMap<Class<?>, Reference<byte[]>> typeToContentMap = new WeakSmartCopyMap<>();

        public ClassLoaderEntry(AmbethClassLoader ambethClassLoader) {
            super();
            this.ambethClassLoader = ambethClassLoader;
            typeToContentMap.setAutoCleanupNullValue(true);
        }
    }
}
