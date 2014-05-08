using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc.Factory;
using System.Collections;

namespace De.Osthus.Ambeth.Service
{
    public class RethrownException : Exception
    {
        public Exception Exception { get; private set; }

        public RethrownException(Exception e)
        {
            if (e is RethrownException)
            {
                e = ((RethrownException)e).Exception;
            }
            Exception = e;
        }

        public override bool Equals(Object obj)
        {
            if (!(obj is RethrownException))
            {
                return false;
            }
            RethrownException other = (RethrownException)obj;
            return Object.Equals(Exception, other.Exception);
        }

        public override int GetHashCode()
        {
            return Exception.GetHashCode();
        }

        public override string Message
        {
            get
            {
                return Exception.Message;
            }
        }

        public override string StackTrace
        {
            get
            {
                return Exception.StackTrace;
            }
        }

        public override IDictionary Data
        {
            get
            {
                return Exception.Data;
            }
        }
    }
}
