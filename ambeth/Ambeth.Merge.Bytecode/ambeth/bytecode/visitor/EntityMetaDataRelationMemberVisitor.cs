using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Attribute;
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
    public class EntityMetaDataRelationMemberVisitor : ClassVisitor
    {
        protected static readonly MethodInstance template_m_getCascadeLoadMode = new MethodInstance(null, typeof(RelationMember), typeof(CascadeLoadMode),
                "get_CascadeLoadMode");

        protected static readonly MethodInstance template_m_setCascadeLoadMode = new MethodInstance(null, typeof(IRelationMemberWrite), typeof(void),
                "SetCascadeLoadMode", typeof(CascadeLoadMode));

        protected static readonly MethodInstance template_m_isManyTo = new MethodInstance(null, typeof(RelationMember), typeof(bool), "get_IsManyTo");

        protected static readonly MethodInstance template_m_isToMany = new MethodInstance(null, typeof(RelationMember), typeof(bool), "get_IsToMany");

        protected readonly Type entityType;

        protected readonly String memberName;

        protected IPropertyInfoProvider propertyInfoProvider;

        public EntityMetaDataRelationMemberVisitor(ClassVisitor cv, Type entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.entityType = entityType;
            this.memberName = memberName;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            IPropertyInfo[] propertyPath = MemberTypeProvider.BuildPropertyPath(entityType, memberName, propertyInfoProvider);
            ImplementCascadeLoadMode(propertyPath);
            ImplementIsManyTo(propertyPath);
            ImplementIsToMany(propertyPath);
            base.VisitEnd();
        }

        protected void ImplementCascadeLoadMode(IPropertyInfo[] propertyPath)
        {
            FieldInstance f_cascadeLoadMode = ImplementField(new FieldInstance(FieldAttributes.Private, "__cascadeLoadMode", typeof(CascadeLoadMode)));

            ImplementGetter(template_m_getCascadeLoadMode, f_cascadeLoadMode);
            ImplementSetter(template_m_setCascadeLoadMode, f_cascadeLoadMode);

            ConstructorInfo[] constructors = State.CurrentType.GetConstructors(BindingFlags.Public | BindingFlags.NonPublic);
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

        protected void ImplementIsManyTo(IPropertyInfo[] propertyPath)
        {
            FieldInstance f_isManyTo = ImplementField(new FieldInstance(FieldAttributes.Private, "__isManyTo", typeof(bool)));

            ImplementGetter(template_m_isManyTo, f_isManyTo);
        }

        protected void ImplementIsToMany(IPropertyInfo[] propertyPath)
        {
            IMethodVisitor mv = VisitMethod(template_m_isToMany);
            mv.Push(ListUtil.IsCollection(propertyPath[propertyPath.Length - 1].PropertyType));
            mv.ReturnValue();
            mv.EndMethod();
        }
    }
}