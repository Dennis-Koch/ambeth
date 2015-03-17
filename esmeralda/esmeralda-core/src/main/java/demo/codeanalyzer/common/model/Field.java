package demo.codeanalyzer.common.model;


/**
 * Stores details of fields in the java code
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public interface Field extends BaseJavaClassModel
{
	/**
	 * @return the {@link ClassFile} this method belongs to.
	 */
	ClassFile getOwningClass();

	String getFieldType();
}
