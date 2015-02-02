package de.osthus.esmeralda.handler.js;

import javax.lang.model.element.VariableElement;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public interface IJsHelper extends ILanguageHelper
{
	@Override
	JsSpecific getLanguageSpecific();

	boolean newLineIndentWithCommaIfFalse(boolean value);

	void writeMetadata(BaseJavaClassModel model);

	/**
	 * Writes the meta data for overload hub methods.
	 * 
	 * @param methodName
	 *            Name of the original method
	 * @param returnType
	 *            Return type of the hub method
	 * @param methods
	 *            List of overloads of this method
	 */
	void writeMetadata(String methodName, String returnType, ArrayList<Method> methods);

	String removeGenerics(String name);

	String convertType(String typeName, boolean direct);

	String createOverloadedMethodNamePostfix(IList<VariableElement> parameters);

	JavaClassInfo findClassInHierarchy(String className, JavaClassInfo current, IConversionContext context);

	Field findFieldInHierarchy(String fieldName, JavaClassInfo current, IConversionContext context);
}
