using De.Osthus.Ambeth.Cache;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class ParentCacheHardRefVisitor : ClassVisitor
    {
        private static readonly MethodInstance template_m_setParentCacheValueHardRef = new MethodInstance(null, typeof(IParentCacheValueHardRef),
                typeof(void), "set_ParentCacheValueHardRef", typeof(Object));

        public ParentCacheHardRefVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void VisitEnd()
        {
            ImplementParentCacheValueHardRef();
            base.VisitEnd();
        }

        protected void ImplementParentCacheValueHardRef()
        {
            FieldInstance f_pcvhr = new FieldInstance(FieldAttributes.Private, "$f_pcvhr", template_m_setParentCacheValueHardRef.Parameters[0]);

            f_pcvhr = ImplementField(f_pcvhr);
            ImplementSetter(template_m_setParentCacheValueHardRef, f_pcvhr);
        }
    }
}