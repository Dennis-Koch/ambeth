package de.osthus.filesystem.directory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 * @author jochen.hormes
 * @start 2014-07-16
 */
public final class TestConstant
{
	public static final String NAME_FILE_FS_TEMP_FOLDER;

	public static final String NAME_DIR_FS_TEMP_FOLDER;

	static
	{
		String tempDirName = System.getProperty("java.io.tmpdir");
		Path tempPath = Paths.get(tempDirName);
		URI tempUri = tempPath.toUri();
		NAME_FILE_FS_TEMP_FOLDER = tempUri.toString();
		NAME_DIR_FS_TEMP_FOLDER = "dir:///" + NAME_FILE_FS_TEMP_FOLDER;
	}

	private TestConstant()
	{
		// Intended blank
	}
}
