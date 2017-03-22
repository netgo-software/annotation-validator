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
 */
package de.tolina.common.validation;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.ClassUtils;
import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * API for {@link AnnotationValidator}
 */
public class AnnotationValidation {
	@VisibleForTesting
	HashSet<String> paramBlacklist;
	private List<AnnotationDefinition> annotationDefinitions;
	private boolean strictValidation;

	AnnotationValidation(@Nonnull final HashSet<String> parametersBlacklist) {
		strictValidation = false;
		paramBlacklist = parametersBlacklist;
		annotationDefinitions = new ArrayList<>();
	}

	/**
	 * Adds an {@link AnnotationDefinition} to the Validator
	 *
	 * @param annotationDefinition the Annotation
	 * @return the AnnotationValidator
	 */
	@Nonnull
	public AnnotationValidation annotation(@Nonnull final AnnotationDefinition annotationDefinition) {
		annotationDefinitions.add(annotationDefinition);
		return this;
	}

	/**
	 * Validates that no other Annotations are defined
	 *
	 * @return the AnnotationValidator
	 */
	@Nonnull
	public AnnotationValidation exactly() {
		strictValidation = true;
		return this;
	}

	/**
	 * Validates Annotations of the given Class and checks that:
	 * <br> - all given Annotations are found
	 * <br> - no other Annotations are on the given Class
	 * <br> - Annotations are in correct order
	 *
	 * @param annotatedClass Class to be validated
	 */
	public void forClass(@Nonnull final Class<?> annotatedClass) {
		forClassOrMethodOrField(annotatedClass);
	}

	/**
	 * Validates Annotations of the given Method and checks that:
	 * <br> - all given Annotations are found
	 * <br> - no other Annotations are on the given Method
	 * <br> - Annotations are in correct order
	 *
	 * @param annotatedMethod Method to be validated
	 */
	public void forMethod(@Nonnull final Method annotatedMethod) {
		forClassOrMethodOrField(annotatedMethod);
	}

	/**
	 * Validates Annotations of the given Field and checks that:
	 * <br> - all given Annotations are found
	 * <br> - no other Annotations are on the given Filed
	 * <br> - Annotations are in correct order
	 *
	 * @param annotatedField Field to be validated
	 */
	public void forField(@Nonnull final Field annotatedField) {
		forClassOrMethodOrField(annotatedField);
	}

	/**
	 * Validates the configured Annotations
	 *
	 * @param annotatedObject can be a Class, a Method or a Field
	 */
	private void forClassOrMethodOrField(@Nonnull final Object annotatedObject) {
		final SoftAssertions softly = new SoftAssertions();
		final List<String> annotationsList = new ArrayList<>();
		for (final AnnotationDefinition annotationDefinition : annotationDefinitions) {
			// check if annotation is present
			final Annotation annotation = findAnnotationFor(annotatedObject, annotationDefinition.getAnnotation());
			softly.assertThat(annotation).as("Expected Annotation %s not found", annotationDefinition.getAnnotation().getName()).isNotNull();

			if (annotation == null) {
				continue;
			}

			annotationsList.add(annotation.annotationType().getName());

			// check all methods defined in annotation definition against current annotation's methods
			final List<String> validatedMethods = validateAllMethodsOfAnnotationDefinition(softly, annotationDefinition, annotation);

			// check if there are undefined methods in annotation definition present in annotation
			checkForUndefinedMethodsInAnnotation(softly, annotation, validatedMethods);
		}

		softly.assertThat(!strictValidation && CollectionUtils.isEmpty(annotationDefinitions)).as("Please add at least one Annotation to assert or enable strict validation.")
				.isFalse();

		if (strictValidation) {
			final Annotation[] allAnnotations = getAllAnnotationsFor(annotatedObject);
			softly.assertThat(allAnnotations).extracting(annotation -> annotation.annotationType().getName()).containsExactlyElementsOf(annotationsList);
		}
		try {
			softly.assertAll();
		} catch (final SoftAssertionError sae) {
			throw new SoftAssertionErrorWithObjectDetails(sae.getErrors(), annotatedObject);
		}
	}

