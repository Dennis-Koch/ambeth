using System;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Schema;

namespace De.Osthus.Ambeth.Util.Xml
{
    /// <summary>
    /// This class encapsulates the validation of xml documents against xsd files.
    /// It is used to have the same API as in Java Ambeth.
    /// </summary>
    public class XmlValidator : IXmlValidator
    {
        protected XmlReaderSettings settings;

        public XmlValidator(XmlSchemaSet schemaSet)
        {
            settings = new XmlReaderSettings();
            settings.ValidationType = ValidationType.Schema;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ProcessInlineSchema;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ProcessSchemaLocation;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ReportValidationWarnings;
            settings.Schemas = schemaSet;
            settings.ValidationEventHandler += new ValidationEventHandler(ValidationCallBack);
        }

        public void Validate(XDocument doc)
        {
            XmlReader xmlReader = doc.CreateReader();
            // Create the XmlReader object.
            XmlReader reader = XmlReader.Create(xmlReader, settings);
            // Parse the file. 
            while (reader.Read()) ;
        }

        protected void ValidationCallBack(object sender, ValidationEventArgs args)
        {
            if (args.Severity == XmlSeverityType.Warning)
                Console.WriteLine("\tWarning: Matching schema not found.  No validation occurred." + args.Message);
            else
                throw new Exception("\tValidation error: " + args.Message);

        }
    }
}
