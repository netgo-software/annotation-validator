package de.tolina.common.validation;

import java.lang.annotation.Annotation;
/*-
 * #%L
 * Annotation Validator
 * %%
 * Copyright (C) 2016 - 2017 arxes-tolina GmbH
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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Combines an Annotation Class with optional {@link AnnotationMethodDefinition}s
 */
public class AnnotationDefinition {
	private Class<? extends Annotation> annotation;
	private List<AnnotationMethodDefinition> annotationMethodDefinitions;

	/**
	 * Describes an Annotation type
	 *
	 * @param annotatedClass - Type of the Annotation
	 */
	@Nonnull
	public static AnnotationDefinition type(@Nonnull final Class<? extends Annotation> annotatedClass) {
		return new AnnotationDefinition(annotatedClass);
	}

	private AnnotationDefinition(@Nonnull final Class<? extends Annotation> annotation) {
		this.annotation = annotation;
		annotationMethodDefinitions = new ArrayList<>();
	}

	/**
	 * Describes an Annotation parameter
	 *
	 * @param method Name of the Method that represents the Parameter
	 * @param values Values of the parameter
	 */
	@Nonnull
	public AnnotationDefinition param(@Nonnull final String method, @Nullable final Object... values) {
		final AnnotationMethodDefinition annotationMethodDefinition = new AnnotationMethodDefinition(method, values);
		annotationMethodDefinitions.add(annotationMethodDefinition);
		return AnnotationDefinition.this;
	}

	@Nonnull
	Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

	@Nonnull
	List<AnnotationMethodDefinition> getAnnotationMethodDefinitions() {
		return annotationMethodDefinitions;
	}

	/**
	 * Holds Method names and Parameters
	 */
	static class AnnotationMethodDefinition {
		private String method;
		private Object[] values;

		private AnnotationMethodDefinition(@Nonnull final String method, @Nullable final Object[] values) {
			this.method = method;
			this.values = values;
		}

		@Nonnull
		String getMethod() {
			return method;
		}

		@Nullable
		Object[] getValues() {
			return values;
		}
	}
}
