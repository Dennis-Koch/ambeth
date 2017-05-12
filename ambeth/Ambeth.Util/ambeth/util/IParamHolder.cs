using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IParamHolder<T>
    {
	    T Value { get; set; }
    }
}
