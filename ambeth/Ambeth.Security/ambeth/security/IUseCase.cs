using System.Text.RegularExpressions;

namespace De.Osthus.Ambeth.Security
{
    public interface IUseCase
    {
        Regex[] Patterns { get; }
    }
}
