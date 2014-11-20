package demo.codeanalyzer.common.model;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;

/**
 * Stores details of fields in the java code
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class FieldInfo extends BaseJavaClassModelInfo implements Field
{

	private ClassFile owningClass;

	protected ExpressionTree initializer;
	private Type fieldType;

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
	public Type getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(Type fieldType)
	{
		this.fieldType = fieldType;
	}
}
