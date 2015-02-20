package de.osthus.ambeth.testutil;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

public class AmbethIocSuite extends Suite
{
	public AmbethIocSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError
	{
		super(klass, suiteClasses);
	}

	public AmbethIocSuite(Class<?> klass, List<Runner> runners) throws InitializationError
	{
		super(klass, runners);
	}

	public AmbethIocSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError
	{
		super(klass, builder);
	}

	public AmbethIocSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError
	{
		super(builder, klass, suiteClasses);
	}

	public AmbethIocSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError
	{
		super(builder, classes);
	}

	@Override
	protected Statement withBeforeClasses(Statement statement)
	{
		final Statement withBeforeClasses = super.withBeforeClasses(statement);
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				AmbethIocRunner.restorePreviousTestSetupTL.set(new ArrayList<IBackgroundWorkerDelegate>());
				withBeforeClasses.evaluate();
			}
		};
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
				AmbethIocRunner.restorePreviousTestSetupTL.set(null);
				IocTestSetup previousTestSetup = AmbethIocRunner.previousTestSetupTL.get();
				if (previousTestSetup != null)
				{
					AmbethIocRunner.previousTestSetupTL.set(null);
					previousTestSetup.dispose();
				}
			}
		};
	}
}
