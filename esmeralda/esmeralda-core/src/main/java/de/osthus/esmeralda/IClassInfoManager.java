package de.osthus.esmeralda;

import java.util.List;

import com.sun.source.tree.Tree;

import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public interface IClassInfoManager
{
	Object[] findPathToTree(Method method, Tree tree);

	JavaClassInfo resolveClassInfo(String fqTypeName);

	JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly);

	void init(List<JavaClassInfo> classInfos);
}