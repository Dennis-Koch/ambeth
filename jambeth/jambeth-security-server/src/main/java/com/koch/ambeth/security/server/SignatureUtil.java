package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.security.model.ISignAndVerify;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignatureUtil implements ISignatureUtil {

    protected final Object semaphore = new Object();

    @Autowired
    protected IPBEncryptor pbEncryptor;
    @Autowired
    protected ISecureRandom secureRandom;

    @Property(name = SecurityServerConfigurationConstants.SignatureAlgorithmName, defaultValue = "SHA1withECDSA")
    protected String algorithm;

    @Property(name = SecurityServerConfigurationConstants.SignatureProviderName, defaultValue = "BC")
    protected String providerName;

    @Property(name = SecurityServerConfigurationConstants.SignatureKeyAlgorithmName, defaultValue = "ECDSA")
    protected String keyFactoryAlgorithm;

    @Property(name = SecurityServerConfigurationConstants.SignatureKeyProviderName, defaultValue = "BC")
    protected String keyFactoryProviderName;

    @Property(name = SecurityServerConfigurationConstants.SignatureKeySize, defaultValue = "384")
    protected int keySize;

    @Getter(lazy = true)
    private final KeyPairGenerator keyGen = createKeyGen();

    protected KeyPairGenerator createKeyGen() {
        KeyPairGenerator keyGen;
        try {
            if (keyFactoryProviderName == null) {
                keyGen = KeyPairGenerator.getInstance(keyFactoryAlgorithm);
            } else {
                keyGen = KeyPairGenerator.getInstance(keyFactoryAlgorithm, keyFactoryProviderName);
            }
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e,
                    "Error occurred while trying to create " + KeyPairGenerator.class.getSimpleName() + ": " + SecurityServerConfigurationConstants.SignatureKeyAlgorithmName + "='" +
                            keyFactoryAlgorithm + "', " + SecurityServerConfigurationConstants.SignatureKeyProviderName + "='" + keyFactoryProviderName + "'");
        }
        try {
            keyGen.initialize(keySize, secureRandom.getSecureRandomHandle());
            return keyGen;
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e,
                    "Error occurred while trying to initialize " + keyGen + ": " + SecurityServerConfigurationConstants.SignatureKeyAlgorithmName + "='" + keyFactoryAlgorithm + "', " +
                            SecurityServerConfigurationConstants.SignatureKeyProviderName + "='" + keyFactoryProviderName + "', " + SecurityServerConfigurationConstants.SignatureKeySize + "=" +
                            keySize);
        }
    }

    @SneakyThrows
    @Override
    public void generateNewSignature(ISignature newEmptySignature, char[] clearTextPassword) {
        ParamChecker.assertParamNotNull(clearTextPassword, "clearTextPassword");

        var keyGen = getKeyGen();
        
        newEmptySignature.getSignAndVerify().setSignatureAlgorithm(algorithm);
        newEmptySignature.getSignAndVerify().setSignatureProvider(providerName);
        newEmptySignature.getSignAndVerify().setKeyFactoryAlgorithm(keyFactoryAlgorithm);
        newEmptySignature.getSignAndVerify().setKeyFactoryProvider(keyFactoryProviderName);

        KeyPair pair;
        synchronized (semaphore) {
            // cannot be 100 percent sure if the keyGen is thread-safe, so we synchronize it here
            pair = keyGen.generateKeyPair();
        }

        var unencryptedPrivateKey = pair.getPrivate().getEncoded();
        var encryptedPrivateKey = pbEncryptor.encrypt(newEmptySignature.getPBEConfiguration(), true, clearTextPassword, unencryptedPrivateKey);

        newEmptySignature.setPublicKey(Base64.encodeBytes(pair.getPublic().getEncoded()).toCharArray());
        newEmptySignature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
    }

    @SneakyThrows
    @Override
    public void reencryptSignature(ISignature signature, char[] oldClearTextPassword, char[] newClearTextPassword) {
        var encryptedPrivateKey = Base64.decode(signature.getPrivateKey());
        var pbec = signature.getPBEConfiguration();
        var decryptedPrivateKey = pbEncryptor.decrypt(pbec, oldClearTextPassword, encryptedPrivateKey);
        pbec.setPaddedKeyAlgorithm(null);
        pbec.setPaddedKeyIterations(0);
        pbec.setPaddedKeySize(0);
        pbec.setPaddedKeySaltSize(0);
        pbec.setPaddedKeySalt(null);
        pbec.setEncryptionAlgorithm(null);
        pbec.setEncryptionKeySpec(null);
        pbec.setEncryptionKeyIV(null);
        encryptedPrivateKey = pbEncryptor.encrypt(pbec, true, newClearTextPassword, decryptedPrivateKey);
        signature.setPrivateKey(Base64.encodeBytes(encryptedPrivateKey).toCharArray());
    }

    @SneakyThrows
    @Override
    public Signature createSignatureHandle(ISignAndVerify signAndVerify, byte[] privateKey) {
        // use the private key to create the signature handle
        var decryptedPrivateKeySpec = new PKCS8EncodedKeySpec(privateKey);
        var keyFactoryProvider = signAndVerify.getKeyFactoryProvider();
        var keyFactory =
                keyFactoryProvider != null ? KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm(), keyFactoryProvider) : KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
        var privateKeyHandle = keyFactory.generatePrivate(decryptedPrivateKeySpec);
        var signatureProvider = signAndVerify.getSignatureProvider();
        var signature = signatureProvider != null ? java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm(), signatureProvider) :
                java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm());
        signature.initSign(privateKeyHandle, secureRandom.getSecureRandomHandle());
        return signature;
    }

    @SneakyThrows
    @Override
    public Signature createVerifyHandle(ISignAndVerify signAndVerify, byte[] publicKey) {
        // use the public key to create the signature handle
        var keySpec = new X509EncodedKeySpec(publicKey);
        var keyFactoryProvider = signAndVerify.getKeyFactoryProvider();
        var keyFactory =
                keyFactoryProvider != null ? KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm(), keyFactoryProvider) : KeyFactory.getInstance(signAndVerify.getKeyFactoryAlgorithm());
        var publicKeyHandle = keyFactory.generatePublic(keySpec);
        var signatureProvider = signAndVerify.getSignatureProvider();
        var signature = signatureProvider != null ? java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm(), signatureProvider) :
                java.security.Signature.getInstance(signAndVerify.getSignatureAlgorithm());
        signature.initVerify(publicKeyHandle);
        return signature;
    }


}
