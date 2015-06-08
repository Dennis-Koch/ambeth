using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Security
{
    public interface ILightweightSecurityContext
    {
		bool IsAuthenticated();
    }
}
