using System;
using System.Reflection;

namespace ClrTest.Reflection
{
    public interface IILProvider
    {
        Byte[] GetByteArray();
    }

    public class MethodBaseILProvider : IILProvider
    {
        MethodBase m_method;
        byte[] m_byteArray;

        public MethodBaseILProvider(MethodBase method)
        {
            m_method = method;
        }

        public byte[] GetByteArray()
        {
            if (m_byteArray == null)
            {
#if SILVERLIGHT
                m_byteArray = new Byte[0];
#else
                MethodBody methodBody = m_method.GetMethodBody();
                m_byteArray = (methodBody == null) ? new Byte[0] : methodBody.GetILAsByteArray();
#endif
            }
            return m_byteArray;
        }
    }
}