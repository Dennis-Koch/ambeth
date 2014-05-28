package de.osthus.ambeth.testutil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import de.osthus.ambeth.annotation.AnnotationInfo;
import de.osthus.ambeth.annotation.IAnnotationInfo;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.Logger;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public class AmbethIocRunner extends BlockJUnit4ClassRunner
{

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

	@Override
	protected final Statement withBeforeClasses(Statement statement)
	{
		final Statement withBeforeClasses = withBeforeClassesWithinContext(statement);
		return new Statement()
		{

			@Override
			public void evaluate() throws Throwable
			{
				rebuildContext(null);

				withBeforeClasses.evaluate();
			}
		};
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

	@SuppressWarnings("unchecked")
	protected void rebuildContext(FrameworkMethod frameworkMethod)
	{
		disposeContext();
		Properties.resetApplication();
		Properties.loadBootstrapPropertyFile();

		Properties baseProps = new Properties(Properties.getApplication());

		extendProperties(frameworkMethod, baseProps);

		List<IAnnotationInfo<?>> testModulesList = findAnnotations(getTestClass().getJavaClass(), frameworkMethod != null ? frameworkMethod.getMethod() : null,
				TestModule.class);

		List<IAnnotationInfo<?>> testFrameworkModulesList = findAnnotations(getTestClass().getJavaClass(),
				frameworkMethod != null ? frameworkMethod.getMethod() : null, TestFrameworkModule.class);

		HashSet<Class<? extends IInitializingModule>> testClassLevelTestFrameworkModulesList = new HashSet<Class<? extends IInitializingModule>>();
		HashSet<Class<? extends IInitializingModule>> testClassLevelTestModulesList = new HashSet<Class<? extends IInitializingModule>>();

		for (IAnnotationInfo<?> testModuleItem : testFrameworkModulesList)
		{
			TestFrameworkModule testFrameworkModule = (TestFrameworkModule) testModuleItem.getAnnotation();
			testClassLevelTestFrameworkModulesList.addAll(Arrays.asList(testFrameworkModule.value()));
		}
		for (IAnnotationInfo<?> testModuleItem : testModulesList)
		{
			TestModule testModule = (TestModule) testModuleItem.getAnnotation();
			testClassLevelTestModulesList.addAll(Arrays.asList(testModule.value()));
		}
		Class<? extends IInitializingModule>[] frameworkModules = testClassLevelTestFrameworkModulesList.toList().toArray(Class.class);
		Class<? extends IInitializingModule>[] bootstrapModules = testClassLevelTestModulesList.toList().toArray(Class.class);

		testClassLevelContext = BeanContextFactory.createBootstrap(baseProps);
		boolean success = false;
		try
		{
			if (Logger.objectCollector == null)
			{
				Logger.objectCollector = testClassLevelContext.getService(IThreadLocalObjectCollector.class);
				objectCollectorOfLoggerSet = true;
			}
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
			if (bootstrapModules.length > 0)
			{
				currentBeanContext = currentBeanContext.createService("application", bootstrapModules);
			}
			beanContext = currentBeanContext;
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

	protected Statement withBeforeClassesWithinContext(Statement statement)
	{
		return super.withBeforeClasses(statement);
	}

	@Override
	protected final Statement withAfterClasses(Statement statement)
	{
		final Statement withAfterClasses = withAfterClassesWithinContext(statement);

		return new Statement()
		{

			@Override
			public void evaluate() throws Throwable
			{
				withAfterClasses.evaluate();
				disposeContext();
			}
		};
	}

	protected Statement withAfterClassesWithinContext(Statement statement)
	{
		return super.withAfterClasses(statement);
	}

	@Override
	protected Object createTest() throws Exception
	{
		Class<?> javaClass = getTestClass().getJavaClass();
		if (IRunnerAware.class.isAssignableFrom(javaClass))
		{
			return beanContext.registerAnonymousBean(javaClass).propertyValue("Runner", this).finish();
		}
		else
		{
			return beanContext.registerAnonymousBean(javaClass).finish();
		}
	}

	@Override
	protected final void runChild(FrameworkMethod method, RunNotifier notifier)
	{
		runChildWithContext(method, notifier, false);
	}

	protected void runChildWithContext(FrameworkMethod method, RunNotifier notifier, boolean hasContextBeenRebuild)
	{
		try
		{
			if (!hasContextBeenRebuild && (method == null || method.getAnnotation(Ignore.class) == null))
			{
				List<IAnnotationInfo<?>> rebuildContextList = findAnnotations(getTestClass().getJavaClass(), TestRebuildContext.class);
				if (rebuildContextList.size() > 0)
				{
					boolean rebuildContext = ((TestRebuildContext) rebuildContextList.get(rebuildContextList.size() - 1).getAnnotation()).value();
					if (rebuildContext)
					{
						rebuildContext(method);
						hasContextBeenRebuild = true;
					}
				}
				if (!hasContextBeenRebuild && method != null)
				{
					Method targetMethod = method.getMethod();

					if (targetMethod.isAnnotationPresent(TestModule.class) || targetMethod.isAnnotationPresent(TestFrameworkModule.class)
							|| targetMethod.isAnnotationPresent(TestPropertiesList.class) || targetMethod.isAnnotationPresent(TestProperties.class))
					{
						rebuildContext(method);
						hasContextBeenRebuild = true;
					}
				}
			}
		}
		catch (MaskingRuntimeException e)
		{
			notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e.getMessage() == null ? e
					.getCause() : e));
			return;
		}
		catch (Throwable e)
		{
			notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e));
			return;
		}
		super.runChild(method, notifier);
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
