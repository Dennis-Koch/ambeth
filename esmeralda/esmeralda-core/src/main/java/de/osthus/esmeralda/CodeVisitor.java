package de.osthus.esmeralda;

import java.util.List;

import javax.lang.model.element.Element;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.helper.ClassInfoDataSetter;
import demo.codeanalyzer.helper.FieldInfoDataSetter;
import demo.codeanalyzer.helper.MethodInfoDataSetter;

/**
 * Visitor class which visits different nodes of the input source file, extracts the required atribute of the visiting class, its mehods, fields, annotations
 * etc and set it in the java class model.
 * 
 * @author Seema Richard (Seema.Richard@ust-global.com)
 * @author Deepa Sobhana (Deepa.Sobhana@ust-global.com)
 */
public class CodeVisitor extends TreePathScanner<Object, Trees>
{
	protected static final ArrayList<JavaClassInfo> classInfoStack = new ArrayList<JavaClassInfo>();

	protected static final ArrayList<JavaClassInfo> collectedClassInfos = new ArrayList<JavaClassInfo>();

	public static List<JavaClassInfo> getClassInfoStack()
	{
		return classInfoStack;
	}

	public void reset()
	{
		classInfoStack.clear();
		collectedClassInfos.clear();
	}

	@Autowired
	protected IConversionContext context;

	@Override
	public Object visitClass(ClassTree classTree, Trees trees)
	{
		JavaClassInfo classInfo = new JavaClassInfo(context);
		classInfoStack.add(classInfo);
		try
		{
			TreePath path = getCurrentPath();
			// populate required class information to model
			ClassInfoDataSetter.populateClassInfo(classInfo, classTree, path, trees);

			if (classInfo.getName() != null)
			{
				collectedClassInfos.add(classInfo);
			}
			return super.visitClass(classTree, trees);
		}
		finally
		{
			classInfoStack.remove(classInfoStack.size() - 1);
		}
	}

	/**
	 * Visits all methods of the input java source file
	 * 
	 * @param methodTree
	 * @param trees
	 * @return
	 */
	@Override
	public Object visitMethod(MethodTree methodTree, Trees trees)
	{
		TreePath path = getCurrentPath();
		// populate required method information to model
		MethodInfoDataSetter.populateMethodInfo(classInfoStack.get(classInfoStack.size() - 1), methodTree, path, trees);
		return super.visitMethod(methodTree, trees);
	}

	/**
	 * Visits all variables of the java source file
	 * 
	 * @param variableTree
	 * @param trees
	 * @return
	 */
	@Override
	public Object visitVariable(VariableTree variableTree, Trees trees)
	{
		TreePath path = getCurrentPath();
		Element e = trees.getElement(path);

		// populate required method information to model
		FieldInfoDataSetter.populateFieldInfo(classInfoStack.get(classInfoStack.size() - 1), variableTree, e, path, trees);
		return super.visitVariable(variableTree, trees);
	}

	/**
	 * Returns the Java class model which holds the details of java source
	 * 
	 * @return clazzInfo Java class model
	 */
	public IList<JavaClassInfo> getClassInfos()
	{
		return collectedClassInfos;
	}
}
