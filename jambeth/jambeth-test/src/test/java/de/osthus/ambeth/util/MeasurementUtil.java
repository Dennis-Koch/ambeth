package de.osthus.ambeth.util;

public final class MeasurementUtil
{
	public static boolean logMeasurements = true;

	private MeasurementUtil()
	{
		// Intended blank
	}

	public static void logMeasurement(String name, Object value)
	{
		if (logMeasurements)
		{
			System.out.println("<measurement><name>" + name + "</name><value>" + value + "</value></measurement>");
		}
	}
}
