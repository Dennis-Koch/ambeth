package de.osthus.esmeralda.handler.js;

import javax.lang.model.element.VariableElement;

import de.osthus.ambeth.collections.IList;
import de.osthus.esmeralda.ILanguageHelper;
import demo.codeanalyzer.common.model.BaseJavaClassModel;

public interface IJsHelper extends ILanguageHelper
{
	boolean newLineIndentWithCommaIfFalse(boolean value);

	void writeMetadata(BaseJavaClassModel model);

	void writeMetadata(BaseJavaClassModel model, String access);

	String convertType(String typeName, boolean direct);

	String createOverloadedMethodNamePostfix(IList<VariableElement> parameters);
}
