package demo.codeanalyzer.common.model;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.TreePath;

import de.osthus.ambeth.collections.IList;

/**
 * Stores method information of java class
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public interface Method extends BaseJavaClassModel
{
	/**
	 * @return the {@link ClassFile} this method belongs to.
	 */
	ClassFile getOwningClass();

	MethodTree getMethodTree();

	TreePath getPath();

	/**
	 * @return the internal names of the method's exception classes. May be null.
	 */
	IList<String> getExceptions();

	IList<VariableElement> getParameters();

	String getReturnType();

	IList<Integer> getParameterIndexToEraseGenericType();

	IList<Integer> getParameterIndexToDelete();

	TypeParameterTree[] getTypeParameters();

	boolean isConstructor();
}
