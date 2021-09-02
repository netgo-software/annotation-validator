/**
 * Copyright © 2016 arxes-tolina GmbH (entwicklung@arxes-tolina.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (C) 2020 Frank Jakop
 */
/*
 *  (c) tolina GmbH, 2017
 */
package de.jakop.validation.annotations;

import java.util.List;

import org.assertj.core.api.SoftAssertionError;

final class SoftAssertionErrorWithObjectDetails extends SoftAssertionError {
	private static final long serialVersionUID = 5527685338875086360L;
	private final String annotatedObject;

	SoftAssertionErrorWithObjectDetails(final List<String> errors, final Object annotatedObject) {
		super(errors);
		this.annotatedObject = annotatedObject.toString();
	}

	// if you are in a test loop and do not know witch object is under test, let's add this information to errormessage
	@Override
	public String getMessage() {
		return "\nError on Validating " + annotatedObject + "\n" + super.getMessage();
	}
}
