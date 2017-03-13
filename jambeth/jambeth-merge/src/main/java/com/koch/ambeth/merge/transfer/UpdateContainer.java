package com.koch.ambeth.merge.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.util.Arrays;

@XmlRootElement(name = "UpdateContainer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateContainer extends AbstractChangeContainer implements ICreateOrUpdateContainer {
	public static final PrimitiveUpdateItem[] emptyPrimitiveItems = new PrimitiveUpdateItem[0];

	public static final RelationUpdateItem[] emptyRelationItems = new RelationUpdateItem[0];

	@XmlElement(required = true)
	protected IPrimitiveUpdateItem[] primitives;

	@XmlElement(required = true)
	protected IRelationUpdateItem[] relations;

	public IPrimitiveUpdateItem[] getPrimitives() {
		return primitives;
	}

	public void setPrimitives(IPrimitiveUpdateItem[] primitives) {
		this.primitives = primitives;
	}

	public IRelationUpdateItem[] getRelations() {
		return relations;
	}

	public void setRelations(IRelationUpdateItem[] relations) {
		this.relations = relations;
	}

	@Override
	public IRelationUpdateItem[] getFullRUIs() {
		return getRelations();
	}

	@Override
	public IPrimitiveUpdateItem[] getFullPUIs() {
		return getPrimitives();
	}

	@Override
	public void toString(StringBuilder sb) {
		super.toString(sb);
		sb.append(" Primitives=");
		Arrays.toString(sb, primitives);
		sb.append(" Relations=");
		Arrays.toString(sb, relations);
	}
}
