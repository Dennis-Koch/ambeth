package de.osthus.filesystem.directory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import lombok.Getter;
import de.osthus.filesystem.common.AbstractUri;

/**
 * URI analyser and data structure for Directory File System specific URIs.
 * 
 * @author jochen.hormes
 * @start 2014-07-21
 */
@Getter
public class DirectoryUri extends AbstractUri
{
	// example1 dir:file:///c:/temp/dir/!/data/file.txt
	// example2 dir:jar:file:/tmp/arc.zip!/test/!/data/file.txt
	private static final Pattern MAIN_URI_PATTERN = Pattern.compile(DirectoryFileSystemProvider.SCHEME + ":(.+?)((?<=/)!(.+))?");

	// Pattern for non-windows file systems
	private static final Pattern SUB_URI_PATTERN_1 = Pattern.compile("(.+)!([^!]+)");

	// Pattern for windows-style file systems
	private static final Pattern SUB_URI_PATTERN_2 = Pattern.compile("([^/]+)(/+(.+))");

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

	public DirectoryUri(String str) throws URISyntaxException
	{
		super(str);
	}

	public DirectoryUri(URI uri) throws URISyntaxException
	{
		super(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getMainUriPattern()
	{
		return MAIN_URI_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getSubUriPattern1()
	{
		return SUB_URI_PATTERN_1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getSubUriPattern2()
	{
		return SUB_URI_PATTERN_2;
	}
}
