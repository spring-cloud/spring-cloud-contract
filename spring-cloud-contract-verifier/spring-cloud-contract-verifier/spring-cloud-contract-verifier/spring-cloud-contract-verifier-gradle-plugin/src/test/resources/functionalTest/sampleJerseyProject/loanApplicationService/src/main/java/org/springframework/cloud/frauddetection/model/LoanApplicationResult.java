/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.frauddetection.model;

public class LoanApplicationResult {

	private LoanApplicationStatus loanApplicationStatus;

	private String rejectionReason;

	public LoanApplicationResult() {
	}

	public LoanApplicationResult(LoanApplicationStatus loanApplicationStatus, String rejectionReason) {
		this.loanApplicationStatus = loanApplicationStatus;
		this.rejectionReason = rejectionReason;
	}

	public LoanApplicationStatus getLoanApplicationStatus() {
		return loanApplicationStatus;
	}

	public void setLoanApplicationStatus(LoanApplicationStatus loanApplicationStatus) {
		this.loanApplicationStatus = loanApplicationStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
