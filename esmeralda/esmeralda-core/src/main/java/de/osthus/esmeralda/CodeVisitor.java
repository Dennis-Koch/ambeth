package de.osthus.esmeralda;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import demo.codeanalyzer.common.model.AnnotationInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.JavaSourceTreeInfo;
import demo.codeanalyzer.common.model.LocationInfo;
import demo.codeanalyzer.common.model.MethodInfo;
import demo.codeanalyzer.helper.DataSetterUtil;
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

	protected static final ArrayList<MethodInfo> methodInfoStack = new ArrayList<MethodInfo>();

	public static final Pattern fqPattern = Pattern.compile("(.+)\\.([^\\.]+)");

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
	protected IASTHelper astHelper;

	@Autowired
	protected IClassInfoManager classInfoManager;

	@Autowired
	protected MethodInfoDataSetter methodInfoDataSetter;

	@Override
	public Object visitClass(ClassTree classTree, Trees trees)
	{
		JavaClassInfo classInfo = new JavaClassInfo(classInfoManager);
		classInfoStack.add(classInfo);
		try
		{
			TreePath path = getCurrentPath();
			// populate required class information to model
			populateClassInfo(classInfo, classTree, path, trees);

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
		MethodInfo methodInfo = methodInfoDataSetter.populateMethodInfo(classInfoStack.get(classInfoStack.size() - 1), methodTree, path, trees);

		methodInfoStack.add(methodInfo);
		try
		{
			return super.visitMethod(methodTree, trees);
		}
		finally
		{
			methodInfoStack.popLastElement();
		}
	}

	@Override
	public Object visitBlock(BlockTree blockTree, Trees trees)
	{
		if (methodInfoStack.size() == 0)
		{
			return super.visitBlock(blockTree, trees);
		}
		MethodInfo currentMethod = methodInfoStack.get(methodInfoStack.size() - 1);

		// currentMethod.pushBlock(blockTree);
		try
		{
			return super.visitBlock(blockTree, trees);
		}
		finally
		{
			// currentMethod.popBlock(blockTree);
		}
	}

	@Override
	public Object visitEmptyStatement(EmptyStatementTree arg0, Trees arg1)
	{
		return super.visitEmptyStatement(arg0, arg1);
	}

	@Override
	public Object visitArrayAccess(ArrayAccessTree arg0, Trees arg1)
	{
		return super.visitArrayAccess(arg0, arg1);
	}

	@Override
	public Object visitIdentifier(IdentifierTree identifierTree, Trees trees)
	{
		return super.visitIdentifier(identifierTree, trees);
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

	/**
	 * Set the attributes of the currently visiting class to the java class model
	 * 
	 * @param classInfo
	 *            The java class model
	 * @param classTree
	 *            Curently visiting class tree
	 * @param path
	 *            tree path
	 * @param trees
	 *            trees
	 */
	public void populateClassInfo(JavaClassInfo classInfo, ClassTree classTree, TreePath path, Trees trees)
	{
		classInfo.setTreePath(path);
		TypeElement e = (TypeElement) trees.getElement(path);

		if (e == null)
		{
			return;
		}
		String fqName = NewClassExpressionHandler.getFqName((JCClassDecl) classTree);
		if (classTree.getSimpleName().contentEquals(""))
		{
			classInfo.setIsAnonymous(true);
		}
		// Set qualified class name
		String[] parsedGenericType = astHelper.parseGenericType(fqName);
		Matcher fqMatcher = fqPattern.matcher(parsedGenericType[0]);
		if (!fqMatcher.matches())
		{
			throw new IllegalStateException("Must never happen");
		}
		classInfo.setClassTree(classTree);
		classInfo.setPackageName(fqMatcher.group(1));
		if (parsedGenericType.length == 2)
		{
			classInfo.setName(fqMatcher.group(2) + "<" + parsedGenericType[1] + ">");
			classInfo.setNonGenericName(fqMatcher.group(2));
		}
		else
		{
			classInfo.setName(fqMatcher.group(2));
		}
		// Set Nesting kind
		classInfo.setNestingKind(e.getNestingKind().toString());
		JCModifiers modifiers = ((JCClassDecl) classTree).getModifiers();

		// Set modifier details
		for (Modifier modifier : modifiers.getFlags())
		{
			DataSetterUtil.setModifiers(modifier.toString(), classInfo);
		}
		String modifiersString = classTree.toString();
		int indexOfSimpleName = modifiersString.indexOf(((JCClassDecl) classTree).getSimpleName().toString());
		modifiersString = modifiersString.substring(0, indexOfSimpleName);
		if (modifiersString.contains(" interface")) // space intended to not match on '@interface' which are annotations
		{
			classInfo.setIsInterface(true);
		}
		if (modifiersString.contains("@interface"))
		{
			classInfo.setIsAnnotation(true);
		}
		if (modifiersString.contains(" enum"))
		{
			classInfo.setIsEnum(true);
		}
		String superClass = e.getSuperclass().toString();
		if (superClass != null && superClass.startsWith(Enum.class.getName() + "<"))
		{
			classInfo.setIsEnum(true);
		}
		// Set extending class info
		if (superClass == null || "<none>".equals(superClass))
		{
			superClass = Object.class.getName();
		}
		classInfo.setNameOfSuperClass(superClass);
		// Set implementing interface details
		for (TypeMirror mirror : e.getInterfaces())
		{
			classInfo.addNameOfInterface(mirror.toString());
		}

		List<? extends AnnotationMirror> annotations = e.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotations)
		{
			AnnotationInfo annotationInfo = new AnnotationInfo(annotationMirror);
			classInfo.addAnnotation(annotationInfo);
		}

		LocationInfo locationInfo = DataSetterUtil.getLocationInfo(trees, path, classTree);
		classInfo.setLocationInfo(locationInfo);

		// setJavaTreeDetails
		JavaSourceTreeInfo treeInfo = new JavaSourceTreeInfo();
		TreePath tp = trees.getPath(e);
		treeInfo.setCompileTree(tp.getCompilationUnit());
		treeInfo.setSourcePos(trees.getSourcePositions());
		classInfo.setSourceTreeInfo(treeInfo);
	}
}
