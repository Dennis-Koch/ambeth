using System;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Rest
{
    public class AuthenticationHolder : IAuthenticationHolder
    {
        [Property(ServiceConfigurationConstants.UserName, Mandatory = false, DefaultValue = "dummyUser")]
        public String UserName { get; set; }

        [Property(ServiceConfigurationConstants.Password, Mandatory = false, DefaultValue = "dummyPass")]
        public String Password { get; set; }

        public String[] GetAuthentication()
        {
            lock (this)
            {
                String[] authentication = { UserName, Password };
                return authentication;
            }
        }

        public void SetAuthentication(String userName, String password)
        {
            lock (this)
            {
                UserName = userName;
                Password = password;
            }
        }
    }
}
