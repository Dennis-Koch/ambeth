using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
	public class EntityMetaDataMemberBehavior : AbstractBehavior
	{
		[LogInstance]
        public ILogger Log { private get; set; }

		[Autowired]
		public IBytecodeEnhancer BytecodeEnhancer { protected get; set; }

		[Autowired]
		public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

		[Autowired]
		public IMemberTypeProvider MemberTypeProvider { protected get; set; }

		[Autowired]
		public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

		public override Type[] GetEnhancements()
		{
			return new Type[] { typeof(IRelationMemberWrite), typeof(IPrimitiveMemberWrite), typeof(IEmbeddedMember) };
		}

		public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
		{
			MemberEnhancementHint memberHint = state.GetContext<MemberEnhancementHint>();
			if (memberHint == null)
			{
				return visitor;
			}
            String[] memberNameSplit = memberHint.MemberName.Split('.');
			if (memberNameSplit.Length == 1)
			{
				RelationMemberEnhancementHint relationMemberHint = state.GetContext<RelationMemberEnhancementHint>();
				visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.EntityType, memberNameSplit[0], BytecodeEnhancer, EntityMetaDataProvider,
						PropertyInfoProvider);
				if (relationMemberHint != null)
				{
					visitor = new InterfaceAdder(visitor, typeof(IRelationMemberWrite));
                    visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.EntityType, memberNameSplit[0], PropertyInfoProvider);
				}
				else
				{
					visitor = new InterfaceAdder(visitor, typeof(IPrimitiveMemberWrite));
                    visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.EntityType, memberNameSplit[0], PropertyInfoProvider);
				}
			}
			else
			{
				visitor = new InterfaceAdder(visitor, typeof(IEmbeddedMember));
				Member[] member = new Member[memberNameSplit.Length];
				Type currType = memberHint.EntityType;
				for (int a = 0, size = memberNameSplit.Length; a < size; a++)
				{
					member[a] = MemberTypeProvider.GetMember(currType, memberNameSplit[a]);
					currType = member[a].RealType;
				}
			}
			return visitor;
		}
	}
}