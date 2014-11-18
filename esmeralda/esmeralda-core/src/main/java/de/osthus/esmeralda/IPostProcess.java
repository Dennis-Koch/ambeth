package de.osthus.esmeralda;

import java.io.Writer;

public interface IPostProcess
{
	void postProcess(Writer writer) throws Throwable;
}
