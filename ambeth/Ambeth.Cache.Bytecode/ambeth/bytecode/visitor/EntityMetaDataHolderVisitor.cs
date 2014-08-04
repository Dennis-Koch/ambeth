using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class EntityMetaDataHolderVisitor : ClassVisitor
    {
        private static readonly MethodInstance m_template_getEntityMetaData = new MethodInstance(null, typeof(IEntityMetaDataHolder), typeof(IEntityMetaData),
                "Get__EntityMetaData");

        protected IEntityMetaData metaData;

        public EntityMetaDataHolderVisitor(IClassVisitor cv, IEntityMetaData metaData)
            : base(cv)
        {
            this.metaData = metaData;
        }

        public override void VisitEnd()
        {
            ImplementGetEntityMetaData();
            base.VisitEnd();
        }

        protected void ImplementGetEntityMetaData()
        {
            MethodInstance method = MethodInstance.FindByTemplate(m_template_getEntityMetaData, true);
            if (method != null)
            {
                // already implemented
                return;
            }
            FieldInstance f_entityMetaData = ImplementStaticAssignedField("$entityMetaData", metaData);

            ImplementGetter(m_template_getEntityMetaData, f_entityMetaData);
        }
    }
}