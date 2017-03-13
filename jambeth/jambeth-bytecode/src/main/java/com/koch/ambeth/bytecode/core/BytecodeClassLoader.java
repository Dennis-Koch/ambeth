package com.koch.ambeth.bytecode.core;

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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

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
import com.koch.ambeth.util.collections.WeakSmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BytecodeClassLoader implements IBytecodeClassLoader, IEventListener
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	protected final AmbethClassLoader ambethClassLoader;

	protected final WeakSmartCopyMap<Class<?>, Reference<byte[]>> typeToContentMap = new WeakSmartCopyMap<Class<?>, Reference<byte[]>>();

	public BytecodeClassLoader()
	{
		ambethClassLoader = new AmbethClassLoader(Thread.currentThread().getContextClassLoader());
		typeToContentMap.setAutoCleanupNullValue(true);
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		if (!(eventObject instanceof ClearAllCachesEvent))
		{
			return;
		}
		typeToContentMap.clear();
	}

	@Override
	public Class<?> loadClass(String typeName, byte[] content)
	{
		typeName = typeName.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement("."));
		return ambethClassLoader.defineClass(typeName, content);
	}

	@Override
	public byte[] readTypeAsBinary(Class<?> type)
	{
		Reference<byte[]> contentR = typeToContentMap.get(type);
		byte[] content = null;
		if (contentR != null)
		{
			content = contentR.get();
		}
		if (content != null)
		{
			return content;
		}
		try
		{
			content = ambethClassLoader.getContent(type);
			if (content != null)
			{
				typeToContentMap.put(type, new WeakReference<byte[]>(content));
				return content;
			}
			String bytecodeTypeName = getBytecodeTypeName(type);
			InputStream is = ambethClassLoader.getResourceAsStream(bytecodeTypeName + ".class");
			if (is == null)
			{
				throw new IllegalArgumentException("No class found with name '" + type.getName() + "'");
			}
			try
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int oneByte;
				while ((oneByte = is.read()) != -1)
				{
					bos.write(oneByte);
				}
				content = bos.toByteArray();
				typeToContentMap.put(type, new WeakReference<byte[]>(content));
				return content;
			}
			finally
			{
				is.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void verify(byte[] content)
	{
		CheckClassAdapter.verify(new ClassReader(content), ambethClassLoader, false, new PrintWriter(new LogWriter(log)));
	}

	@Override
	public byte[] buildTypeFromScratch(String newTypeName, Writer writer, IBuildVisitorDelegate buildVisitorDelegate)
	{
		newTypeName = getBytecodeTypeName(newTypeName);
		try
		{
			byte[] objContent = readTypeAsBinary(Object.class);

			return buildTypeFromParent(newTypeName, objContent, writer, buildVisitorDelegate);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] buildTypeFromParent(String newTypeName, byte[] sourceContent, Writer writer, IBuildVisitorDelegate buildVisitorDelegate)
	{
		newTypeName = getBytecodeTypeName(newTypeName);
		try
		{
			ClassReader cr = new ClassReader(new ByteArrayInputStream(sourceContent));
			ClassNode cn = new ClassNode();
			cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.EXPAND_FRAMES);

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			PrintWriter pw = new PrintWriter(writer);

			ClassVisitor visitor = new SuppressLinesClassVisitor(cw);
			visitor = beanContext.registerWithLifecycle(new LogImplementationsClassVisitor(visitor)).finish();
			visitor = new TraceClassVisitor(visitor, pw);

			ClassVisitor wrappedVisitor = visitor;
			int originalModifiers = BytecodeBehaviorState.getState().getOriginalType().getModifiers();
			if (Modifier.isInterface(originalModifiers) || Modifier.isAbstract(originalModifiers))
			{
				wrappedVisitor = new InterfaceToClassVisitor(wrappedVisitor);
			}
			if (!PublicConstructorVisitor.hasValidConstructor())
			{
				wrappedVisitor = new PublicConstructorVisitor(wrappedVisitor);
			}
			wrappedVisitor = buildVisitorDelegate.build(wrappedVisitor);

			if (wrappedVisitor == visitor)
			{
				// there seem to be no custom action to be done with the new type. So we skip type enhancement
				return null;
			}
			visitor = wrappedVisitor;

			visitor.visit(cn.version, cn.access, newTypeName, null, cn.name, new String[0]);

			visitor.visitEnd();

			// visitor = new ClassDeriver(visitor, newTypeName);
			// cr.accept(visitor, ClassReader.EXPAND_FRAMES);
			byte[] content = cw.toByteArray();
			verify(content);
			return content;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public String toPrintableBytecode(Class<?> type)
	{
		if (type == null)
		{
			return "<null>";
		}
		try
		{
			StringBuilder sb = new StringBuilder();

			toPrintableByteCodeIntern(type, sb);
			return sb.toString();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void toPrintableByteCodeIntern(Class<?> type, StringBuilder sb)
	{
		if (type.getSuperclass() != null && IEnhancedType.class.isAssignableFrom(type.getSuperclass()))
		{
			// write parent classes first
			toPrintableByteCodeIntern(type.getSuperclass(), sb);
			sb.append('\n');
		}
		{
			try
			{
				byte[] content = ambethClassLoader.getContent(type);
				if (content == null)
				{
					content = readTypeAsBinary(type);
				}
				ClassReader cr = new ClassReader(new ByteArrayInputStream(content));

				StringWriter writer = new StringWriter();
				PrintWriter pw = new PrintWriter(writer);
				ClassVisitor visitor = new TraceClassVisitor(pw);
				cr.accept(visitor, ClassReader.EXPAND_FRAMES);
				sb.append(writer.toString());
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public String getBytecodeTypeName(Class<?> type)
	{
		return getBytecodeTypeName(type.getName());
	}

	protected String getBytecodeTypeName(String typeName)
	{
		return typeName.replaceAll(Pattern.quote("."), "/");
	}
}
