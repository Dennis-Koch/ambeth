package com.koch.ambeth.testutil.category;

public interface IntegrationTests extends SlowTests {
	int setProperty(java.util.Properties properties);

	void deleteProperty(String property);

	void doAwesomeStuff();

	void baba();

	int cntTo1();
}
