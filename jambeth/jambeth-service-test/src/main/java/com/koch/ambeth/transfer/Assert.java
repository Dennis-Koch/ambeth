package com.koch.ambeth.transfer;

import com.koch.ambeth.service.transfer.ServiceDescription;

public class Assert
{
	private Assert()
	{
		// Intended blank
	}

	public static void assertEquals(ServiceDescription expected, ServiceDescription actual)
	{
		if (expected == null)
		{
			org.junit.Assert.assertNull(actual);
			return;
		}

		org.junit.Assert.assertNotNull(actual);
		org.junit.Assert.assertEquals(expected.getServiceName(), actual.getServiceName());
		org.junit.Assert.assertEquals(expected.getMethodName(), actual.getMethodName());
		org.junit.Assert.assertArrayEquals(expected.getParamTypes(), actual.getParamTypes());
		org.junit.Assert.assertArrayEquals(expected.getArguments(), actual.getArguments());
		org.junit.Assert.assertArrayEquals(expected.getSecurityScopes(), actual.getSecurityScopes());
	}
}
