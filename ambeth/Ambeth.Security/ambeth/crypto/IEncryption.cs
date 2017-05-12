using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.IO;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Crypto
{
    public interface IEncryption
    {
        void Encrypt(Stream inputStream, Stream outputStream);

        void Decrypt(Stream inputStream, Stream outputStream);
    }
}
