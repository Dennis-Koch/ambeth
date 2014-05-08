package de.osthus.ambeth.testutil.contextstore;

public interface IInjectionConfig
{
	void validate();

	IBeanGetter getSourceBeanGetter();

	IBeanGetter getTargetBeanGetter();

	String getTargetPropertyName();
}
