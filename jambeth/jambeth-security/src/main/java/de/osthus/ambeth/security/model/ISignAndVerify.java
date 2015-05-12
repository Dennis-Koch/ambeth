package de.osthus.ambeth.security.model;

public interface ISignAndVerify
{
	String getSignatureAlgorithm();

	void setSignatureAlgorithm(String signatureAlgorithm);

	String getKeyFactoryAlgorithm();

	void setKeyFactoryAlgorithm(String keyFactoryAlgorithm);
}
