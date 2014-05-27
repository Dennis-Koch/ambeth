package de.osthus.ambeth.xml.postprocess;

import de.osthus.ambeth.xml.IReader;

public interface IPostProcessReader extends IReader
{
	void executeObjectCommands();
}
