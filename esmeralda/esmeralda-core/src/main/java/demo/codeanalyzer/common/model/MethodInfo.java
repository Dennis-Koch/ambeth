package demo.codeanalyzer.common.model;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;

/**
 * Stores method information of java class
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class MethodInfo extends BaseJavaClassModelInfo implements Method
{
	private ClassFile owningClass;
	private MethodTree methodTree;
	private ArrayList<VariableElement> parameters = new ArrayList<VariableElement>();
	private String returnType;
	ArrayList<String> exceptions = new ArrayList<String>();
	private ArrayList<Integer> parameterIndexToEraseGenericType = new ArrayList<Integer>();
	private ArrayList<Integer> parameterIndexToDelete = new ArrayList<Integer>();
	private TreePath path;

	@Override
	public boolean isConstructor()
	{
		return getName().equals(getOwningClass().getName());
	}

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
	public MethodTree getMethodTree()
	{
		return methodTree;
	}

	public void setMethodTree(MethodTree methodTree)
	{
		this.methodTree = methodTree;
	}

	@Override
	public IList<VariableElement> getParameters()
	{
		return parameters;
	}

	public void addParameters(VariableElement parameter)
	{
		parameters.add(parameter);
	}

	@Override
	public String getReturnType()
	{
		return returnType;
	}

	public void setReturnType(String returnType)
	{
		this.returnType = returnType;
	}

	@Override
	public IList<String> getExceptions()
	{
		return exceptions;
	}

	public void addException(String exception)
	{
		exceptions.add(exception);
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Method Name: " + getName());
		buffer.append("\n");
		if (parameters.size() <= 0)
		{
			buffer.append("No parameters defined ");
			buffer.append("\n");
		}
		for (Object param : parameters)
		{
			buffer.append(param.toString());
			buffer.append(", ");
		}
		return buffer.toString();
	}

	@Override
	public IList<Integer> getParameterIndexToEraseGenericType()
	{
		return parameterIndexToEraseGenericType;
	}

	public void addParameterIndexToEraseGenericType(int parameterIndex)
	{
		parameterIndexToEraseGenericType.add(parameterIndex);
	}

	@Override
	public IList<Integer> getParameterIndexToDelete()
	{
		return parameterIndexToDelete;
	}

	public void addParameterIndexToDelete(int parameterIndex)
	{
		parameterIndexToDelete.add(parameterIndex);
	}

	public void setPath(TreePath path)
	{
		this.path = path;
	}

	@Override
	public TreePath getPath()
	{
		return path;
	}
}
