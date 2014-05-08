package de.osthus.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.util.Arrays;

@XmlRootElement(name = "UpdateContainer", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateContainer extends AbstractChangeContainer
{
	public static final PrimitiveUpdateItem[] emptyPrimitiveItems = new PrimitiveUpdateItem[0];

	public static final RelationUpdateItem[] emptyRelationItems = new RelationUpdateItem[0];

	@XmlElement(required = true)
	protected IPrimitiveUpdateItem[] primitives;

	@XmlElement(required = true)
	protected IRelationUpdateItem[] relations;

	public IPrimitiveUpdateItem[] getPrimitives()
	{
		return primitives;
	}

	public void setPrimitives(IPrimitiveUpdateItem[] primitives)
	{
		this.primitives = primitives;
	}

	public IRelationUpdateItem[] getRelations()
	{
		return relations;
	}

	public void setRelations(IRelationUpdateItem[] relations)
	{
		this.relations = relations;
	}

	@Override
	public void toString(StringBuilder sb)
	{
		super.toString(sb);
		sb.append(" Primitives=");
		Arrays.toString(sb, primitives);
		sb.append(" Relations=");
		Arrays.toString(sb, relations);
	}
}
