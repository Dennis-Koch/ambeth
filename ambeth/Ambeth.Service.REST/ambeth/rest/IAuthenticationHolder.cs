using System;

namespace De.Osthus.Ambeth.Rest
{
    public interface IAuthenticationHolder
    {
        String[] GetAuthentication();

        void SetAuthentication(String userName, String password);
    }
}
