package demo.codeanalyzer.common.model;

import javax.lang.model.element.NestingKind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;

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
	private boolean isSerializable;
	private JavaSourceTreeInfo sourceTreeInfo;
	private ArrayList<Method> methods = new ArrayList<Method>();
	private ArrayList<Method> constructors = new ArrayList<Method>();
	private ArrayList<String> nameOfInterfaces = new ArrayList<String>();
	private ArrayList<String> classTypes = new ArrayList<String>();
	private LinkedHashMap<String, Field> fields = new LinkedHashMap<String, Field>();

	public void addField(Field field)
	{
		fields.put(field.getName(), field);
	}

	@Override
	public Field getField(String fieldName)
	{
		return fields.get(fieldName);
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
}
