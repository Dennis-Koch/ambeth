package de.osthus.ambeth.testutil;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import de.osthus.ambeth.annotation.AnnotationInfo;
import de.osthus.ambeth.annotation.IAnnotationInfo;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.NullPrintStream;

public class AmbethIocRunner extends BlockJUnit4ClassRunner
{
	public static final ThreadLocal<List<IBackgroundWorkerDelegate>> restorePreviousTestSetupTL = new ThreadLocal<List<IBackgroundWorkerDelegate>>();

	public static final ThreadLocal<IocTestSetup> previousTestSetupTL = new ThreadLocal<IocTestSetup>();

	protected boolean hasContextBeenRebuildForThisTest;

	protected boolean isRebuildContextForThisTestRecommended;

	protected IocTestSetup testSetup;

	protected final ThreadLocal<Object> targetProxyTL = new ThreadLocal<Object>();

	public AmbethIocRunner(Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	public IServiceContext getBeanContext()
	{
		return testSetup != null ? testSetup.beanContext : null;
	}

	public void cleanupThreadLocals()
	{
		testSetup.beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
	}

	public void rebuildContext()
	{
		rebuildContext(null);
	}

	public void disposeContext()
	{
		if (testSetup != null)
		{
			testSetup.dispose();
			testSetup = null;
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

	protected boolean deepEquals(Object left, Object right)
	{
		if (left instanceof List && right instanceof List)
		{
			List<?> leftColl = (List<?>) left;
			List<?> rightColl = (List<?>) right;
			if (leftColl.size() != rightColl.size())
			{
				return false;
			}
			for (int a = leftColl.size(); a-- > 0;)
			{
				if (!deepEquals(leftColl.get(a), rightColl.get(a)))
				{
					return false;
				}
			}
			return true;
		}
		return EqualsUtil.equals(left, right);
	}

	protected boolean isTestSetupIdenticallyConfiguredWithMethod(FrameworkMethod frameworkMethod, IocTestSetup testSetup)
	{
		if (testSetup == null)
		{
			return false;
		}
		Properties baseProps = new Properties(Properties.getApplication());

		extendProperties(frameworkMethod, baseProps);

		ISet<String> allNewKeys = baseProps.collectAllPropertyKeys();
		HashMap<String, Object> existingBaseProps = testSetup.baseProps;
		// if (existingBaseProps.size() != allNewKeys.size())
		// {
		// return false;
		// }
		for (String newKey : allNewKeys)
		{
			Object existingValue = existingBaseProps.get(newKey);
			Object newValue = baseProps.get(newKey);
			if (!deepEquals(newValue, existingValue))
			{
				return false;
			}
		}
		for (String existingKey : existingBaseProps.keySet())
		{
			Object existingValue = existingBaseProps.get(existingKey);
			Object newValue = baseProps.get(existingKey);
			if (!deepEquals(newValue, existingValue))
			{
				return false;
			}
		}
		LinkedHashSet<Class<? extends IInitializingModule>> frameworkModulesSet = new LinkedHashSet<Class<? extends IInitializingModule>>();
		frameworkModulesSet.addAll(buildFrameworkTestModuleList(frameworkMethod));

		ArrayList<Class<?>> existingFrameworkModules = testSetup.frameworkModules;
		if (existingFrameworkModules.size() != frameworkModulesSet.size())
		{
			return false;
		}
		for (Class<?> existingFrameworkModule : existingFrameworkModules)
		{
			if (!frameworkModulesSet.contains(existingFrameworkModule))
			{
				return false;
			}
		}

		LinkedHashSet<Class<? extends IInitializingModule>> applicationModulesSet = new LinkedHashSet<Class<? extends IInitializingModule>>();
		applicationModulesSet.addAll(buildTestModuleList(frameworkMethod));

		ArrayList<Class<?>> existingApplicationModules = testSetup.applicationModules;
		if (existingApplicationModules.size() != applicationModulesSet.size())
		{
			return false;
		}
		for (Class<?> existingApplicationModule : existingApplicationModules)
		{
			if (!applicationModulesSet.contains(existingApplicationModule))
			{
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	protected void rebuildContext(FrameworkMethod frameworkMethod)
	{
		IocTestSetup previousTestSetup = previousTestSetupTL.get();
		if (previousTestSetup != null)
		{
			previousTestSetupTL.set(null);
			previousTestSetup.dispose();
			previousTestSetup = null;
		}
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

		IServiceContext testClassLevelContext = BeanContextFactory.createBootstrap(baseProps);
		IServiceContext beanContext = null;
		boolean success = false;
		try
		{
			Class<?> oldTypeScope = FileUtil.setCurrentTypeScope(getTestClass().getJavaClass());
			try
			{
				IServiceContext currentBeanContext = testClassLevelContext;
				if (frameworkModules.length > 0)
				{
					currentBeanContext = currentBeanContext.createService("framework", new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
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
				testClassLevelContext.getRoot().dispose();
				testClassLevelContext = null;
				beanContext = null;
			}
		}
		testSetup = createTestSetup(testClassLevelContext, beanContext);
		for (String key : baseProps.collectAllPropertyKeys())
		{
			testSetup.baseProps.put(key, baseProps.get(key));
		}
		testSetup.frameworkModules.addAll(frameworkModules);
		testSetup.applicationModules.addAll(applicationModules);

		if (restorePreviousTestSetupTL.get() != null)
		{
			previousTestSetupTL.set(testSetup);
		}
	}

	protected IocTestSetup createTestSetup(IServiceContext testClassLevelContext, IServiceContext beanContext)
	{
		return new IocTestSetup(testClassLevelContext, beanContext);
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
		Class<? extends ICleanupAfter> cleanupAfterType = getCleanupAfterType();
		if (cleanupAfterType != null)
		{
			childContextFactory.registerBean(cleanupAfterType).autowireable(ICleanupAfter.class);
		}
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

				if (previousTestSetupTL.get() != testSetup)
				{
					disposeContext();
					System.gc();
				}
			}
		};
	}

	protected Class<? extends ICleanupAfter> getCleanupAfterType()
	{
		return CleanupAfterIoc.class;
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
				Object targetProxy;
				if (IRunnerAware.class.isAssignableFrom(target.getClass()))
				{
					targetProxy = testSetup.beanContext.registerWithLifecycle(target).propertyValue("Runner", AmbethIocRunner.this).finish();
				}
				else
				{
					targetProxy = testSetup.beanContext.registerWithLifecycle(target).finish();
				}
				Object oldTargetProxy = targetProxyTL.get();
				targetProxyTL.set(targetProxy);
				try
				{
					returningStatement.evaluate();
				}
				finally
				{
					targetProxyTL.set(oldTargetProxy);

					if (testSetup.beanContext != null)
					{
						ICleanupAfter cleanupAfter = testSetup.beanContext.getService(ICleanupAfter.class, false);
						if (cleanupAfter != null)
						{
							cleanupAfter.cleanup();
						}
					}
					else if (testSetup.testClassLevelContext != null)
					{
						IThreadLocalCleanupController tlCleanupController = testSetup.testClassLevelContext.getService(IThreadLocalCleanupController.class);
						tlCleanupController.cleanupThreadLocal();
					}
				}
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
				Object targetProxy = targetProxyTL.get();
				if (targetProxy != null)
				{
					method.invokeExplosively(targetProxy);
				}
				else
				{
					statement.evaluate();
				}
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
				boolean doRebuildContext = false;
				if (!hasContextBeenRebuildForThisTest)
				{
					List<IAnnotationInfo<?>> rebuildContextList = findAnnotations(getTestClass().getJavaClass(), TestRebuildContext.class);
					if (rebuildContextList.size() > 0)
					{
						doRebuildContext = ((TestRebuildContext) rebuildContextList.get(rebuildContextList.size() - 1).getAnnotation()).value();
					}
					if (!doRebuildContext && !isTestSetupIdenticallyConfiguredWithMethod(method, testSetup))
					{
						if (isTestSetupIdenticallyConfiguredWithMethod(method, previousTestSetupTL.get()))
						{
							testSetup = previousTestSetupTL.get();
							hasContextBeenRebuildForThisTest = true;
							isRebuildContextForThisTestRecommended = false;
						}
						else
						{
							doRebuildContext = true;
						}
					}
				}
				if (doRebuildContext || isRebuildContextForThisTestRecommended)
				{
					rebuildContext(method);
					hasContextBeenRebuildForThisTest = true;
					isRebuildContextForThisTestRecommended = false;
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
