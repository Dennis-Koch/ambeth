package demo.codeanalyzer.common.model;

import java.util.List;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.VariableElement;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.TreePath;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.handler.ITransformedMethod;
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
	private JavaSourceTreeInfo sourceTreeInfo;
	private ArrayList<Method> methods = new ArrayList<Method>();
	private ArrayList<Method> constructors = new ArrayList<Method>();
	private ArrayList<String> nameOfInterfaces = new ArrayList<String>();
	private ArrayList<String> classTypes = new ArrayList<String>();
	private LinkedHashMap<String, Field> fields = new LinkedHashMap<String, Field>();
	private LinkedHashSet<IVariable> allUsedVariables = null;
	private boolean isAnonymous;

	public IClassInfoManager classInfoManager;

	private JavaClassInfo extendsFrom;

	private TreePath treePath;
	private ClassTree classTree;
	private JavaClassInfo[] typeArguments;
	private String nonGenericName;
	private String nonGenericFqName;
	private String fqName;
	private JavaClassInfo componentType;

	public JavaClassInfo(IClassInfoManager classInfoManager)
	{
		this.classInfoManager = classInfoManager;
	}

	public boolean isArray()
	{
		return componentType != null;
	}

	public void addField(Field field)
	{
		fields.put(field.getName(), field);
	}

	@Override
	public Field getField(String fieldName)
	{
		return getField(fieldName, false);
	}

	@Override
	public Field getField(String fieldName, boolean tryOnly)
	{
		Field field = fields.get(fieldName);
		if (field != null)
		{
			return field;
		}
		if ("this".equals(fieldName))
		{
			FieldInfo thisField = new FieldInfo();
			thisField.setOwningClass(this);
			thisField.setName(fieldName);
			thisField.setFieldType(getFqName());
			thisField.setPublicFlag(true);
			addField(thisField);
			return thisField;
		}
		JavaClassInfo extendsFrom = getExtendsFrom();
		if (extendsFrom != null)
		{
			field = extendsFrom.getField(fieldName, tryOnly);
			if (field != null)
			{
				return field;
			}
		}
		String nameOfSuperClass = getNameOfSuperClass();
		if (nameOfSuperClass != null)
		{
			JavaClassInfo superClassInfo = classInfoManager.resolveClassInfo(nameOfSuperClass);
			if ("super".equals(fieldName))
			{
				FieldInfo thisField = new FieldInfo();
				thisField.setOwningClass(this);
				thisField.setName(fieldName);
				thisField.setFieldType(superClassInfo.getFqName());
				thisField.setPublicFlag(true);
				addField(thisField);
				return thisField;
			}
			if (superClassInfo != null)
			{
				Field fieldFromSuper = superClassInfo.getField(fieldName, true);
				if (fieldFromSuper != null)
				{
					return fieldFromSuper;
				}
			}
		}
		for (String interfaceName : getNameOfInterfaces())
		{
			JavaClassInfo interfaceCI = classInfoManager.resolveClassInfo(interfaceName);
			if (interfaceCI == null)
			{
				continue;
			}
			Field fieldFromInterface = interfaceCI.getField(fieldName, true);
			if (fieldFromInterface != null)
			{
				return fieldFromInterface;
			}
		}
		if (tryOnly)
		{
			return null;
		}
		throw new IllegalArgumentException("No field found: " + getFqName() + "." + fieldName);
	}

	@Override
	public IList<Field> getFields()
	{
		return fields.values();
	}

	public void setNameOfSuperClass(String superClass)
	{
		if (superClass != null)
		{
			superClass = superClass.intern();
		}
		nameOfSuperClass = superClass;
	}

	@Override
	public String getNameOfSuperClass()
	{
		return nameOfSuperClass;
	}

	public void setPackageName(String packageName)
	{
		if (packageName != null)
		{
			packageName = packageName.intern();
		}
		this.packageName = packageName;
		updateFqName();
		updateNonGenericFqName();
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
				if (!existingParameter.asType().toString().equals(parameter.asType().toString()))
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

	public boolean hasMethodWithIdenticalSignature(ITransformedMethod method)
	{
		for (Method existingMethod : getMethods())
		{
			if (!existingMethod.getName().equals(method.getName()))
			{
				// not the same name
				continue;
			}
			IList<VariableElement> existingParameters = existingMethod.getParameters();
			String[] parameters = method.getArgumentTypes();

			if (existingParameters.size() != parameters.length)
			{
				// not the same parameter count
				continue;
			}
			boolean parametersIdentical = true;
			for (int a = existingParameters.size(); a-- > 0;)
			{
				VariableElement existingParameter = existingParameters.get(a);
				String parameter = parameters[a];
				if (!existingParameter.asType().toString().equals(parameter))
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
		return fqName;
	}

	private void updateFqName()
	{
		if (getPackageName() != null)
		{
			fqName = (getPackageName() + "." + getName()).intern();
		}
		else
		{
			fqName = getName();
		}
	}

	@Override
	public String toString()
	{
		String fqName = getFqName();

		JavaClassInfo extendsFrom = getExtendsFrom();
		if (extendsFrom == null)
		{
			return fqName;
		}
		return fqName + " extends " + extendsFrom.getFqName();
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

	public ClassTree getClassTree()
	{
		return classTree;
	}

	public void setClassTree(ClassTree classTree)
	{
		this.classTree = classTree;
	}

	public JavaClassInfo[] getTypeArguments()
	{
		return typeArguments;
	}

	public void setTypeArguments(JavaClassInfo[] typeArguments)
	{
		this.typeArguments = typeArguments;
	}

	public JavaClassInfo getExtendsFrom()
	{
		return extendsFrom;
	}

	public void setExtendsFrom(JavaClassInfo extendsFrom)
	{
		this.extendsFrom = extendsFrom;
	}

	@Override
	public void setName(String name)
	{
		if (name != null)
		{
			name = name.intern();
		}
		super.setName(name);
		updateFqName();
		setNonGenericName(getName());
	}

	@Override
	public String getNonGenericName()
	{
		return nonGenericName;
	}

	public void setNonGenericName(String nonGenericName)
	{
		if (nonGenericName != null)
		{
			nonGenericName = nonGenericName.intern();
		}
		this.nonGenericName = nonGenericName;
		updateNonGenericFqName();
	}

	public String getNonGenericFqName()
	{
		return nonGenericFqName;
	}

	private void updateNonGenericFqName()
	{
		if (getPackageName() != null)
		{
			nonGenericFqName = (getPackageName() + "." + getNonGenericName()).intern();
		}
		else
		{
			nonGenericFqName = getNonGenericName();
		}
	}

	public JavaClassInfo getComponentType()
	{
		return componentType;
	}

	public void setComponentType(JavaClassInfo componentType)
	{
		this.componentType = componentType;
	}

	public void fillTypeArgumentsIfNecessary()
	{
		if (getTypeArguments() != null)
		{
			return;
		}
		ClassTree classTree = getClassTree();
		if (classTree == null)
		{
			return;
		}
		List<? extends TypeParameterTree> typeParameters = classTree.getTypeParameters();
		typeArguments = new JavaClassInfo[typeParameters.size()];
		for (int a = typeParameters.size(); a-- > 0;)
		{
			TypeParameterTree typeParameter = typeParameters.get(a);
			typeArguments[a] = classInfoManager.resolveClassInfo(typeParameter.getName().toString());
		}
	}
}
