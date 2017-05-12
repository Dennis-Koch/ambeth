using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
public class ObjRefBehavior : AbstractBehavior
{
	[LogInstance]
	public ILogger Log { private get; set; }

	[Autowired]
	public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

	public override Type[] GetEnhancements()
    {
 	     return new Type[] { typeof(IObjRef) };
    }

    public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
    {
		ObjRefEnhancementHint memberHint = state.GetContext<ObjRefEnhancementHint>();
		if (memberHint == null)
		{
			return visitor;
		}
		IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(memberHint.EntityType);
		visitor = new ObjRefVisitor(visitor, metaData, memberHint.IdIndex);
		return visitor;
	}
}
}