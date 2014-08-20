package de.osthus.ambeth.rdf;

import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Model;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Property;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.RDFNode;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Resource;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Statement;
import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.StmtIterator;
import de.osthus.ambeth.util.ParamChecker;

/**
 * This class contains general helper methods used in different allotrope projects.
 */
public class RdfUtils implements IRdfUtils
{
	@LogInstance
	private ILogger log;

	@Override
	public void insertTriple(Model rdfModel, String subject, String predicate, String object)
	{
		ParamChecker.assertNotNull(rdfModel, "rdfModel");
		ParamChecker.assertNotNull(subject, "subject");
		ParamChecker.assertNotNull(predicate, "predicate");
		ParamChecker.assertNotNull(object, "object");

		Resource subject_ = rdfModel.createResource(subject);
		Property predicate_ = rdfModel.createProperty(predicate);
		RDFNode object_ = rdfModel.createLiteral(object);

		rdfModel.add(subject_, predicate_, object_);
	}

	@Override
	public List<String> getObjects(Model rdfModel, String subject, String predicate)
	{
		ParamChecker.assertNotNull(rdfModel, "rdfModel");
		ParamChecker.assertNotNull(subject, "subject");
		ParamChecker.assertNotNull(predicate, "predicate");

		Resource subject_ = rdfModel.createResource(subject);
		Property predicate_ = rdfModel.createProperty(predicate);

		StmtIterator listStatements = rdfModel.listStatements(subject_, predicate_, (RDFNode) null);
		ArrayList<String> resultStrings = new ArrayList<>();
		for (Statement s : listStatements.toList())
		{
			RDFNode object = s.getObject();
			if (object.isLiteral())
			{
				resultStrings.add(object.asLiteral().getString());
			}
		}
		return resultStrings;

	}
}
