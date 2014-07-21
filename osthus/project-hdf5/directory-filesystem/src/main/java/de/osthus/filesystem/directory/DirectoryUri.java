package de.osthus.filesystem.directory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * URI analyser and data structure for Directory File System specific URIs.
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
@Getter
public class DirectoryUri
{
	// example1 dir:file:///c:/temp/dir/!/data/file.txt
	// example2 dir:jar:file:/tmp/arc.zip!/test/!/data/file.txt
	private static final Pattern MAIN_URI_PATTERN = Pattern.compile(DirectoryFileSystemProvider.SCHEME + ":(.+?)((?<=/)!(.+))?");
	protected static final int MAIN_URI_GROUP_IDENTIFIER = 1; // Key for FS: 'file:///c:/temp/dir/'
	// Group 2 is only defined as a group to be optional
	protected static final int MAIN_URI_GROUP_INTERNAL_PATH = 3; // Internal path: '/data/file.txt'

	// Pattern for non-windows file systems
	private static final Pattern SUB_URI_PATTERN_1 = Pattern.compile("(.+)!([^!]+)");
	protected static final int SUB_URI_1_GROUP_SUB_SCHEME = 1; // Sub FS identifier: jar:file:///c:/temp/arc.zip
	protected static final int SUB_URI_1_GROUP_SUB_PATH = 2; // Sub path in FS: '/test/'

	// Pattern for windows-style file systems
	private static final Pattern SUB_URI_PATTERN_2 = Pattern.compile("([^/]+)(/+(.+))");
	protected static final int SUB_URI_2_GROUP_SUB_SCHEME = 1; // Sub FS identifier: file:
	protected static final int SUB_URI_2_GROUP_SUB_PATH = 2; // Sub path in FS: '///C:/temp/target/'
	protected static final int SUB_URI_2_GROUP_SUB_PATH_2 = 3; // Sub path for Windows: 'C:/temp/target/'

	public static DirectoryUri create(String str)
	{
		URI uri = URI.create(str);
		return create(uri);
	}

	public static DirectoryUri create(URI uri)
	{
		try
		{
			return new DirectoryUri(uri);
		}
		catch (URISyntaxException x)
		{
			throw new IllegalArgumentException(x.getMessage(), x);
		}
	}

	private String identifier;

	private String path;

	private String underlyingFileSystem;

	private String underlyingPath;

	private String underlyingPath2 = null;

	public DirectoryUri(String str) throws URISyntaxException
	{
		URI uri = URI.create(str);
		analyse(uri);
	}

	public DirectoryUri(URI uri) throws URISyntaxException
	{
		analyse(uri);
	}

	private void analyse(URI uri) throws URISyntaxException
	{
		String uriString = uri.toString();
		Matcher matcher = MAIN_URI_PATTERN.matcher(uriString);
		if (matcher.matches())
		{
			identifier = matcher.group(MAIN_URI_GROUP_IDENTIFIER);
			path = matcher.group(MAIN_URI_GROUP_INTERNAL_PATH);
			if (path == null)
			{
				path = "";
			}

			Matcher matcher2 = SUB_URI_PATTERN_1.matcher(identifier);
			if (matcher2.matches())
			{
				underlyingFileSystem = matcher2.group(SUB_URI_1_GROUP_SUB_SCHEME);
				underlyingPath = matcher2.group(SUB_URI_1_GROUP_SUB_PATH);
			}
			else
			{
				matcher2 = SUB_URI_PATTERN_2.matcher(identifier);
				if (matcher2.matches())
				{
					underlyingFileSystem = matcher2.group(SUB_URI_2_GROUP_SUB_SCHEME);
					underlyingPath = matcher2.group(SUB_URI_2_GROUP_SUB_PATH);
					underlyingPath2 = matcher2.group(SUB_URI_2_GROUP_SUB_PATH_2);
				}
				else
				{
					throw new URISyntaxException(uriString, "URI not recognized by Directory File System");
				}
			}
			if (underlyingFileSystem.endsWith(":"))
			{
				underlyingFileSystem += "/";
			}
		}
	}
}
