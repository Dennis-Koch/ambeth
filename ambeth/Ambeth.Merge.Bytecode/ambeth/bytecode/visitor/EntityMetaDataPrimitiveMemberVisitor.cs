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
        protected static readonly MethodInstance template_m_isTechnicalMember = new MethodInstance(null, typeof(PrimitiveMember), typeof(bool), "get_TechnicalMember");

        protected static readonly MethodInstance template_m_setTechnicalMember = new MethodInstance(null, typeof(IPrimitiveMemberWrite), typeof(void),
                "SetTechnicalMember", typeof(bool));

        protected readonly Type entityType;

        protected readonly String memberName;

        protected readonly IPropertyInfo[] propertyPath;

        public EntityMetaDataPrimitiveMemberVisitor(IClassVisitor cv, Type entityType, String memberName, IPropertyInfo[] propertyPath) : base(new InterfaceAdder(cv, typeof(IPrimitiveMemberWrite)))
        {
            this.entityType = entityType;
            this.memberName = memberName;
            this.propertyPath = propertyPath;
        }

        public override void VisitEnd()
        {
            ImplementIsTechnicalMember();
            base.VisitEnd();
        }

        protected void ImplementIsTechnicalMember()
        {
            FieldInstance f_technicalMember = ImplementField(new FieldInstance(FieldAttributes.Private, "__technicalMember", typeof(bool)));

            ImplementGetter(template_m_isTechnicalMember, f_technicalMember);
            ImplementSetter(template_m_setTechnicalMember, f_technicalMember);
        }
    }
}