using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;

namespace CsharpClassbrowser.Test
{
    [TestClass]
    public class ParserUtilTest
    {
        [TestMethod]
        public void TestAddFieldDescriptions()
        {
            TypeDescription typeDescription = new TypeDescription("null", "null", "null", "null", "null", "null", 0);
            ParserUtil.AddFieldDescriptions(typeof(TestClass), typeDescription);

            IList<FieldDescription> fieldDescriptions = typeDescription.FieldDescriptions;
            Assert.IsNotNull(fieldDescriptions);
            Assert.AreEqual(4, fieldDescriptions.Count);
            Assert.AreEqual(1, fieldDescriptions[0].Annotations.Count);
            Assert.AreEqual(1, fieldDescriptions[1].Annotations.Count);
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
            Assert.AreEqual(4, fieldDescriptions.Count);

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
