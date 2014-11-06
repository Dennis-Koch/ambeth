using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;

namespace CsharpClassbrowser.Test
{
    [TestClass]
    public class ParserUtilTest
    {
        [TestMethod]
        public void TestAnalyzeType()
        {
            Type type = typeof(TestClass);
            String typeType = ParserUtil.GetTypeType(type);
            String source = "C:\\dev\\lib\\jambeth-ioc-2.2.321.dll";
            IDictionary<String, String> moduleMap = new Dictionary<String, String>();
            TypeDescription typeDescription = ParserUtil.AnalyzeType(type, typeType, source, moduleMap);

            Assert.AreEqual("CsharpClassbrowser.AbstractTestClass", typeDescription.SuperType);
            IList<String> interfaces = typeDescription.Interfaces;
            Assert.AreEqual(2, interfaces.Count);
            Assert.IsTrue(interfaces.Contains("CsharpClassbrowser.TestInterface1"));
            Assert.IsTrue(interfaces.Contains("CsharpClassbrowser.TestInterface2"));
        }

        [TestMethod]
        public void TestAddFieldDescriptions()
        {
            TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
            ParserUtil.AddFieldDescriptions(typeof(TestClass), typeDescription);

            IList<FieldDescription> fieldDescriptions = typeDescription.FieldDescriptions;
            Assert.IsNotNull(fieldDescriptions);
            Assert.AreEqual(7, fieldDescriptions.Count);
            Assert.AreEqual(1, fieldDescriptions[1].Annotations.Count);
            Assert.AreEqual(1, fieldDescriptions[2].Annotations.Count);
            Assert.AreEqual(0, fieldDescriptions[5].Annotations.Count);
            Assert.AreEqual(0, fieldDescriptions[6].Annotations.Count);

            // Check initialValue of constant fields
            Assert.AreEqual("test constant", fieldDescriptions[5].InitialValue);
            Assert.AreEqual("42", fieldDescriptions[6].InitialValue);
        }

        [TestMethod]
        public void TestAddMethodDescriptions()
        {
            TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
            ParserUtil.AddMethodDescriptions(typeof(TestClass), typeDescription);

            IList<MethodDescription> methodDescriptions = typeDescription.MethodDescriptions;
            Assert.IsNotNull(methodDescriptions);
            Assert.AreEqual(1, methodDescriptions.Count);
            Assert.AreEqual(1, methodDescriptions[0].Annotations.Count);
        }

        [TestMethod]
        public void TestGetAnnotationInfo()
        {
            TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
            ParserUtil.AddFieldDescriptions(typeof(TestClass), typeDescription);

            IList<FieldDescription> fieldDescriptions = typeDescription.FieldDescriptions;
            Assert.IsNotNull(fieldDescriptions);
            Assert.AreEqual(7, fieldDescriptions.Count);

            // Check FieldDescriptors
            FieldDescription logFieldDescription = fieldDescriptions[0];
            IList<AnnotationInfo> annotations = logFieldDescription.Annotations;
            Assert.AreEqual(1, annotations.Count);
            AnnotationInfo annotationInfo = annotations[0];
            Assert.AreEqual("De.Osthus.Ambeth.Log.LogInstanceAttribute", annotationInfo.AnnotationType);
            Assert.AreEqual(0, annotationInfo.Parameters.Count);

            FieldDescription serviceFieldDescription = fieldDescriptions[1];
            annotations = serviceFieldDescription.Annotations;
            Assert.AreEqual(1, annotations.Count);
            annotationInfo = annotations[0];
            Assert.AreEqual("De.Osthus.Ambeth.Ioc.Annotation.AutowiredAttribute", annotationInfo.AnnotationType);
            Assert.AreEqual(3, annotationInfo.Parameters.Count);

            FieldDescription service2FieldDescription = fieldDescriptions[2];
            annotations = service2FieldDescription.Annotations;
            Assert.AreEqual(1, annotations.Count);
            annotationInfo = annotations[0];
            Assert.AreEqual("De.Osthus.Ambeth.Ioc.Annotation.AutowiredAttribute", annotationInfo.AnnotationType);
            Assert.AreEqual(3, annotationInfo.Parameters.Count);

            FieldDescription service3FieldDescription = fieldDescriptions[3];
            annotations = service3FieldDescription.Annotations;
            Assert.AreEqual(1, annotations.Count);
            annotationInfo = annotations[0];
            Assert.AreEqual("De.Osthus.Ambeth.Ioc.Annotation.AutowiredAttribute", annotationInfo.AnnotationType);
            IList<AnnotationParamInfo> parameters = annotationInfo.Parameters;
            Assert.AreEqual(3, parameters.Count);

            // Check parameters of the @Autowired of the fourth field
            Dictionary<string, object[]> expected = new Dictionary<string, object[]>();
            expected.Add("Name", new object[] { "System.String", "", "test" });
            expected.Add("Optional", new object[] { "System.Boolean", false, true });
            expected.Add("Value", new object[] { "System.Type", typeof(Object), typeof(Object) });

            for (int i = 0; i < 3; i++)
            {
                AnnotationParamInfo annotationParamInfo = parameters[i];
                string paramName = annotationParamInfo.Name;
                Assert.IsTrue(expected.ContainsKey(paramName));
                object[] expectedValues = expected[paramName];
                Assert.AreEqual(expectedValues[0], annotationParamInfo.Type);
                Assert.AreEqual(expectedValues[1], annotationParamInfo.DefaultValue);
                Assert.AreEqual(expectedValues[2], annotationParamInfo.CurrentValue);
            }
        }
    }
}
