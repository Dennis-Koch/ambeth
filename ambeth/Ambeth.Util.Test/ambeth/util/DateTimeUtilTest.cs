using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Util.Test
{
    [TestClass]
    public class DateTimeUtilTest
    {
        [TestMethod]
        public void TestRoundTripSummerUnspecified()
        {
            // Unspecified values are interpreted as local!
            DateTime inSummer = new DateTime(2013, 06, 28, 17, 24, 03, DateTimeKind.Local);
            long longSummer = DateTimeUtil.ConvertDateTimeToJavaMillis(inSummer);
            long summerTime = 1372433043000;
            Assert.AreEqual(summerTime, longSummer, "differes by " + Math.Abs(summerTime - longSummer) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longSummer);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inSummer.Subtract(new TimeSpan(2, 0, 0)), actual);
        }

        [TestMethod]
        public void TestRoundTripWinterUnspecified()
        {
            // Unspecified values are interpreted as local!
            DateTime inWinter = new DateTime(2013, 11, 28, 16, 24, 03, DateTimeKind.Local);
            long longWinter = DateTimeUtil.ConvertDateTimeToJavaMillis(inWinter);
            long winterTime = 1385652243000;
            Assert.AreEqual(winterTime, longWinter, "differes by " + Math.Abs(winterTime - longWinter) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longWinter);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inWinter.Subtract(new TimeSpan(1, 0, 0)), actual);
        }

        [TestMethod]
        public void TestRoundTripSummerExplicitUtc()
        {
            DateTime inSummer = new DateTime(2013, 06, 28, 15, 24, 03, DateTimeKind.Utc);
            long longSummer = DateTimeUtil.ConvertDateTimeToJavaMillis(inSummer);
            long summerTime = 1372433043000;
            Assert.AreEqual(summerTime, longSummer, "differes by " + Math.Abs(summerTime - longSummer) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longSummer);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inSummer, actual);
        }

        [TestMethod]
        public void TestRoundTripWinterExplicitUtc()
        {
            DateTime inWinter = new DateTime(2013, 11, 28, 15, 24, 03, DateTimeKind.Utc);
            long longWinter = DateTimeUtil.ConvertDateTimeToJavaMillis(inWinter);
            long winterTime = 1385652243000;
            Assert.AreEqual(winterTime, longWinter, "differes by " + Math.Abs(winterTime - longWinter) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longWinter);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inWinter, actual);
        }

        [TestMethod]
        public void TestRoundTripSummerExplicitLocal()
        {
            DateTime inSummer = new DateTime(2013, 06, 28, 17, 24, 03, DateTimeKind.Local);
            long longSummer = DateTimeUtil.ConvertDateTimeToJavaMillis(inSummer);
            long summerTime = 1372433043000;
            Assert.AreEqual(summerTime, longSummer, "differes by " + Math.Abs(summerTime - longSummer) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longSummer);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inSummer.Subtract(new TimeSpan(2, 0, 0)), actual);
        }

        [TestMethod]
        public void TestRoundTripWinterExplicitLocal()
        {
            DateTime inWinter = new DateTime(2013, 11, 28, 16, 24, 03, DateTimeKind.Local);
            long longWinter = DateTimeUtil.ConvertDateTimeToJavaMillis(inWinter);
            long winterTime = 1385652243000;
            Assert.AreEqual(winterTime, longWinter, "differes by " + Math.Abs(winterTime - longWinter) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longWinter);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inWinter.Subtract(new TimeSpan(1, 0, 0)), actual);
        }

        [TestMethod]
        public void TestRoundTripWinterSpecificDateTime1()
        {
            DateTime inWinter = new DateTime(2001, 1, 1, 0, 0, 0, DateTimeKind.Utc); // Begin of new millenium
            long longWinter = DateTimeUtil.ConvertDateTimeToJavaMillis(inWinter);
            long winterTime = 978307200000;
            Assert.AreEqual(winterTime, longWinter, "differes by " + Math.Abs(winterTime - longWinter) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longWinter);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inWinter, actual);
        }

        [TestMethod]
        public void TestRoundTripWinterSpecificDateTime2()
        {
            DateTime inWinter = new DateTime(2001, 9, 9, 1, 46, 40, DateTimeKind.Utc);
            long longWinter = DateTimeUtil.ConvertDateTimeToJavaMillis(inWinter);
            long winterTime = 1000000000000;
            Assert.AreEqual(winterTime, longWinter, "differes by " + Math.Abs(winterTime - longWinter) + "ms");

            DateTime actual = DateTimeUtil.ConvertJavaMillisToDateTime(longWinter);
            Assert.AreEqual(DateTimeKind.Utc, actual.Kind);
            Assert.AreEqual(inWinter, actual);
        }
    }
}
