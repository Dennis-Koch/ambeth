using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityMetaDataPrimitiveMemberVisitor : ClassVisitor
    {
        protected static readonly MethodInstance template_m_isTechnicalMember = new MethodInstance(null, typeof(PrimitiveMember), typeof(bool), "isTechnicalMember");

        protected static readonly MethodInstance template_m_setTechnicalMember = new MethodInstance(null, typeof(IPrimitiveMemberWrite), typeof(void),
                "setTechnicalMember", typeof(bool));

        protected readonly Type entityType;

        protected readonly String memberName;

        protected IPropertyInfoProvider propertyInfoProvider;

        public EntityMetaDataPrimitiveMemberVisitor(ClassVisitor cv, Type entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.entityType = entityType;
            this.memberName = memberName;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            IPropertyInfo[] propertyPath = MemberTypeProvider.BuildPropertyPath(entityType, memberName, propertyInfoProvider);
            ImplementIsTechnicalMember(propertyPath);
            base.VisitEnd();
        }

        protected void ImplementIsTechnicalMember(IPropertyInfo[] propertyPath)
        {
            FieldInstance f_technicalMember = ImplementField(new FieldInstance(FieldAttributes.Private, "__technicalMember", typeof(bool)));

            ImplementGetter(template_m_isTechnicalMember, f_technicalMember);
            ImplementSetter(template_m_setTechnicalMember, f_technicalMember);
        }
    }
}