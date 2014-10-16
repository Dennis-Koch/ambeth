using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public class PrintableDelegateWrapper : IPrintable
    {
	    protected readonly PrintableDelegate p;

        public PrintableDelegateWrapper(PrintableDelegate p)
	    {
		    this.p = p;
	    }

        public void ToString(StringBuilder sb)
        {
            p(sb);
        }
    }
}
