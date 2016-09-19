package de.osthus.ambeth.bytebuffer;

/**
 * Ensures that {@link FileKey}-related IO will be executed always with an identical instance of RandomAccessFile which is then forwarded to the
 * <code>IFileReadDelegate</code>.
 */
public interface IFileHandleCache
{
	<T> T readOnFile(FileKey fileKey, IFileReadDelegate<T> fileReadDelegate);
}