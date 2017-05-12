using System;
using System.Collections.Generic;
using System.Text;
using System.Security.Cryptography;
using System.IO;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Security.Config;

namespace De.Osthus.Ambeth.Crypto
{
    public class AESEncryption : IInitializingBean, IEncryption
    {
        [Property(SecurityConfigurationConstants.EnryptionPassword, DefaultValue="1234abcABC")]
        public virtual String EncryptionPassword { get; set; }

        private byte[] saltBytes = Encoding.UTF8.GetBytes("adgmkfl23458u90cvnm45n245ß9yf");

        protected AesManaged aesManaged;

        protected Rfc2898DeriveBytes rfc;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(EncryptionPassword, "EncryptionPassword");
            rfc = new Rfc2898DeriveBytes(EncryptionPassword, saltBytes);

            aesManaged= new AesManaged();
            aesManaged.BlockSize = aesManaged.LegalBlockSizes[0].MaxSize;
            aesManaged.KeySize = aesManaged.LegalKeySizes[0].MaxSize;
            aesManaged.Key = rfc.GetBytes(aesManaged.KeySize / 8);
            aesManaged.IV = rfc.GetBytes(aesManaged.BlockSize / 8);
        }

        public virtual void Encrypt(Stream inputStream, Stream outputStream)
        {
            StreamIntern(inputStream, outputStream, true);
        }

        public virtual void Decrypt(Stream inputStream, Stream outputStream)
        {
            StreamIntern(inputStream, outputStream, false);
        }

        protected virtual void StreamIntern(Stream inputStream, Stream outputStream, bool encrypt)
        {
            byte[] buffer = new byte[4096];

            using (ICryptoTransform cryptoTransform = encrypt ? aesManaged.CreateEncryptor() : aesManaged.CreateDecryptor())
            using (CryptoStream encryptor = new CryptoStream(outputStream, cryptoTransform, CryptoStreamMode.Write))
            {
                int bytesRead;
                while ((bytesRead = inputStream.Read(buffer, 0, buffer.Length)) != 0)
                {
                    encryptor.Write(buffer, 0, bytesRead);
                }
                encryptor.Flush();
                encryptor.Close();
            }
        }

        private byte[] StrToByteArray(String str)
        {
            return Encoding.UTF8.GetBytes(str);
        }

        private String ByteArrayToStr(byte[] dBytes)
        {
            return Encoding.UTF8.GetString(dBytes, 0, dBytes.Length);
        }
    }
}
