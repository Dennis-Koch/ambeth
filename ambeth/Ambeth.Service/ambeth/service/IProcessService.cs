using System;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Service
{
    public interface IProcessService
    {
        Object InvokeService(IServiceDescription serviceDescription);
    }
}
