package com.koch.ambeth.audit.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.IPrivateKeyProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class AuditEntryToSignature implements IAuditEntryToSignature, IAuditEntryWriterExtendable {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrivateKeyProvider privateKeyProvider;

	@Property(name = AuditConfigurationConstants.ProtocolVersion, defaultValue = "1")
	protected int protocol;

	@Property(name = AuditConfigurationConstants.AuditedInformationHashAlgorithm,
			defaultValue = "SHA-256")
	protected String hashAlgorithm;

	protected final MapExtendableContainer<Integer, IAuditEntryWriter> auditEntryWriters =
			new MapExtendableContainer<>(
					"auditEntryWriter", "auditEntryProtocol");

	@Override
	public void signAuditEntry(CreateOrUpdateContainerBuild auditEntry, char[] clearTextPassword,
			ISignature signature) {
		final java.security.Signature signatureHandle = privateKeyProvider.getSigningHandle(signature,
				clearTextPassword);
		if (signatureHandle == null) {
			auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.SignedValue).setNewValue(null);
			auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(null);
			return;
		}
		auditEntry.ensurePrimitive(IAuditEntry.HashAlgorithm).setNewValue(hashAlgorithm);
		auditEntry.ensurePrimitive(IAuditEntry.Protocol).setNewValue(protocol);

		if (signature != null) {
			auditEntry.ensureRelation(IAuditEntry.SignatureOfUser)
					.addObjRef(objRefHelper.entityToObjRef(signature));
		}
		IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
		if (auditEntryWriter == null) {
			throw new IllegalArgumentException("No instance of " + IAuditEntryWriter.class.getSimpleName()
					+ " found for protocol '" + protocol + "' of " + auditEntry);
		}
		try {
			MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

			byte[] auditEntryDigest = auditEntryWriter.writeAuditEntry(auditEntry, md,
					new Function<byte[], byte[]>() {
						@Override
						public byte[] apply(byte[] auditedEntityDigest) {
							try {
								signatureHandle.update(auditedEntityDigest);
								return signatureHandle.sign();
							}
							catch (SignatureException e) {
								throw RuntimeExceptionUtil.mask(e);
							}
						}
					});
			if (auditEntryDigest == null) {
				auditEntry.ensurePrimitive(IAuditEntry.SignedValue).setNewValue(null);
				throw new IllegalStateException(
						"Could not sign due to an inconsistent model: " + auditEntry);
			}
			signatureHandle.update(auditEntryDigest);
			byte[] sign = signatureHandle.sign();
			auditEntry.ensurePrimitive(IAuditEntry.SignedValue)
					.setNewValue(Base64.encodeBytes(sign).toCharArray());
		}
		catch (SignatureException | NoSuchAlgorithmException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditEntry auditEntry,
			BiFunction<IAuditedEntity, byte[], Boolean> auditedEntityVerifyFunction) {
		try {
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null) {
				throw new IllegalArgumentException(
						"No instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
								+ protocol + "' of " + auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0) {
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
			for (IAuditedEntity auditedEntity : auditEntry.getEntities()) {
				byte[] auditedEntityDigest = auditEntryWriter.writeAuditedEntity(auditedEntity, md);
				if (auditedEntityDigest == null) {
					return null;
				}
				if (!auditedEntityVerifyFunction.apply(auditedEntity, auditedEntityDigest)) {
					return null;
				}
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the
			// audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much
			// stronger than e.g. MD5 as well...

			return auditEntryWriter.writeAuditEntry(auditEntry, md);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public byte[] createVerifyDigest(IAuditedEntity auditedEntity) {
		try {
			IAuditEntry auditEntry = auditedEntity.getEntry();
			int protocol = auditEntry.getProtocol();
			IAuditEntryWriter auditEntryWriter = auditEntryWriters.getExtension(protocol);
			if (auditEntryWriter == null) {
				throw new IllegalArgumentException(
						"No instance of " + IAuditEntryWriter.class.getSimpleName() + " found for protocol '"
								+ protocol + "' of " + auditEntry);
			}
			String hashAlgorithm = auditEntry.getHashAlgorithm();
			if (hashAlgorithm == null || hashAlgorithm.length() == 0) {
				throw new IllegalArgumentException("No hash algorithm specified");
			}
			// build a good hash from the audited information: to sign its hash is faster than to sign the
			// audited information itself
			// the clue is to choose a good hash algorithm which is fast enough to make sense but much
			// stronger than e.g. MD5 as well...
			MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
			return auditEntryWriter.writeAuditedEntity(auditedEntity, md);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion) {
		auditEntryWriters.register(auditEntryWriter, Integer.valueOf(protocolVersion));
	}

	@Override
	public void unregisterAuditEntryWriter(IAuditEntryWriter auditEntryWriter, int protocolVersion) {
		auditEntryWriters.unregister(auditEntryWriter, Integer.valueOf(protocolVersion));
	}
}
