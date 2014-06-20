package de.osthus.ambeth.bytecode.core;

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

import de.osthus.ambeth.bytecode.IBuildVisitorDelegate;
import de.osthus.ambeth.bytecode.IBytecodeClassLoader;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.visitor.InterfaceToClassVisitor;
import de.osthus.ambeth.bytecode.visitor.LogImplementationsClassVisitor;
import de.osthus.ambeth.bytecode.visitor.PublicConstructorVisitor;
import de.osthus.ambeth.bytecode.visitor.SuppressLinesClassVisitor;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.WeakSmartCopyMap;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.LogWriter;
import de.osthus.ambeth.proxy.IEnhancedType;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassReader;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassWriter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.tree.ClassNode;
import de.osthus.ambeth.repackaged.org.objectweb.asm.util.CheckClassAdapter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.util.TraceClassVisitor;

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
		typeToContentMap.setAutoCleanupReference(true);
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
