using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class GetIdMethodCreator : ClassVisitor
    {
        protected readonly IEntityMetaData metaData;

        private static readonly MethodInstance template_m_entityEquals_getId = new MethodInstance(null, typeof(IEntityEquals), typeof(Object), "Get__Id");

        public static MethodInstance GetGetId()
        {
            return MethodInstance.FindByTemplate(template_m_entityEquals_getId, false);
        }

        public GetIdMethodCreator(IClassVisitor cv, IEntityMetaData metaData)
            : base(cv)
        {
            this.metaData = metaData;
        }

        public override void VisitEnd()
        {
            MethodInstance m_get__Id = MethodInstance.FindByTemplate(GetIdMethodCreator.template_m_entityEquals_getId, true);
            if (m_get__Id != null)
            {
                base.VisitEnd();
                return;
            }
            MethodInstance m_getEntityMetaData = EntityMetaDataHolderVisitor.GetImplementedGetEntityMetaData(this, metaData);
            IMethodVisitor mg = VisitMethod(GetIdMethodCreator.template_m_entityEquals_getId);

            mg.CallThisGetter(m_getEntityMetaData);
            mg.InvokeInterface(new MethodInstance(null, typeof(IEntityMetaData), typeof(PrimitiveMember), "get_IdMember"));
            mg.LoadThis();
            mg.Push(false);
            mg.InvokeVirtual(EntityMetaDataMemberVisitor.template_m_getValueWithFlag);
            mg.ReturnValue();
		    mg.EndMethod();

            base.VisitEnd();
        }

        protected Type GetDeclaringType(MemberInfo member, Type newEntityType)
        {
            if (member.DeclaringType.IsInterface)
            {
                return newEntityType;
            }
            return member.DeclaringType;
        }
    }
}