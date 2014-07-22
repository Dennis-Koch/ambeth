package de.osthus.filesystem.directory;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RandomAccessTest
{
	private static final String randomAccessFsDir = "src/test/resources/folder1";

	private static final String randomAccessPath = "/data/myProject1/randomAccessFile.txt";

	private static final Set<OpenOption> optionRead = Collections.<OpenOption> singleton(StandardOpenOption.READ);

	private static long startIndex = 246 + 4 * System.getProperty("line.separator").length();

	private static int length = 9;

	private static Path directoryPath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		Path fsPath = Paths.get(randomAccessFsDir);
		String str = "dir:" + fsPath.toUri().toString() + "!" + randomAccessPath;

		URI uri = URI.create(str);
		directoryPath = Paths.get(uri);
	}

	private FileChannel fileChannel;

	@Before
	public void setUp() throws Exception
	{
		fileChannel = FileChannel.open(directoryPath, optionRead);
	}

	@After
	public void tearDown() throws Exception
	{
		fileChannel.close();
	}

	@Test
	public void test() throws IOException
	{
		MappedByteBuffer byteBuffer = fileChannel.map(MapMode.READ_ONLY, startIndex, length);

		byte[] bytes = new byte[length];
		byteBuffer.get(bytes);

		String content = new String(bytes);
		assertEquals("Test Text", content);
	}
}
