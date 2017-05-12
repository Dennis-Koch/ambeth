using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class ObjRefStoreVisitor : ObjRefVisitor
    {
        public ObjRefStoreVisitor(IClassVisitor cv, IEntityMetaData metaData, int idIndex)
            : base(cv, metaData, idIndex)
        {

        }

        public override void VisitEnd()
        {
            base.VisitEnd();
        }
    }
}