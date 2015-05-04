package de.osthus.ambeth.audit;

import java.io.DataOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.util.NullOutputStream;
import de.osthus.ambeth.audit.util.SignatureOutputStream;
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
			writeToSignatureHandle(signatureHandle, null, auditEntry);

			byte[] sign = signatureHandle.sign();

			auditEntry.ensurePrimitive(IAuditEntry.Signature).setNewValue(Base64.encodeBytes(sign).toCharArray());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void writeToSignatureHandle(java.security.Signature signatureHandle, IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		try
		{
			int protocol = getProtocol(auditEntry, auditEntryContainer);
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null)
			{
				throw new IllegalArgumentException("Not instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '" + protocol + "' of "
						+ auditEntry);
			}
			String hashAlgorithm = getHashAlgorithm(auditEntry, auditEntryContainer);
			if (hashAlgorithm != null && hashAlgorithm.length() > 0)
			{
				// build a good hash from the audited information: to sign its hash is faster than to sign the audited information itself
				// the clue is to choose a good hash algorithm which is fast enough to make sense but much stronger than e.g. MD5 as well...

				MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

				DigestOutputStream digestOS = new DigestOutputStream(new NullOutputStream(), md);
				DataOutputStream dos = new DataOutputStream(digestOS);

				if (auditEntry != null)
				{
					auditEntryWriter.writeAuditEntry(auditEntry, dos);
				}
				else
				{
					auditEntryWriter.writeAuditEntry(auditEntryContainer, dos);
				}
				dos.close();

				byte[] digestToSign = md.digest();
				signatureHandle.update(digestToSign);
			}
			else
			{
				// we have no hashAlgorithm: so we sign the whole audited information
				SignatureOutputStream sos = new SignatureOutputStream(new NullOutputStream(), signatureHandle);
				DataOutputStream dos = new DataOutputStream(sos);
				auditEntryWriter.writeAuditEntry(auditEntry, dos);
				dos.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected int getProtocol(IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		if (auditEntry != null)
		{
			return auditEntry.getProtocol();
		}
		return conversionHelper.convertValueToType(Integer.class, auditEntryContainer.findPrimitive(IAuditEntry.Protocol).getNewValue()).intValue();
	}

	protected String getHashAlgorithm(IAuditEntry auditEntry, CreateOrUpdateContainerBuild auditEntryContainer)
	{
		if (auditEntry != null)
		{
			return auditEntry.getHashAlgorithm();
		}
		return conversionHelper.convertValueToType(String.class, auditEntryContainer.findPrimitive(IAuditEntry.HashAlgorithm).getNewValue());
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
