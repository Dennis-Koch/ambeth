package de.osthus.esmeralda.misc;

import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public interface IToDoWriter
{
	void clearToDoFolder(String languagePathName);

	void write(String topic, Method method);

	void write(String topic, Method method, int pos);

	void write(String topic, JavaClassInfo classInfo, int pos);

	void write(String topic, String todo);

	void write(String topic, String todo, String languagePathName, boolean dryRun);
}
