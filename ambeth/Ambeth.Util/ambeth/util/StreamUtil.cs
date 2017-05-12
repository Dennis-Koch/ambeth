using System.IO;

namespace De.Osthus.Ambeth.Util
{
    public class StreamUtil
    {
        public static void CopyStream(Stream input, Stream output)
        {
            byte[] b = new byte[32768];
            int count;
            while ((count = input.Read(b, 0, b.Length)) > 0)
            {
                output.Write(b, 0, count);
            }
        }

        public static byte[] GetBytes(Stream input)
        {
            MemoryStream mem = new MemoryStream();
            CopyStream(input, mem);
            return mem.ToArray();
        }
    }
}
