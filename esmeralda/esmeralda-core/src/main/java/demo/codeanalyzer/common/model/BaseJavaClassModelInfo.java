package demo.codeanalyzer.common.model;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;

/**
 * Stores common attributes of a java class
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class BaseJavaClassModelInfo implements BaseJavaClassModel
{

	private int moduleType;

	private String name = null;

	private ArrayList<Annotation> annontations = new ArrayList<Annotation>();

	private Location locationInfo;

	private boolean publicFlag;

	private boolean privateFlag;

	private boolean protectedFlag;

	private boolean finalFlag;

	private boolean abstractFlag;

	private boolean nativeFlag;

	private boolean staticFlag;

	public void setAbstractFlag(boolean abstractFlag)
	{
		this.abstractFlag = abstractFlag;
	}

	@Override
	public boolean isAbstract()
	{
		return abstractFlag;
	}

	public void setFinalFlag(boolean finalFlag)
	{
		this.finalFlag = finalFlag;
	}

	@Override
	public boolean isFinal()
	{
		return finalFlag;
	}

	public void setNativeFlag(boolean nativeFlag)
	{
		this.nativeFlag = nativeFlag;
	}

	@Override
	public boolean isNative()
	{
		return nativeFlag;
	}

	public void setPrivateFlag(boolean privateFlag)
	{
		this.privateFlag = privateFlag;
	}

	@Override
	public boolean isPrivate()
	{
		return privateFlag;
	}

	public void setProtectedFlag(boolean protectedFlag)
	{
		this.protectedFlag = protectedFlag;
	}

	@Override
	public boolean isProtected()
	{
		return protectedFlag;
	}

	public void setPublicFlag(boolean publicFlag)
	{
		this.publicFlag = publicFlag;
	}

	@Override
	public boolean isPublic()
	{
		return publicFlag;
	}

	public void setStaticFlag(boolean staticFlag)
	{
		this.staticFlag = staticFlag;
	}

	@Override
	public boolean isStatic()
	{
		return staticFlag;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name != null ? name.intern() : name;
	}

	@Override
	public IList<Annotation> getAnnotations()
	{
		return annontations;
	}

	public void addAnnotation(Annotation anno)
	{
		annontations.add(anno);
	}

	public int getModuleType()
	{
		return moduleType;
	}

	public void setModuleType(int moduleType)
	{
		this.moduleType = moduleType;
	}

	@Override
	public Location getLocationInfo()
	{
		return locationInfo;
	}

	public void setLocationInfo(Location locationInfo)
	{
		this.locationInfo = locationInfo;
	}

}
