using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityMetaDataRelationMemberVisitor : ClassVisitor
    {
        protected static readonly MethodInstance template_m_getCascadeLoadMode = new MethodInstance(null, typeof(RelationMember), typeof(CascadeLoadMode),
                "get_CascadeLoadMode");

        protected static readonly MethodInstance template_m_setCascadeLoadMode = new MethodInstance(null, typeof(IRelationMemberWrite), typeof(void),
                "SetCascadeLoadMode", typeof(CascadeLoadMode));

        protected static readonly MethodInstance template_m_isManyTo = new MethodInstance(null, typeof(RelationMember), typeof(bool), "get_IsManyTo");

        protected readonly Type entityType;

        protected readonly String memberName;

        protected readonly IPropertyInfo[] propertyPath;

        public EntityMetaDataRelationMemberVisitor(IClassVisitor cv, Type entityType, String memberName, IPropertyInfo[] propertyPath)
            : base(new InterfaceAdder(cv, typeof(IRelationMemberWrite)))
        {
            this.entityType = entityType;
            this.memberName = memberName;
            this.propertyPath = propertyPath;
        }

        public override void VisitEnd()
        {
            ImplementCascadeLoadMode();
            ImplementIsManyTo();
            base.VisitEnd();
        }

        protected void ImplementCascadeLoadMode()
        {
            FieldInstance f_cascadeLoadMode = ImplementField(new FieldInstance(FieldAttributes.Private, "__cascadeLoadMode", typeof(CascadeLoadMode)));

            ImplementGetter(template_m_getCascadeLoadMode, f_cascadeLoadMode);
            ImplementSetter(template_m_setCascadeLoadMode, f_cascadeLoadMode);

            ConstructorInfo[] constructors = State.CurrentType.GetConstructors(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.DeclaredOnly);
            for (int a = constructors.Length; a-- > 0; )
            {
                ConstructorInstance ci = new ConstructorInstance(constructors[a]);
                ci = new ConstructorInstance(MethodAttributes.Public, ci.Parameters);
                IMethodVisitor mv = VisitMethod(ci);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeSuperOfCurrentMethod();

                mv.PutThisField(f_cascadeLoadMode, delegate(IMethodVisitor mg)
                    {
                        mg.PushEnum(CascadeLoadMode.DEFAULT);
                    });
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementIsManyTo()
        {
            FieldInstance f_isManyTo = ImplementField(new FieldInstance(FieldAttributes.Private, "__isManyTo", typeof(bool)));

            ImplementGetter(template_m_isManyTo, f_isManyTo);
        }
    }
}