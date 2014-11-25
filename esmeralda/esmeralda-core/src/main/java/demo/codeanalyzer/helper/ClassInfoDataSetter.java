package demo.codeanalyzer.helper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;

import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import demo.codeanalyzer.common.model.AnnotationInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.JavaSourceTreeInfo;
import demo.codeanalyzer.common.model.LocationInfo;

/**
 * Helper class to set the properties of a java class to the java class model
 * 
 * @author Seema Richard (Seema.Richard@ust-global.com)
 * @author Deepa Sobhana (Deepa.Sobhana@ust-global.com)
 */
public class ClassInfoDataSetter
{
	public static final Pattern fqPattern = Pattern.compile("(.+)\\.([^\\.]+)");
	private static final String JCClassDecl = null;

	/**
	 * Set the attributes of the currently visiting class to the java class model
	 * 
	 * @param clazzInfo
	 *            The java class model
	 * @param classTree
	 *            Curently visiting class tree
	 * @param path
	 *            tree path
	 * @param trees
	 *            trees
	 */
	public static void populateClassInfo(JavaClassInfo clazzInfo, ClassTree classTree, TreePath path, Trees trees)
	{
		clazzInfo.setTreePath(path);
		TypeElement e = (TypeElement) trees.getElement(path);

		if (e == null)
		{
			return;
		}
		String anonymousFqName = NewClassExpressionHandler.findFqAnonymousName(path);
		String fqName = NewClassExpressionHandler.getFqNameFromAnonymousName(anonymousFqName);
		if (!fqName.equals(anonymousFqName))
		{
			clazzInfo.setIsAnonymous(true);
		}
		// Set qualified class name
		Matcher fqMatcher = fqPattern.matcher(fqName);
		if (!fqMatcher.matches())
		{
			throw new IllegalStateException("Must never happen");
		}
		clazzInfo.setPackageName(fqMatcher.group(1));
		clazzInfo.setName(fqMatcher.group(2));

		// Set Nesting kind
		clazzInfo.setNestingKind(e.getNestingKind().toString());
		JCModifiers modifiers = ((JCClassDecl) classTree).getModifiers();

		// Set modifier details
		for (Modifier modifier : modifiers.getFlags())
		{
			DataSetterUtil.setModifiers(modifier.toString(), clazzInfo);
		}
		String modifiersString = classTree.toString();
		int indexOfSimpleName = modifiersString.indexOf(((JCClassDecl) classTree).getSimpleName().toString());
		modifiersString = modifiersString.substring(0, indexOfSimpleName);
		if (modifiersString.contains(" interface")) // space intended to not match on '@interface' which are annotations
		{
			clazzInfo.setIsInterface(true);
		}
		if (modifiersString.contains("@interface"))
		{
			clazzInfo.setIsAnnotation(true);
		}
		if (modifiersString.contains(" enum"))
		{
			clazzInfo.setIsEnum(true);
		}
		String superClass = e.getSuperclass().toString();
		if (superClass != null && superClass.startsWith(Enum.class.getName() + "<"))
		{
			clazzInfo.setIsEnum(true);
		}
		// Set extending class info
		if (superClass != null && !"<none>".equals(superClass))
		{
			clazzInfo.setNameOfSuperClass(superClass);
		}
		// Set implementing interface details
		for (TypeMirror mirror : e.getInterfaces())
		{
			clazzInfo.addNameOfInterface(mirror.toString());
		}

		List<? extends AnnotationMirror> annotations = e.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotations)
		{
			AnnotationInfo annotationInfo = new AnnotationInfo(annotationMirror);
			clazzInfo.addAnnotation(annotationInfo);
		}

		LocationInfo locationInfo = DataSetterUtil.getLocationInfo(trees, path, classTree);
		clazzInfo.setLocationInfo(locationInfo);

		// setJavaTreeDetails
		JavaSourceTreeInfo treeInfo = new JavaSourceTreeInfo();
		TreePath tp = trees.getPath(e);
		treeInfo.setCompileTree(tp.getCompilationUnit());
		treeInfo.setSourcePos(trees.getSourcePositions());
		clazzInfo.setSourceTreeInfo(treeInfo);
	}
}
