package demo.codeanalyzer.helper;

import static demo.codeanalyzer.common.util.CodeAnalyzerConstants.SERIALIZABLE_PKG;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

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
		TypeElement e = (TypeElement) trees.getElement(path);

		if (e == null)
		{
			return;
		}
		String fqName = e.getQualifiedName().toString();
		if (fqName.length() == 0)
		{
			// skip anonymous classes
			return;
		}
		if (!(e.getEnclosingElement() instanceof PackageElement))
		{
			// skip internal classes
			return;
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

		// Set modifier details
		for (Modifier modifier : e.getModifiers())
		{
			DataSetterUtil.setModifiers(modifier.toString(), clazzInfo);
		}

		// Set extending class info
		clazzInfo.setNameOfSuperClass(e.getSuperclass().toString());

		// Set implementing interface details
		for (TypeMirror mirror : e.getInterfaces())
		{
			clazzInfo.addNameOfInterface(mirror.toString());
		}
		// Set serializable property
		try
		{
			Class serializable = Class.forName(SERIALIZABLE_PKG);
			Class thisClass = Class.forName(e.getQualifiedName().toString());
			if (serializable.isAssignableFrom(thisClass))
			{
				clazzInfo.setSerializable(true);
			}
			else
			{
				clazzInfo.setSerializable(false);
			}

		}
		catch (ClassNotFoundException ex)
		{
			clazzInfo.setSerializable(false);
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
