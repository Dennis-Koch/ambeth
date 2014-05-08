using System;
using De.Osthus.Ambeth.Util.Converter;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Util.Test
{
    [TestClass]
    public class SByteArrayConverterTest
    {
        private SByteArrayConverter fixture;

        [TestInitialize]
        public void SetUp()
        {
            fixture = new SByteArrayConverter();
        }

        [TestMethod]
        public void TestConvertValueToType_Plain()
        {
            String expected = "Not yet implemented";
            sbyte[] converted = (sbyte[])fixture.ConvertValueToType(typeof(sbyte[]), typeof(String), expected, null);
            String actual = (String)fixture.ConvertValueToType(typeof(String), typeof(sbyte[]), converted, null);
            Assert.AreEqual(expected, actual);
        }

        [TestMethod]
        public void TestConvertValueToType_Base64()
        {
            String expected = "Not yet implemented";
            sbyte[] converted = (sbyte[])fixture.ConvertValueToType(typeof(sbyte[]), typeof(String), expected, EncodingInformation.SOURCE_PLAIN
                    | EncodingInformation.TARGET_BASE64);
            String actual = (String)fixture.ConvertValueToType(typeof(String), typeof(sbyte[]), converted, EncodingInformation.SOURCE_BASE64
                    | EncodingInformation.TARGET_PLAIN);
            Assert.AreEqual(expected, actual);
        }
    }
}
