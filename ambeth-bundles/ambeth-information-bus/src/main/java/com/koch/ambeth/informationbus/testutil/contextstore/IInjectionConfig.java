package com.koch.ambeth.informationbus.testutil.contextstore;

public interface IInjectionConfig
{
	void validate();

	IBeanGetter getSourceBeanGetter();

	IBeanGetter getTargetBeanGetter();

	String getTargetPropertyName();
}
