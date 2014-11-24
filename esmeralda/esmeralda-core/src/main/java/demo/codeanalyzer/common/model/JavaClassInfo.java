package demo.codeanalyzer.common.model;

import java.util.List;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.VariableElement;

import com.sun.source.util.TreePath;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IVariable;

/**
 * Stores basic attributes of a java class
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class JavaClassInfo extends BaseJavaClassModelInfo implements ClassFile
{
	private String nameOfSuperClass;
	private String packageName;
	private String nestingKind;
	private boolean isInterface;
	private boolean isAnnotation;
	private boolean isEnum;
	private boolean isSerializable;
	private boolean isArray;
	private JavaSourceTreeInfo sourceTreeInfo;
	private ArrayList<Method> methods = new ArrayList<Method>();
	private ArrayList<Method> constructors = new ArrayList<Method>();
	private ArrayList<String> nameOfInterfaces = new ArrayList<String>();
	private ArrayList<String> classTypes = new ArrayList<String>();
	private LinkedHashMap<String, Field> fields = new LinkedHashMap<String, Field>();
	private LinkedHashSet<IVariable> allUsedVariables = null;
	private boolean isAnonymous;

	public IConversionContext context;

	private TreePath treePath;

	public JavaClassInfo(IConversionContext context)
	{
		this.context = context;
	}

	public void setArray(boolean isArray)
	{
		this.isArray = isArray;
	}

	public boolean isArray()
	{
		return isArray;
	}

	public void addField(Field field)
	{
		fields.put(field.getName(), field);
	}

	@Override
	public Field getField(String fieldName)
	{
		Field field = fields.get(fieldName);
		if (field == null)
		{
			String nameOfSuperClass = getNameOfSuperClass();
			if (nameOfSuperClass != null)
			{
				return context.resolveClassInfo(nameOfSuperClass).getField(fieldName);
			}
			throw new IllegalArgumentException("No field found: " + getPackageName() + "." + getName() + "." + fieldName);
		}
		return field;
	}

	@Override
	public IList<Field> getFields()
	{
		return fields.values();
	}

	public void setNameOfSuperClass(String superClass)
	{
		nameOfSuperClass = superClass;
	}

	@Override
	public String getNameOfSuperClass()
	{
		return nameOfSuperClass;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public String getNestingKind()
	{
		return nestingKind;
	}

	public void setNestingKind(String nestingKind)
	{
		this.nestingKind = nestingKind;
	}

	public void setSourceTreeInfo(JavaSourceTreeInfo sourceTreeInfo)
	{
		this.sourceTreeInfo = sourceTreeInfo;
	}

	@Override
	public JavaSourceTreeInfo getSourceTreeInfo()
	{
		return sourceTreeInfo;
	}

	@Override
	public IList<Method> getMethods()
	{
		return methods;
	}

	@Override
	public boolean hasMethodWithIdenticalSignature(Method method)
	{
		for (Method existingMethod : getMethods())
		{
			if (!existingMethod.getName().equals(method.getName()))
			{
				// not the same name
				continue;
			}
			IList<VariableElement> existingParameters = existingMethod.getParameters();
			IList<VariableElement> parameters = method.getParameters();

			if (existingParameters.size() != parameters.size())
			{
				// not the same parameter count
				continue;
			}
			boolean parametersIdentical = true;
			for (int a = existingParameters.size(); a-- > 0;)
			{
				VariableElement existingParameter = existingParameters.get(a);
				VariableElement parameter = parameters.get(a);
				if (!existingParameter.asType().equals(parameter.asType()))
				{
					parametersIdentical = false;
					break;
				}
			}
			if (parametersIdentical)
			{
				return true;
			}
		}
		return false;
	}

	public void addMethod(Method method)
	{
		methods.add(method);
	}

	@Override
	public IList<Method> getConstructors()
	{
		return constructors;
	}

	public void addConstructor(Method method)
	{
		constructors.add(method);
	}

	@Override
	public IList<String> getNameOfInterfaces()
	{
		return nameOfInterfaces;
	}

	public void addNameOfInterface(String interfaceDet)
	{
		nameOfInterfaces.add(interfaceDet);
	}

	@Override
	public IList<String> getClassTypes()
	{
		return classTypes;
	}

	@Override
	public void addClassType(String classType)
	{
		classTypes.add(classType);
	}

	@Override
	public boolean isInterface()
	{
		return isInterface;
	}

	public void setIsInterface(boolean interfaceFlag)
	{
		isInterface = interfaceFlag;
	}

	@Override
	public boolean isEnum()
	{
		return isEnum;
	}

	public void setIsEnum(boolean isEnum)
	{
		this.isEnum = isEnum;
	}

	@Override
	public boolean isAnnotation()
	{
		return isAnnotation;
	}

	public void setIsAnnotation(boolean isAnnotation)
	{
		this.isAnnotation = isAnnotation;
	}

	public void setSerializable(boolean isSerializable)
	{
		this.isSerializable = isSerializable;
	}

	@Override
	public boolean isSerializable()
	{
		return isSerializable;
	}

	@Override
	public boolean isTopLevelClass()
	{
		return nestingKind.equals(NestingKind.TOP_LEVEL.toString());
	}

	@Override
	public String getFqName()
	{
		return getPackageName() + "." + getName();
	}

	@Override
	public String toString()
	{
		return getFqName();
	}

	public boolean isAnonymous()
	{
		return isAnonymous;
	}

	public void setIsAnonymous(boolean isAnonymous)
	{
		this.isAnonymous = isAnonymous;
	}

	public void addUsedVariables(List<IVariable> allUsedVariables)
	{
		if (this.allUsedVariables == null)
		{
			this.allUsedVariables = new LinkedHashSet<IVariable>();
		}
		this.allUsedVariables.addAll(allUsedVariables);
	}

	public IList<IVariable> getAllUsedVariables()
	{
		if (allUsedVariables == null)
		{
			return EmptyList.getInstance();
		}
		return allUsedVariables.toList();
	}

	public TreePath getTreePath()
	{
		return treePath;
	}

	public void setTreePath(TreePath treePath)
	{
		this.treePath = treePath;
	}
}
