package de.osthus.ambeth.rdf;

import java.util.List;

import de.osthus.ambeth.rdf.repackaged.com.hp.hpl.jena.rdf.model.Model;

public interface IRdfUtils
{

	/**
	 * this method creates a rdf triple node and add it to the delivered rdf model
	 * 
	 * @param rdfModel
	 *            {@link Model}
	 * @param subject
	 *            String
	 * @param predicate
	 *            String
	 * @param object
	 *            String
	 */
	void insertTriple(Model rdfModel, String subject, String predicate, String object);

	/**
	 * Gets a list of objects as Strings from the supplied model, as long as they were inserted as a literal
	 * 
	 * @param rdfModel
	 *            the {@link Model} containing
	 * @param subject
	 *            The subject sought
	 * @param predicate
	 *            the predicate sought
	 * @return a list of all Strings listed as object literals for this combination of subject and predicate, never <code>null</code>
	 */
	List<String> getObjects(Model rdfModel, String subject, String predicate);

}