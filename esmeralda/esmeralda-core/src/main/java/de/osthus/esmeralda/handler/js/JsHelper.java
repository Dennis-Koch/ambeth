package de.osthus.esmeralda.handler.js;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsHelper implements IJsHelper
{
	protected static final HashMap<String, String[]> javaTypeToJsMap = new HashMap<String, String[]>();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void newLineIntend()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append('\n');
		int indentationLevel = context.getIndentationLevel();
		for (int a = indentationLevel; a-- > 0;)
		{
			writer.append("    ");
		}
	}

	@Override
	public boolean newLineIntendIfFalse(boolean value)
	{
		if (!value)
		{
			newLineIntend();
		}
		return false;
	}

	@Override
	public boolean newLineIntendWithCommaIfFalse(boolean value)
	{
		if (!value)
		{
			IConversionContext context = this.context.getCurrent();
			IWriter writer = context.getWriter();

			writer.append(",");
			newLineIntend();
		}
		return false;
	}

	@Override
	public void scopeIntend(IBackgroundWorkerDelegate run)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append('{');
		context.incremetIndentationLevel();
		try
		{
			run.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.decremetIndentationLevel();
		}
		newLineIntend();
		writer.append('}');
	}

	@Override
	public File createTargetFile()
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();
		File targetPath = context.getTargetPath();
		Path relativeTargetPath = createRelativeTargetPath();
		File targetFileDir = new File(targetPath, relativeTargetPath.toString());
		targetFileDir.mkdirs();

		File targetFile = new File(targetFileDir, createTargetFileName(classInfo));
		return targetFile;
	}

	@Override
	public Path createRelativeTargetPath()
	{
		String namespace = createNamespace();

		String relativeTargetPathName = namespace.replace(".", File.separator);

		String languagePath = context.getLanguagePath();
		if (languagePath != null && !languagePath.isEmpty())
		{
			relativeTargetPathName = languagePath + File.separator + relativeTargetPathName;
		}
		Path relativeTargetPath = Paths.get(relativeTargetPathName);

		return relativeTargetPath;
	}

	@Override
	public String createTargetFileName(JavaClassInfo classInfo)
	{
		String nonGenericType = astHelper.extractNonGenericType(classInfo.getName());
		return nonGenericType + ".js";
	}

	@Override
	public String createNamespace()
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();

		String packageName = classInfo.getPackageName();
		packageName = prefixModification(packageName, context);
		String namespace = StringConversionHelper.upperCaseFirst(objectCollector, packageName);

		return namespace;
	}

	@Override
	public boolean writeStringIfFalse(String value, boolean condition)
	{
		return false;
	}

	@Override
	public void writeType(String typeName)
	{
		writeTypeIntern(typeName, false);
	}

	@Override
	public void writeTypeDirect(String typeName)
	{
		writeTypeIntern(typeName, true);
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel)
	{
		return false;
	}

	@Override
	public void writeAnnotations(BaseJavaClassModel handle)
	{
	}

	@Override
	public void writeAnnotation(Annotation annotation)
	{
	}

	@Override
	public void writeGenericTypeArguments(List<Type> genericTypeArguments)
	{
	}

	@Override
	public void writeMethodArguments(List<JCExpression> methodArguments)
	{
	}

	@Override
	public void writeMethodArguments(JCExpression methodInvocation)
	{
	}

	@Override
	public void writeExpressionTree(Tree expressionTree)
	{
	}

	protected void writeTypeIntern(String typeName, boolean direct)
	{
		ParamChecker.assertParamNotNullOrEmpty(typeName, "typeName");

		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToJsMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				writeTypeIntern(typeName.substring(0, typeName.length() - 2), direct);
				writer.append("[]");
				return;
			}
			Matcher genericTypeMatcher = ASTHelper.genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);
				writeTypeIntern(plainType, direct);
				return;
			}

			typeName = prefixModification(typeName, context);

			if (!direct)
			{
				typeName = astHelper.resolveFqTypeFromTypeName(typeName);
				mappedTypeName = new String[] { StringConversionHelper.upperCaseFirst(objectCollector, typeName) };
			}
			else
			{
				mappedTypeName = new String[] { typeName };
			}
		}
		ISet<TypeUsing> usedTypes = context.getUsedTypes();
		if (usedTypes != null)
		{
			usedTypes.add(new TypeUsing(mappedTypeName[0], false));
		}
		// TODO think: Always full name in JS?
		// else
		// {
		// Map<String, String> imports = context.getImports();
		// if (imports != null)
		// {
		// String nameFromImplicitImport = imports.get(mappedTypeName[0]);
		// if (nameFromImplicitImport != null)
		// {
		// mappedTypeName = new String[] { nameFromImplicitImport };
		// }
		// }
		//
		// }
		writer.append(mappedTypeName[0]);
	}

	protected String prefixModification(String name, IConversionContext context)
	{
		String nsPrefixRemove = context.getNsPrefixRemove();
		if (name.startsWith(nsPrefixRemove))
		{
			int removeLength = nsPrefixRemove.length();
			name = name.substring(removeLength);
		}

		String nsPrefixAdd = context.getNsPrefixAdd();
		if (nsPrefixAdd != null)
		{
			name = nsPrefixAdd + name;
		}
		return name;
	}
}
