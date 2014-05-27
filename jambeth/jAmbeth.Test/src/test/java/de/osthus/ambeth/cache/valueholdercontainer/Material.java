package de.osthus.ambeth.cache.valueholdercontainer;

import java.util.List;

import de.osthus.ambeth.annotation.ParentChild;

public class Material extends AbstractMaterial
{
	// private List<PropertyChangedEventHandler> propertyChangedEventHandlers;
	private long id;

	private Object id2;

	private int version;

	private String name;

	private List<String> names;

	private MaterialType childMatType;

	private List<MaterialType> childMatTypes;

	private EmbeddedMaterial embMat;

	private EmbeddedMaterial embMat3;

	@ParentChild
	public MaterialType getChildMatType()
	{
		return childMatType;
	}

	public void setChildMatType(MaterialType childMatType)
	{
		this.childMatType = childMatType;
	}

	@ParentChild
	public List<MaterialType> getChildMatTypes()
	{
		return childMatTypes;
	}

	public void setChildMatTypes(List<MaterialType> childMatTypes)
	{
		this.childMatTypes = childMatTypes;
	}

	// public List<PropertyChangedEventHandler> PropertyChangedEventHandlers
	// {
	// get
	// {
	// if (propertyChangedEventHandlers == null)
	// {
	// propertyChangedEventHandlers = new List<PropertyChangedEventHandler>();
	// }
	// return propertyChangedEventHandlers;
	// }
	// }

	// public override IList<MaterialType> Types
	// {
	// get
	// {
	// return base.Types;
	// }
	// set
	// {
	// base.Types = value;
	// }
	// }

	// public virtual bool ToBeDeleted { get; set; }

	// public virtual bool ToBeUpdated { get; set; }

	// private Object GetId()
	// {
	// return Id2;
	// }

	// public virtual bool ToBeCreated
	// {
	// get
	// {
	// return GetId() == null;
	// }
	// }

	// public virtual bool HasPendingChanges
	// {
	// get
	// {
	// return ToBeUpdated || ToBeCreated || ToBeDeleted;
	// }
	// }

	// public Type GetBaseType()
	// {
	// return typeof(AbstractMaterial);
	// }

	// public override bool Equals(Object obj)
	// {
	// if (Object.ReferenceEquals(obj, this))
	// {
	// return true;
	// }
	// if (!(obj is IEntityEquals))
	// {
	// return false;
	// }
	// Object id = GetId();
	// if (id == null)
	// {
	// // Null id can never be equal with something other than itself
	// return false;
	// }
	// IEntityEquals other = (IEntityEquals) obj;
	// return Object.Equals(id, other.GetId()) && Object.Equals(GetBaseType(), other.GetBaseType());
	// }

	// public override int GetHashCode()
	// {
	// Object id = GetId();
	// int hash = GetBaseType().GetHashCode();
	// if (id == null)
	// {
	// return hash;
	// }
	// return hash ^ id.GetHashCode();
	// }

	// public Object testGetId()
	// {
	// return Id;
	// }

	// public void testSetId(Object value)
	// {
	// Id = (int)value;
	// }

	// public Object GetDirect()
	// {
	// return base.Types;
	// }

	// public void SetDirect(Object value)
	// {
	// base.Types = (IList<MaterialType>)value;
	// }

	// private ValueHolderContainerTemplate vhct;

	// public IObjRelation GetSelf(IRelationInfoItem member)
	// {
	// return vhct.GetSelf(this, member);
	// }

	// public int PropertyName_initialized;

	// protected IRelationInfoItem[] relationMembers;

	// public virtual IList<MaterialType> PropertyName
	// {
	// get
	// {
	// if (PropertyName_initialized != 0)
	// {
	// return base.Types;
	// }
	// return (IList<MaterialType>)vhct.GetValue(this, relationMembers[55]);
	// }
	// }

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Object getId2()
	{
		return id2;
	}

	public void setId2(Object id2)
	{
		this.id2 = id2;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getNames()
	{
		return names;
	}

	public void setNames(List<String> names)
	{
		this.names = names;
	}

	public EmbeddedMaterial getEmbMat()
	{
		return embMat;
	}

	public void setEmbMat(EmbeddedMaterial embMat)
	{
		this.embMat = embMat;
	}

	public EmbeddedMaterial getEmbMat3()
	{
		return embMat3;
	}

	public void setEmbMat3(EmbeddedMaterial embMat3)
	{
		this.embMat3 = embMat3;
	}

}