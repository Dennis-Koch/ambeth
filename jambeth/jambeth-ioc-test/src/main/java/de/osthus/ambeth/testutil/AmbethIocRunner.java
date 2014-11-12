package de.osthus.ambeth.testutil;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import de.osthus.ambeth.annotation.AnnotationInfo;
import de.osthus.ambeth.annotation.IAnnotationInfo;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.Logger;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.NullPrintStream;

public class AmbethIocRunner extends BlockJUnit4ClassRunner
{
	protected boolean hasContextBeenRebuildForThisTest;

	protected boolean isRebuildContextForThisTestRecommended;

	protected IServiceContext testClassLevelContext;

	protected IServiceContext beanContext;

	protected boolean objectCollectorOfLoggerSet;

	public AmbethIocRunner(Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	public IServiceContext getBeanContext()
	{
		return beanContext;
	}

	public void cleanupThreadLocals()
	{
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
	}

	public void rebuildContext()
	{
		rebuildContext(null);
	}

	public void disposeContext()
	{
		if (testClassLevelContext != null)
		{
			IThreadLocalCleanupController tlCleanupController = testClassLevelContext.getService(IThreadLocalCleanupController.class);
			testClassLevelContext.dispose();
			testClassLevelContext = null;
			beanContext = null;
			if (objectCollectorOfLoggerSet)
			{
				Logger.objectCollector = null;
			}
			tlCleanupController.cleanupThreadLocal();
		}
	}

	protected List<Class<? extends IInitializingModule>> buildTestModuleList(FrameworkMethod frameworkMethod)
	{
		List<IAnnotationInfo<?>> testModulesList = findAnnotations(getTestClass().getJavaClass(), frameworkMethod != null ? frameworkMethod.getMethod() : null,
				TestModule.class);

		ArrayList<Class<? extends IInitializingModule>> moduleList = new ArrayList<Class<? extends IInitializingModule>>();
		for (IAnnotationInfo<?> testModuleItem : testModulesList)
		{
			TestModule testFrameworkModule = (TestModule) testModuleItem.getAnnotation();
			moduleList.addAll(testFrameworkModule.value());
		}
		return moduleList;
	}

	protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(FrameworkMethod frameworkMethod)
	{
		List<IAnnotationInfo<?>> testFrameworkModulesList = findAnnotations(getTestClass().getJavaClass(),
				frameworkMethod != null ? frameworkMethod.getMethod() : null, TestFrameworkModule.class);

		ArrayList<Class<? extends IInitializingModule>> frameworkModuleList = new ArrayList<Class<? extends IInitializingModule>>();
		for (IAnnotationInfo<?> testModuleItem : testFrameworkModulesList)
		{
			TestFrameworkModule testFrameworkModule = (TestFrameworkModule) testModuleItem.getAnnotation();
			frameworkModuleList.addAll(testFrameworkModule.value());
		}
		return frameworkModuleList;
	}

	@SuppressWarnings("unchecked")
	protected void rebuildContext(FrameworkMethod frameworkMethod)
	{
		disposeContext();
		Properties.resetApplication();

		PrintStream oldPrintStream = System.out;
		System.setOut(NullPrintStream.INSTANCE);
		try
		{
			Properties.loadBootstrapPropertyFile();
		}
		finally
		{
			System.setOut(oldPrintStream);
		}

		Properties baseProps = new Properties(Properties.getApplication());

		extendProperties(frameworkMethod, baseProps);

		LinkedHashSet<Class<? extends IInitializingModule>> testClassLevelTestFrameworkModulesList = new LinkedHashSet<Class<? extends IInitializingModule>>();
		LinkedHashSet<Class<? extends IInitializingModule>> testClassLevelTestModulesList = new LinkedHashSet<Class<? extends IInitializingModule>>();

		testClassLevelTestModulesList.addAll(buildTestModuleList(frameworkMethod));
		testClassLevelTestFrameworkModulesList.addAll(buildFrameworkTestModuleList(frameworkMethod));

		Class<? extends IInitializingModule>[] frameworkModules = testClassLevelTestFrameworkModulesList.toArray(Class.class);
		Class<? extends IInitializingModule>[] applicationModules = testClassLevelTestModulesList.toArray(Class.class);

		testClassLevelContext = BeanContextFactory.createBootstrap(baseProps);
		boolean success = false;
		try
		{
			if (Logger.objectCollector == null)
			{
				Logger.objectCollector = testClassLevelContext.getService(IThreadLocalObjectCollector.class);
				objectCollectorOfLoggerSet = true;
			}
			Class<?> oldTypeScope = FileUtil.setCurrentTypeScope(getTestClass().getJavaClass());
			try
			{
				IServiceContext currentBeanContext = testClassLevelContext;
				if (frameworkModules.length > 0)
				{
					currentBeanContext = currentBeanContext.createService("framework", new RegisterPhaseDelegate()
					{
						@Override
						public void invoke(IBeanContextFactory childContextFactory)
						{
							rebuildContextDetails(childContextFactory);
						}
					}, frameworkModules);
				}
				if (applicationModules.length > 0)
				{
					currentBeanContext = currentBeanContext.createService("application", applicationModules);
				}
				beanContext = currentBeanContext;
			}
			finally
			{
				FileUtil.setCurrentTypeScope(oldTypeScope);
			}
			success = true;
		}
		finally
		{
			if (!success && testClassLevelContext != null)
			{
				testClassLevelContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
			}
		}
	}

	protected List<TestProperties> getAllTestProperties(FrameworkMethod frameworkMethod)
	{
		List<IAnnotationInfo<?>> testPropertiesList = findAnnotations(getTestClass().getJavaClass(), frameworkMethod != null ? frameworkMethod.getMethod()
				: null, TestPropertiesList.class, TestProperties.class);

		ArrayList<TestProperties> allTestProperties = new ArrayList<TestProperties>();

		for (int a = 0, size = testPropertiesList.size(); a < size; a++)
		{
			Annotation testPropertiesItem = testPropertiesList.get(a).getAnnotation();

			if (testPropertiesItem instanceof TestPropertiesList || testPropertiesItem instanceof TestProperties)
			{
				if (testPropertiesItem instanceof TestPropertiesList)
				{
					TestPropertiesList mtp = (TestPropertiesList) testPropertiesItem;
					allTestProperties.addAll(mtp.value());
				}
				else
				{
					allTestProperties.add((TestProperties) testPropertiesItem);
				}
			}
		}
		return allTestProperties;
	}

	protected void extendProperties(FrameworkMethod frameworkMethod, Properties props)
	{
		List<TestProperties> allTestProperties = getAllTestProperties(frameworkMethod);

		for (int a = 0, size = allTestProperties.size(); a < size; a++)
		{
			TestProperties testProperties = allTestProperties.get(a);
			Class<? extends IPropertiesProvider> testPropertiesType = testProperties.type();
			if (testPropertiesType != null && !IPropertiesProvider.class.equals(testPropertiesType))
			{
				IPropertiesProvider propertiesProvider;
				try
				{
					propertiesProvider = testPropertiesType.newInstance();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				propertiesProvider.fillProperties(props);
			}
			String testPropertiesFile = testProperties.file();
			if (testPropertiesFile != null && testPropertiesFile.length() > 0)
			{
				props.load(testPropertiesFile);
			}
			String testPropertyName = testProperties.name();
			String testPropertyValue = testProperties.value();
			if (testPropertyName != null && testPropertyName.length() > 0)
			{
				if (testPropertyValue != null && testPropertyValue.length() > 0)
				{
					// Override intended
					props.put(testPropertyName, testPropertyValue);
				}
			}
		}
	}

	protected void rebuildContextDetails(IBeanContextFactory childContextFactory)
	{
		childContextFactory.registerExternalBean(new TestContext(this)).autowireable(ITestContext.class);
	}

	@Override
	protected Statement withAfterClasses(Statement statement)
	{
		final Statement withAfterClasses = super.withAfterClasses(statement);

		return new Statement()
		{

			@Override
			public void evaluate() throws Throwable
			{
				withAfterClasses.evaluate();
				disposeContext();
				System.gc();
			}
		};
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Statement withAfters(FrameworkMethod method, final Object target, Statement statement)
	{
		final Statement returningStatement = super.withAfters(method, target, statement);
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				if (IRunnerAware.class.isAssignableFrom(target.getClass()))
				{
					beanContext.registerWithLifecycle(target).propertyValue("Runner", AmbethIocRunner.this).finish();
				}
				else
				{
					beanContext.registerWithLifecycle(target).finish();
				}
				returningStatement.evaluate();
			}
		};
	}

	@Override
	protected Object createTest() throws Exception
	{
		Class<?> javaClass = getTestClass().getJavaClass();
		return javaClass.newInstance();
	}

	@Override
	protected Statement methodInvoker(final FrameworkMethod method, final Object test)
	{
		final Statement statement = super.methodInvoker(method, test);
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				statement.evaluate();
			}
		};
	}

	@Override
	protected Statement methodBlock(final FrameworkMethod method)
	{
		final Statement statement = super.methodBlock(method);
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				if (!hasContextBeenRebuildForThisTest)
				{
					if (method == null || method.getAnnotation(Ignore.class) == null)
					{
						List<IAnnotationInfo<?>> rebuildContextList = findAnnotations(getTestClass().getJavaClass(), TestRebuildContext.class);
						if (rebuildContextList.size() > 0)
						{
							boolean rebuildContext = ((TestRebuildContext) rebuildContextList.get(rebuildContextList.size() - 1).getAnnotation()).value();
							if (rebuildContext)
							{
								rebuildContext(method);
								hasContextBeenRebuildForThisTest = true;
								isRebuildContextForThisTestRecommended = false;
							}
						}
						if (method != null)
						{
							Method targetMethod = method.getMethod();

							if (targetMethod.isAnnotationPresent(TestModule.class) || targetMethod.isAnnotationPresent(TestFrameworkModule.class)
									|| targetMethod.isAnnotationPresent(TestPropertiesList.class) || targetMethod.isAnnotationPresent(TestProperties.class))
							{
								rebuildContext(method);
								hasContextBeenRebuildForThisTest = true;
								isRebuildContextForThisTestRecommended = false;
							}
						}
					}
				}
				if (beanContext == null || isRebuildContextForThisTestRecommended)
				{
					rebuildContext(method);
				}
				statement.evaluate();
			}
		};
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier)
	{
		Class<?> oldTypeScope = FileUtil.setCurrentTypeScope(getTestClass().getJavaClass());
		try
		{
			hasContextBeenRebuildForThisTest = false;
			isRebuildContextForThisTestRecommended = false;
			super.runChild(method, notifier);
		}
		finally
		{
			FileUtil.setCurrentTypeScope(oldTypeScope);
		}
	}

	protected List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Class<?>... annotationTypes)
	{
		return findAnnotations(type, null, annotationTypes);
	}

	@SuppressWarnings("unchecked")
	protected List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Method method, Class<?>... annotationTypes)
	{
		ArrayList<IAnnotationInfo<?>> targetList = new ArrayList<IAnnotationInfo<?>>();
		findAnnotations(type, targetList, true, annotationTypes);

		if (method != null)
		{
			for (Class<?> annotationType : annotationTypes)
			{
				Annotation annotation = method.getAnnotation((Class<? extends Annotation>) annotationType);
				if (annotation != null)
				{
					targetList.add(new AnnotationInfo<Annotation>(annotation, method));
				}
			}
		}
		return targetList;
	}

	@SuppressWarnings("unchecked")
	protected void findAnnotations(Class<?> type, List<IAnnotationInfo<?>> targetList, boolean isFirst, Class<?>... annotationTypes)
	{
		if (type == null || Object.class.equals(type))
		{
			return;
		}
		if (!type.isInterface())
		{
			findAnnotations(type.getSuperclass(), targetList, false, annotationTypes);
		}
		for (Class<?> annotationType : annotationTypes)
		{
			Annotation annotation = type.getAnnotation((Class<? extends Annotation>) annotationType);
			if (annotation != null)
			{
				targetList.add(new AnnotationInfo<Annotation>(annotation, type));
			}
		}
		if (isFirst)
		{
			Class<?>[] interfaces = type.getInterfaces();
			for (Class<?> currInterface : interfaces)
			{
				for (Class<?> annotationType : annotationTypes)
				{
					Annotation annotationOfInterface = currInterface.getAnnotation((Class<? extends Annotation>) annotationType);
					if (annotationOfInterface != null)
					{
						targetList.add(new AnnotationInfo<Annotation>(annotationOfInterface, currInterface));
					}
				}
			}
		}
	}
}
