package de.osthus.ambeth.audit;

import java.security.Signature;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.security.IPrivateKeyProvider;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.util.IConversionHelper;

public class AuditEntryToSignature implements IAuditEntryToSignature, IAuditEntryWriterExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrivateKeyProvider privateKeyProvider;

	@Property(name = AuditConfigurationConstants.ProtocolVersion, defaultValue = "1")
	protected int protocol;

	@Property(name = AuditConfigurationConstants.AuditedInformationHashAlgorithm, defaultValue = "SHA-256")
	protected String hashAlgorithm;

	protected final MapExtendableContainer<Integer, IAuditEntryWriter> auditEntryWriters = new MapExtendableContainer<Integer, IAuditEntryWriter>(
			"auditEntryWriter", "auditEntryProtocol");

	@Override
	public void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword, ISignature signature)
	{
		java.security.Signature signatureHandle = privateKeyProvider.getSigningHandle(signature, clearTextPassword);
		if (signatureHandle == null)
		{
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(null);
			return;
		}
		try
		{
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(hashAlgorithm);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(protocol);

			if (signature != null)
			{
				auditEntry.ensureRelation(IAuditEntry.SignatureOfUser).addObjRef(objRefHelper.entityToObjRef(signature));
			}
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '" + protocol + "' of "
						+ auditEntry);
			}
			auditEntryWriter.writeAuditEntry(auditEntry, hashAlgorithm, signatureHandle);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditEntry auditEntry, Signature signature)
	{
		try
		{
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '" + protocol + "' of "
						+ auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0)
			{
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			for (IAuditedEntity auditedEntity : auditEntry.getEntities())
			{
				byte[] auditedEntityDigest = auditEntryWriter.writeAuditedEntity(auditedEntity, hashAlgorithm);
				signature.update(auditedEntityDigest);
				if (!signature.verify(Base64.decode(auditedEntity.getSignature())))
				{
					return null;
				}
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much stronger than e.g. MD5 as well...

			return auditEntryWriter.writeAuditEntry(auditEntry, hashAlgorithm);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditedEntity auditedEntity)
	{
		try
		{
			IAuditEntry auditEntry = auditedEntity.getEntry();
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '" + protocol + "' of "
						+ auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0)
			{
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much stronger than e.g. MD5 as well...

			return auditEntryWriter.writeAuditedEntity(auditedEntity, hashAlgorithm);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion)
	{
		auditEntryWriters.register(auditEntryWriter, Integer.valueOf(protocolVersion));
	}

	@Override
	public void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion)
	{
		auditEntryWriters.unregister(auditEntryWriter, Integer.valueOf(protocolVersion));
	}
}