	private void addIfNotPresent(@Nonnull final Collection<Annotation> collection, @Nonnull final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (!collection.contains(annotation)) {
				collection.add(annotation);
			}
		}
	}

	private void checkForUndefinedMethodsInAnnotation(@Nonnull final SoftAssertions softly, @Nonnull final Annotation annotation, @Nonnull final List<String> validatedMethods) {
		final Method[] allMethods = annotation.getClass().getDeclaredMethods();

		for (final Method declaredMethod : allMethods) {
			// we do not want these to be checked
			final boolean isBlacklisted = paramBlacklist.contains(declaredMethod.getName());
			// skip already validated methods
			final boolean isAlreadyValidated = validatedMethods.contains(declaredMethod.getName());

			if (isBlacklisted || isAlreadyValidated) {
				continue;
			}

			// all methods in current annotation which are not defined in annotation definition or blacklist are to be reported as error
			final Object methodResult = ReflectionUtils.invokeMethod(declaredMethod, annotation);
			if (Object[].class.isInstance(methodResult)) {
				softly.assertThat((Object[]) methodResult).as("Unexpected values for %s found.", declaredMethod.getName()).isNullOrEmpty();
			} else {
				final String description = "Unexpected value for Method '%s' found.";
				if (methodResult instanceof String) {
					softly.assertThat((String) methodResult).as(description, declaredMethod.getName()).isNullOrEmpty();
				} else {
					softly.assertThat(methodResult).as(description, declaredMethod.getName()).isNull();
				}
			}
		}
	}

	private boolean equalParamTypes(@Nonnull final Class<?>[] typesOne, @Nonnull final Class<?>[] typesTwo) {
		if (typesOne.length == typesTwo.length) {
			for (int i = 0; i < typesOne.length; i++) {
				if (typesOne[i] != typesTwo[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Calls dependent on the type of the given Object:
	 * <br> - {@link AnnotationUtils#findAnnotation(Method, Class)} or
	 * <br> - {@link AnnotationUtils#findAnnotation(java.lang.reflect.AnnotatedElement, Class)} or
	 * <br> - {@link AnnotationUtils#findAnnotation(Class, Class)}
	 */
	@Nullable
	private Annotation findAnnotationFor(@Nonnull final Object annotated, @Nonnull final Class<? extends Annotation> annotation) {
		if (annotated instanceof Method) {
			return findAnnotation((Method) annotated, annotation);
		}
		if (annotated instanceof Field) {
			return findAnnotation((Field) annotated, annotation);
		}
		return findAnnotation((Class<?>) annotated, annotation);
	}

	/**
	 * Calls dependent on the type of the given Object:
	 * <br> - {@link AnnotationUtils#getAnnotations(Method)} or
	 * <br> - {@link AnnotationUtils#getAnnotations(java.lang.reflect.AnnotatedElement)}
	 */
	@Nullable
	private Annotation[] getAllAnnotationsFor(@Nonnull final Object annotated) {
		if (annotated instanceof Field) {
			return getAnnotations((Field) annotated);
		}

		if (annotated instanceof Method) {
			final Method annotatedMethod = (Method) annotated;
			final Class<?> declaringClass = annotatedMethod.getDeclaringClass();
			final List<Class<?>> allClasses = new ArrayList<>();
			allClasses.add(declaringClass);
			allClasses.addAll(ClassUtils.getAllSuperclasses(declaringClass));

			final ArrayList<Annotation> allAnnotations = new ArrayList<>();

			for (final Class<?> aClass : allClasses) {
				final ArrayList<Method> allMethods = new ArrayList<>();
				allMethods.addAll(Arrays.asList(aClass.getDeclaredMethods()));

				final List<Class<?>> interfaces = ClassUtils.getAllInterfaces(aClass);
				for (final Class<?> anInterface : interfaces) {
					allMethods.addAll(Arrays.asList(anInterface.getDeclaredMethods()));
				}

				allMethods.stream().filter(method -> isSameMethod(method, annotatedMethod)).forEachOrdered(method -> addIfNotPresent(allAnnotations, getAnnotations(method)));
			}

			return allAnnotations.toArray(new Annotation[] {});
		}

		final Class<?> annotatedClass = (Class<?>) annotated;
		final List<Class<?>> allClasses = new ArrayList<>();
		allClasses.add(annotatedClass);
		allClasses.addAll(ClassUtils.getAllSuperclasses(annotatedClass));

		final ArrayList<Annotation> allAnnotations = new ArrayList<>();

		for (final Class<?> aClass : allClasses) {
			addIfNotPresent(allAnnotations, getAnnotations(aClass));
			final List<Class<?>> interfaces = ClassUtils.getAllInterfaces(aClass);
			for (final Class<?> anInterface : interfaces) {
				addIfNotPresent(allAnnotations, getAnnotations(anInterface));
			}
		}

		return allAnnotations.toArray(new Annotation[] {});
	}

	private boolean isSameMethod(@Nonnull final Method one, @Nonnull final Method two) {
		return Objects.equals(one.getName(), two.getName()) && equalParamTypes(one.getParameterTypes(), two.getParameterTypes());
	}

	private List<String> validateAllMethodsOfAnnotationDefinition(@Nonnull final SoftAssertions softly, @Nonnull final AnnotationDefinition annotationDefinition,
			@Nonnull final Annotation annotation) {
		final List<String> validatedMethods = Lists.newArrayList();

		// check all methods defined in annotation definition
		for (final AnnotationDefinition.AnnotationMethodDefinition annotationMethodDefinition : annotationDefinition.getAnnotationMethodDefinitions()) {
			final String methodName = annotationMethodDefinition.getMethod();
			final Object[] values = annotationMethodDefinition.getValues();

			Method method = null;
			try {
				method = annotation.annotationType().getMethod(methodName);
			} catch (final NoSuchMethodException e) {
				// noop
			}

			// check that defined method is present in annotation
			softly.assertThat(method).as("Method %s not found.", methodName).isNotNull();

			if (method == null) {
				continue;
			}

			// check that actual method in annotation has defined return types
			final Object methodResult = ReflectionUtils.invokeMethod(method, annotation);
			if (Object[].class.isInstance(methodResult)) {
				// this produces readable descriptions on its own
				// all and only defined values must be returned in defined order
				softly.assertThat((Object[]) methodResult).containsExactlyElementsOf(Arrays.asList(values));
			} else {
				// this produces readable descriptions on its own
				softly.assertThat(methodResult).isEqualTo(values[0]);
			}
			validatedMethods.add(method.getName());
			// check if this annotation's method is an alias
			final AliasFor alias = AnnotationUtils.findAnnotation(method, AliasFor.class);
			if (alias != null) {
				// mark the aliased method as validated
				validatedMethods.add(alias.value());
			}

		}
		return validatedMethods;
	}

}
