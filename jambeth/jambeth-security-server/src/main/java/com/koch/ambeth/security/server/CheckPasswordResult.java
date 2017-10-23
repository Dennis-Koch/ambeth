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

public class CheckPasswordResult implements ICheckPasswordResult {
	protected final boolean passwordCorrect, changeRecommended, changeRequired, rehashRecommended,
			accountingActive;

	public CheckPasswordResult(boolean passwordCorrect, boolean changeRecommended,
			boolean changeRequired, boolean rehashRecommended, boolean accountingActive) {
		this.passwordCorrect = passwordCorrect;
		this.changeRecommended = changeRecommended;
		this.changeRequired = changeRequired;
		this.rehashRecommended = rehashRecommended;
		this.accountingActive = accountingActive;
	}

	@Override
	public boolean isPasswordCorrect() {
		return passwordCorrect;
	}

	@Override
	public boolean isChangePasswordRecommended() {
		return changeRecommended;
	}

	@Override
	public boolean isChangePasswordRequired() {
		return changeRequired;
	}

	@Override
	public boolean isRehashPasswordRecommended() {
		return rehashRecommended;
	}

	@Override
	public boolean isAccountingActive() {
		return accountingActive;
	}
}
