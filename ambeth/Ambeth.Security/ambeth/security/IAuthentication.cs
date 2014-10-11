using System;

namespace De.Osthus.Ambeth.Security
{
    public interface IAuthentication
    {
        String UserName { get; }

        char[] Password { get; }

        PasswordType Type { get; }
    }
}