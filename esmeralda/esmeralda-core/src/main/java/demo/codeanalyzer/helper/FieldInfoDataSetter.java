package demo.codeanalyzer.helper;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;

import demo.codeanalyzer.common.model.AnnotationInfo;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.LocationInfo;

/**
 * Helper class to set the properties of fields to the java class model
 * 
 * @author Seema Richard (Seema.Richard@ust-global.com)
 * @author Deepa Sobhana (Deepa.Sobhana@ust-global.com)
 */
public class FieldInfoDataSetter
{

	public static void populateFieldInfo(JavaClassInfo clazzInfo, VariableTree variableTree, Element e, TreePath path, Trees trees)
	{
		if (e == null)
		{
			return;
		}
		Element parent = e.getEnclosingElement();
		if (!(parent instanceof ClassSymbol))
		{
			return;
		}
		FieldInfo fieldInfo = new FieldInfo();
		String fieldName = variableTree.getName().toString();
		fieldInfo.setName(fieldName);

		fieldInfo.setOwningClass(clazzInfo);
		fieldInfo.setFieldType(((Symbol) e).type.toString());
		// Set modifier details
		Set<Modifier> modifiers = variableTree.getModifiers().getFlags();
		for (Modifier modifier : modifiers)
		{
			DataSetterUtil.setModifiers(modifier.toString(), fieldInfo);
		}
		fieldInfo.setInitializer(variableTree.getInitializer());
		List<? extends AnnotationMirror> annotations = e.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annotations)
		{
			AnnotationInfo annotationInfo = new AnnotationInfo(annotationMirror);
			fieldInfo.addAnnotation(annotationInfo);
		}
		clazzInfo.addField(fieldInfo);

		// Set Temp LocationInfo
		LocationInfo locationInfo = DataSetterUtil.getLocationInfo(trees, path, variableTree);
		fieldInfo.setLocationInfo(locationInfo);
	}
}
