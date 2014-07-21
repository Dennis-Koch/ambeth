package de.osthus.filesystem.hdf5;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Hdf5UriTest.class, //
		Hdf5FileSystemProviderTest.class, //
		Hdf5FileSystemTest.class, //
		Hdf5PathTest.class, //
		PackAndUnpackTest.class, //
		RandomAccessTest.class })
public class AllTests
{
}
