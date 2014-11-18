package demo.codeanalyzer.common.model;

import com.sun.source.tree.ExpressionTree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;

/**
 * Stores details of fields in the java code
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class FieldInfo extends BaseJavaClassModelInfo implements Field
{

	private ClassFile owningClass;
	private ArrayList<String> fieldTypes = new ArrayList<String>();

	protected ExpressionTree initializer;

	public ExpressionTree getInitializer()
	{
		return initializer;
	}

	public void setInitializer(ExpressionTree initializer)
	{
		this.initializer = initializer;
	}

	@Override
	public void addFieldType(String fieldType)
	{
		fieldTypes.add(fieldType);
	}

	@Override
	public IList<String> getFieldTypes()
	{
		return fieldTypes;
	}

	/**
	 * @return the {@link ClassFile} this method belongs to.
	 */
	@Override
	public ClassFile getOwningClass()
	{
		return owningClass;
	}

	public void setOwningClass(ClassFile owningClass)
	{
		this.owningClass = owningClass;
	}
}
