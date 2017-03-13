package com.koch.ambeth.xml.postprocess;

import com.koch.ambeth.xml.IReader;

public interface IPostProcessReader extends IReader
{
	void executeObjectCommands();
}
