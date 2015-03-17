package demo.codeanalyzer.common.model;

import com.sun.source.tree.ExpressionTree;

/**
 * Stores details of fields in the java code
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class FieldInfo extends BaseJavaClassModelInfo implements Field
{

	private ClassFile owningClass;

	protected ExpressionTree initializer;
	private String fieldType;

	public ExpressionTree getInitializer()
	{
		return initializer;
	}

	public void setInitializer(ExpressionTree initializer)
	{
		this.initializer = initializer;
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

	@Override
	public String getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(String fieldType)
	{
		this.fieldType = fieldType;
	}

	@Override
	public String toString()
	{
		return getFieldType() + " " + getOwningClass().getFqName() + "." + getName();
	}
}
