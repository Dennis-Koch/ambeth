using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Mixin;
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

		protected static readonly MethodInstance template_m_isTransient = new MethodInstance(null, typeof(PrimitiveMember), typeof(bool), "get_Transient");

		protected static readonly MethodInstance template_m_setTransient = new MethodInstance(null, typeof(IPrimitiveMemberWrite), typeof(void),
				"SetTransient", typeof(bool));

		protected static readonly MethodInstance template_m_getDefinedBy = new MethodInstance(null, typeof(PrimitiveMember), typeof(PrimitiveMember), "get_DefinedBy");

		protected static readonly MethodInstance template_m_setDefinedBy = new MethodInstance(null, typeof(IPrimitiveMemberWrite), typeof(void),
				"SetDefinedBy", typeof(PrimitiveMember));

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
			ImplementTransient();
			ImplementDefinedBy();
            base.VisitEnd();
        }

        protected void ImplementIsTechnicalMember()
        {
            FieldInstance f_technicalMember = ImplementField(new FieldInstance(FieldAttributes.Private, "__technicalMember", typeof(bool)));

            ImplementGetter(template_m_isTechnicalMember, f_technicalMember);
            ImplementSetter(template_m_setTechnicalMember, f_technicalMember);
        }

		protected void ImplementTransient()
		{
			FieldInstance f_transient = ImplementField(new FieldInstance(FieldAttributes.Private, "__transient", typeof(bool)));

			ImplementGetter(template_m_isTransient, f_transient);
			ImplementSetter(template_m_setTransient, f_transient);
		}

		protected void ImplementDefinedBy()
		{
			FieldInstance f_definedBy = ImplementField(new FieldInstance(FieldAttributes.Private, "__definedBy", typeof(PrimitiveMember)));

			ImplementGetter(template_m_getDefinedBy, f_definedBy);
			ImplementSetter(template_m_setDefinedBy, f_definedBy);
		}
    }
}