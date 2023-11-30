package com.koch.ambeth.server.helloworld.security;

/*-
 * #%L
 * jambeth-server-helloworld
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

import com.koch.ambeth.security.model.IPBEConfiguration;
import com.koch.ambeth.security.model.ISignAndVerify;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PojoSignature implements ISignature, IPBEConfiguration, ISignAndVerify {
    private IUser user;

    private char[] privateKey;

    private char[] publicKey;

    private String signatureAlgorithm;

    private String signatureProvider;

    private String keyFactoryAlgorithm;

    private String keyFactoryProvider;

    private String encryptionAlgorithm;

    private String encryptionKeySpec;

    private char[] encryptionKeyIV;

    private char[] paddedKeySalt;

    private int paddedKeySaltSize;

    private String paddedKeyAlgorithm;

    private int paddedKeySize;

    private int paddedKeyIterations;

    @Override
    public IPBEConfiguration getPBEConfiguration() {
        return this;
    }

    @Override
    public ISignAndVerify getSignAndVerify() {
        return this;
    }
}
