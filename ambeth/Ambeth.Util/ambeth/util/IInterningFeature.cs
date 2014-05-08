using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IInterningFeature
    {
        T Intern<T>(T value);
    }
}