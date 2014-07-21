package de.osthus.filesystem.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * Abstract URI analyzer and data structure for File System specific URIs.
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
@Getter
public abstract class AbstractUri
{
	protected static final int MAIN_URI_GROUP_IDENTIFIER = 1; // Key for FS: 'file:///c:/temp/dir/'
	// Group 2 is only defined as a group to be optional
	protected static final int MAIN_URI_GROUP_INTERNAL_PATH = 3; // Internal path: '/data/file.txt'

	protected static final int SUB_URI_1_GROUP_SUB_SCHEME = 1; // Sub FS identifier: jar:file:///c:/temp/arc.zip
	protected static final int SUB_URI_1_GROUP_SUB_PATH = 2; // Sub path in FS: '/test/'

	protected static final int SUB_URI_2_GROUP_SUB_SCHEME = 1; // Sub FS identifier: file:
	protected static final int SUB_URI_2_GROUP_SUB_PATH = 2; // Sub path in FS: '///C:/temp/target/'
	protected static final int SUB_URI_2_GROUP_SUB_PATH_2 = 3; // Sub path for Windows: 'C:/temp/target/'

	private String identifier;

	private String path;

	private String underlyingFileSystem;

	private String underlyingPath;

	private String underlyingPath2 = null;

	public AbstractUri(String str) throws URISyntaxException
	{
		URI uri = URI.create(str);
		analyse(uri);
	}

	public AbstractUri(URI uri) throws URISyntaxException
	{
		analyse(uri);
	}

	private void analyse(URI uri) throws URISyntaxException
	{
		String uriString = uri.toString();
		Matcher matcher = getMainUriPattern().matcher(uriString);
		if (matcher.matches())
		{
			identifier = matcher.group(MAIN_URI_GROUP_IDENTIFIER);
			path = matcher.group(MAIN_URI_GROUP_INTERNAL_PATH);
			if (path == null)
			{
				path = "";
			}

			Matcher matcher2 = getSubUriPattern1().matcher(identifier);
			if (matcher2.matches())
			{
				underlyingFileSystem = matcher2.group(SUB_URI_1_GROUP_SUB_SCHEME);
				underlyingPath = matcher2.group(SUB_URI_1_GROUP_SUB_PATH);
			}
			else
			{
				matcher2 = getSubUriPattern2().matcher(identifier);
				if (matcher2.matches())
				{
					underlyingFileSystem = matcher2.group(SUB_URI_2_GROUP_SUB_SCHEME);
					underlyingPath = matcher2.group(SUB_URI_2_GROUP_SUB_PATH);
					underlyingPath2 = matcher2.group(SUB_URI_2_GROUP_SUB_PATH_2);
				}
				else
				{
					throw new URISyntaxException(uriString, "URI not recognized");
				}
			}
			if (underlyingFileSystem.endsWith(":"))
			{
				underlyingFileSystem += "/";
			}
		}
	}

	/**
	 * Getter for the main URI pattern.<br>
	 * Provides the file system key and the internal path.<br>
	 * <br>
	 * Input example 1: dir:file:///c:/temp/dir/!/data/file.txt<br>
	 * Input example 2: dir:jar:file:/tmp/arc.zip!/test/!/data/file.txt<br>
	 * 
	 * @return Main URI pattern
	 */
	protected abstract Pattern getMainUriPattern();

	/**
	 * Getter for the pattern for non-windows file system URIs.
	 * 
	 * @return Sub URI pattern 1
	 */
	protected abstract Pattern getSubUriPattern1();

	/**
	 * Getter for the pattern for windows-style file system URIs.
	 * 
	 * @return Sub URI pattern 2
	 */
	protected abstract Pattern getSubUriPattern2();
}
