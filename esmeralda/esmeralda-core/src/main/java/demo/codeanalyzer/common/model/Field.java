package demo.codeanalyzer.common.model;

import de.osthus.ambeth.collections.IList;

/**
 * Stores details of fields in the java code
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public interface Field extends BaseJavaClassModel
{
	void addFieldType(String fieldType);

	/**
	 * @return the {@link ClassFile} this method belongs to.
	 */
	ClassFile getOwningClass();

	IList<String> getFieldTypes();
}
