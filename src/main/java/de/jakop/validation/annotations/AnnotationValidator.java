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
package de.jakop.validation.annotations;

import java.util.HashSet;

import javax.annotation.Nonnull;

/**
 * Annotation Validation
 */
public class AnnotationValidator {

	private AnnotationValidator() {
		// only via factory method
	}

	/**
	 * Returns an AnnotationValidation
	 * <br> - uses SoftAssertions
	 * <br> - validates all given parameters for an Annotation and fails if there are differences
	 * <br> - respects parameter aliases
	 *
	 * @return ClassAnnotationValidator to validate for a specific Class
	 */
	@Nonnull
	public static AnnotationValidation validate() {
		final HashSet<String> paramBlacklist = new HashSet<>();
		paramBlacklist.add("equals");
		paramBlacklist.add("toString");
		paramBlacklist.add("hashCode");
		paramBlacklist.add("annotationType");

		return new AnnotationValidation(paramBlacklist);
	}
}
