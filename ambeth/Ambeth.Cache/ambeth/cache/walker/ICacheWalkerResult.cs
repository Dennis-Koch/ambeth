using De.Osthus.Ambeth.Util;
using System.Text;

namespace De.Osthus.Ambeth.Walker
{
    public interface ICacheWalkerResult : IPrintable
    {
	    void ToString(StringBuilder sb, int tabCount);
    }
}