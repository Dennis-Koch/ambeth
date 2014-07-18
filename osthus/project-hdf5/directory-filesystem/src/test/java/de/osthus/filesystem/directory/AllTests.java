package de.osthus.filesystem.directory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DirectoryFileSystemProviderTest.class, //
		DirectoryFileSystemTest.class, //
		DirectoryPathTest.class, //
		PackAndUnpackTest.class })
public class AllTests
{
}
