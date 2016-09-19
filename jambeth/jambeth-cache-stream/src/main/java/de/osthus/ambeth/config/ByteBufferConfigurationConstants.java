package de.osthus.ambeth.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class ByteBufferConfigurationConstants
{
	@ConfigurationConstantDescription("TODO")
	public static final String ChunkPrefetchCount = "ambeth.bytebuffer.chunk.prefetch.count";

	@ConfigurationConstantDescription("TODO")
	public static final String ChunkSize = "ambeth.bytebuffer.chunk.size";

	@ConfigurationConstantDescription("TODO")
	public static final String CleanupCounterThreshold = "ambeth.bytebuffer.freephysicalmemory.cleanupcounter";

	@ConfigurationConstantDescription("TODO")
	public static final String FreePhysicalMemoryRatio = "ambeth.bytebuffer.freephysicalmemory.ratio";

	private ByteBufferConfigurationConstants()
	{
		// Intended blank
	}
}
