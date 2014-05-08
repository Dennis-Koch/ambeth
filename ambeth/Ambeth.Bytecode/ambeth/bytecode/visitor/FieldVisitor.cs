using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class FieldVisitor : IFieldVisitor
    {
        protected readonly FieldBuilder fb;

        public FieldVisitor(FieldBuilder fb)
        {
            this.fb = fb;
        }

        public void VisitEnd()
        {
            // Intended blank
        }
    }
}
