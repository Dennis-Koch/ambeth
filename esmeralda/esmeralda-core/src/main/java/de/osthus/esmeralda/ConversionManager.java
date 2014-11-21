package de.osthus.esmeralda;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.EmptySet;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.misc.StatementCount;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.MethodInfo;

public class ConversionManager implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected CodeProcessor codeProcessor;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IEsmeFileUtil fileUtil;

	@Autowired
	protected INodeHandlerRegistry nodeHandlerRegistry;

	@Property(name = "source-path")
	protected File[] sourcePath;

	@Property(name = "snippet-path")
	protected File snippetPath;

	@Property(name = "target-path")
	protected File targetPath;

	@Override
	public void afterStarted() throws Throwable
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);)
		{
			File[] allSourceFiles = fileUtil.findAllSourceFiles(sourcePath).toArray(File.class);
			Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(allSourceFiles);

			CompilationTask task = compiler.getTask(null, fileManager, new DiagnosticListener<JavaFileObject>()
			{
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic)
				{
					System.out.println(diagnostic.getMessage(null));
				}
			}, null, null, fileObjects);

			ArrayList<AbstractProcessor> processors = new ArrayList<AbstractProcessor>();
			processors.add(codeProcessor);
			task.setProcessors(processors);

			try
			{
				task.call();
			}
			catch (FastBreakException e)
			{
				// intended blank
			}
			catch (RuntimeException e)
			{
				if (!(e.getCause() instanceof FastBreakException))
				{
					throw e;
				}
				// intended blank
			}
		}

		INodeHandlerExtension csClassHandler = nodeHandlerRegistry.get(Lang.C_SHARP + EsmeType.CLASS);
		INodeHandlerExtension jsClassHandler = nodeHandlerRegistry.get(Lang.JS + EsmeType.CLASS);

		ArrayList<JavaClassInfo> classInfos = codeProcessor.getClassInfos();
		HashMap<String, JavaClassInfo> fqNameToClassInfoMap = new HashMap<String, JavaClassInfo>();

		for (JavaClassInfo classInfo : classInfos)
		{
			String fqName = classInfo.getPackageName() + "." + classInfo.getName();
			if (!fqNameToClassInfoMap.putIfNotExists(fqName, classInfo))
			{
				throw new IllegalStateException("Full qualified name is not unique: " + fqName);
			}
		}

		addClassInfo(mockType(java.io.ByteArrayInputStream.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.io.InputStream.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.io.OutputStream.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.Enum.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.Exception.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.Object.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.ref.SoftReference.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.ref.WeakReference.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.lang.RuntimeException.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.util.AbstractSet.class), fqNameToClassInfoMap);
		addClassInfo(mockType(java.util.EventObject.class), fqNameToClassInfoMap);
		addClassInfo(mockType(org.junit.runners.BlockJUnit4ClassRunner.class), fqNameToClassInfoMap);

		StatementCount csMetric = new StatementCount("C#");
		StatementCount jsMetric = new StatementCount("JS");

		for (JavaClassInfo classInfo : classInfos)
		{
			String packageName = classInfo.getPackageName();
			if (packageName == null)
			{
				continue;
			}

			ConversionContext csContext = new ConversionContext();
			csContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			csContext.setSnippetPath(snippetPath);
			csContext.setTargetPath(targetPath);
			csContext.setLanguagePath("csharp");
			csContext.setMetric(csMetric);
			csContext.setNsPrefixRemove("de.osthus.");
			csContext.setClassInfo(classInfo);

			invokeNodeHandler(csClassHandler, csContext);

			ConversionContext jsContext = new ConversionContext();
			jsContext.setFqNameToClassInfoMap(fqNameToClassInfoMap);
			jsContext.setSnippetPath(snippetPath);
			jsContext.setTargetPath(targetPath);
			jsContext.setLanguagePath("js");
			csContext.setMetric(jsMetric);
			jsContext.setNsPrefixRemove("de.osthus.");
			jsContext.setClassInfo(classInfo);

			invokeNodeHandler(jsClassHandler, jsContext);
		}

		if (log.isInfoEnabled())
		{
			log.info(csMetric.toString());
			log.info(jsMetric.toString());
		}
	}

	protected void invokeNodeHandler(INodeHandlerExtension nodeHandler, IConversionContext newContext)
	{
		IConversionContext oldContext = context.getCurrent();
		context.setCurrent(newContext);
		try
		{
			nodeHandler.handle(null);
		}
		catch (TypeResolveException e)
		{
			log.error(e);
		}
		catch (Throwable e)
		{
			JavaClassInfo classInfo = newContext.getClassInfo();
			log.error(new MaskingRuntimeException("Error occured while processing type '" + classInfo.getName() + "'", e));
		}
		finally
		{
			context.setCurrent(oldContext);
		}
	}

	protected void addClassInfo(JavaClassInfo classInfo, IMap<String, JavaClassInfo> fqNameToClassInfoMap)
	{
		String fqName = classInfo.getPackageName() + "." + classInfo.getName();
		if (!fqNameToClassInfoMap.putIfNotExists(fqName, classInfo))
		{
			throw new IllegalStateException("Full qualified name is not unique: " + fqName);
		}
	}

	protected JavaClassInfo mockType(Class<?> type)
	{
		JavaClassInfo classInfo = new JavaClassInfo();
		classInfo.setName(type.getSimpleName());
		classInfo.setPackageName(type.getPackage().getName());
		// TODO: set correct modifiers & type
		classInfo.setPublicFlag(true);

		// TODO: mock fields
		Method[] declaredMethods = ReflectUtil.getDeclaredMethods(Object.class);
		for (Method declaredMethod : declaredMethods)
		{
			classInfo.addMethod(mockMethod(classInfo, declaredMethod));
		}
		return classInfo;
	}

	protected MethodInfo mockMethod(JavaClassInfo owner, java.lang.reflect.Method method)
	{
		MethodInfo mi = new MethodInfo();
		mi.setName(method.getName());
		mi.setReturnType(method.getReturnType().getName());
		mi.setOwningClass(owner);
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int a = 0, size = parameterTypes.length; a < size; a++)
		{
			final String parameterName = "arg" + a;
			final Class<?> parameterType = parameterTypes[a];
			VariableElement ve = new VariableElement()
			{
				@Override
				public Name getSimpleName()
				{
					return new Name()
					{
						@Override
						public CharSequence subSequence(int start, int end)
						{
							return parameterName.subSequence(start, end);
						}

						@Override
						public int length()
						{
							return parameterName.length();
						}

						@Override
						public char charAt(int index)
						{
							return parameterName.charAt(index);
						}

						@Override
						public boolean contentEquals(CharSequence cs)
						{
							return parameterName.contentEquals(cs);
						}

						@Override
						public String toString()
						{
							return parameterName;
						}
					};
				}

				@Override
				public Set<Modifier> getModifiers()
				{
					return EmptySet.emptySet();
				}

				@Override
				public ElementKind getKind()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public Element getEnclosingElement()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<? extends Element> getEnclosedElements()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<? extends AnnotationMirror> getAnnotationMirrors()
				{
					return EmptyList.getInstance();
				}

				@Override
				public <A extends Annotation> A getAnnotation(Class<A> annotationType)
				{
					return null;
				}

				@Override
				public TypeMirror asType()
				{
					return new TypeMirror()
					{
						@Override
						public TypeKind getKind()
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public <R, P> R accept(TypeVisitor<R, P> v, P p)
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public String toString()
						{
							return parameterType.getName();
						}
					};
				}

				@Override
				public <R, P> R accept(ElementVisitor<R, P> v, P p)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public Object getConstantValue()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public String toString()
				{
					return asType().toString() + " " + getSimpleName().toString();
				}
			};
			mi.addParameters(ve);
		}
		return mi;
	}
}
