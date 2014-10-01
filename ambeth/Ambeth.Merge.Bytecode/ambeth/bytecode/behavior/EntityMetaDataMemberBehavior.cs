using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Text;

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
			RelationMemberEnhancementHint relationMemberHint = state.GetContext<RelationMemberEnhancementHint>();
			String[] memberNameSplit = EmbeddedMember.Split(memberHint.MemberName);
		    if (memberNameSplit.Length == 1)
		    {
			    IPropertyInfo[] sPropertyPath = new IPropertyInfo[1];
                sPropertyPath[0] = PropertyInfoProvider.GetProperty(memberHint.DeclaringType, memberHint.MemberName);
			    visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.DeclaringType, memberHint.DeclaringType, memberHint.MemberName,
                        EntityMetaDataProvider, sPropertyPath);
			    if (relationMemberHint != null)
			    {
                    visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.DeclaringType, memberHint.MemberName, sPropertyPath);
			    }
			    else
			    {
                    visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.DeclaringType, memberHint.MemberName, sPropertyPath);
			    }
			    return visitor;
		    }
		    Member[] members = new Member[memberNameSplit.Length];
		    Type currType = memberHint.DeclaringType;
		    IPropertyInfo[] propertyPath = new IPropertyInfo[members.Length];
		    StringBuilder sb = new StringBuilder();
		    for (int a = 0, size = memberNameSplit.Length; a < size; a++)
		    {
			    if (a + 1 < memberNameSplit.Length)
			    {
				    members[a] = MemberTypeProvider.GetMember(currType, memberNameSplit[a]);
			    }
			    else if (relationMemberHint != null)
			    {
                    members[a] = MemberTypeProvider.GetRelationMember(currType, memberNameSplit[a]);
			    }
			    else
			    {
                    members[a] = MemberTypeProvider.GetPrimitiveMember(currType, memberNameSplit[a]);
			    }
			    if (a > 0)
			    {
				    sb.Append('.');
			    }
			    sb.Append(members[a].Name);
			    propertyPath[a] = PropertyInfoProvider.GetProperty(currType, memberNameSplit[a]);
			    if (a + 1 < memberNameSplit.Length)
			    {
				    currType = BytecodeEnhancer.GetEnhancedType(members[a].RealType,
						    new EmbeddedEnhancementHint(memberHint.DeclaringType, currType, sb.ToString()));
			    }
			    else
			    {
				    currType = members[a].RealType;
			    }
		    }
		    visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.DeclaringType, memberHint.DeclaringType, memberHint.MemberName,
				    EntityMetaDataProvider, propertyPath);
		    if (relationMemberHint != null)
		    {
			    visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.DeclaringType, memberHint.MemberName, propertyPath);
		    }
		    else
		    {
			    visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.DeclaringType, memberHint.MemberName, propertyPath);
		    }
		    visitor = new EntityMetaDataEmbeddedMemberVisitor(visitor, memberHint.DeclaringType, memberHint.MemberName, members);
		    return visitor;
		}
	}
}