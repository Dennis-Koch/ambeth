package de.osthus.filesystem.hdf5;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

// TODO Activate when HDF5 access is implemented
@Ignore
public class RandomAccessTest
{
	private static final String randomAccessFsDir = "src/test/resources/project1.h5";

	private static final String randomAccessPath = "/data/myProject1/randomAccessFile.txt";

	private static final Set<OpenOption> optionRead = Collections.<OpenOption> singleton(StandardOpenOption.READ);

	private static Hdf5FileSystemProvider hdf5FileSystemProvider;

	private static Hdf5Path hdf5Path;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		hdf5FileSystemProvider = new Hdf5FileSystemProvider();
		Path fsPath = Paths.get(randomAccessFsDir);
		String str = hdf5FileSystemProvider.getScheme() + ":" + fsPath.toUri().toString() + "!" + randomAccessPath;

		URI uri = URI.create(str);

		hdf5Path = hdf5FileSystemProvider.getPath(uri);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	private FileChannel fileChannel;

	@Before
	public void setUp() throws Exception
	{
		fileChannel = FileChannel.open(hdf5Path, optionRead);
	}

	@After
	public void tearDown() throws Exception
	{
		fileChannel.close();
	}

	@Test
	public void test() throws IOException
	{
		MappedByteBuffer byteBuffer = fileChannel.map(MapMode.READ_ONLY, 254, 9);

		byte[] bytes = new byte[9];
		byteBuffer.get(bytes);

		String content = new String(bytes);
		assertEquals("Test Text", content);
	}
}