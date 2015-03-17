package de.osthus.ambeth.mapping;

import de.osthus.ambeth.merge.IObjRefProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;

public class MappingObjRefProvider implements IObjRefProvider
{
	protected final Member refBOBuidMember;

	protected final Member refBOVersionMember;

	protected final byte refBOBuidIndex;

	public MappingObjRefProvider(Member refBOBuidMember, Member refBOVersionMember, byte refBOBuidIndex)
	{
		this.refBOBuidMember = refBOBuidMember;
		this.refBOVersionMember = refBOVersionMember;
		this.refBOBuidIndex = refBOBuidIndex;
	}

	@Override
	public IObjRef getORI(Object obj, IEntityMetaData metaData)
	{
		String buid = (String) refBOBuidMember.getValue(obj, false);
		Object version = refBOVersionMember != null ? refBOVersionMember.getValue(obj, true) : null;
		IObjRef ori = new ObjRef(metaData.getEntityType(), refBOBuidIndex, buid, version);
		return ori;
	}
}
