package de.osthus.esmeralda.snippet;

import java.util.List;

public interface ISnippetManager
{
	void writeSnippet(List<String> untranslatableStatements);

	void finished();
}
