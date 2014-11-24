package de.osthus.esmeralda.handler;

import demo.codeanalyzer.common.model.JavaClassInfo;

public interface IClassInfoFactory
{
	JavaClassInfo createClassInfo(String fqName);
}
