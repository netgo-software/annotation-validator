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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.assertj.core.util.VisibleForTesting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * API for {@link AnnotationValidator}
 */
public class AnnotationValidation {

    @VisibleForTesting
    HashSet<String> paramBlacklist;
    private List<AnnotationDefinition> annotationDefinitions;
    private ValidationMode validationMode;
    private Annotation[] allAnnotations;
    private static final String
            ACCESS_OR_INVOCATION_EXCEPTION_MESSAGE = "Could not access/invoke aliased method for '%s'.";


    AnnotationValidation(
            @Nonnull final HashSet<String> parametersBlacklist) {
        validationMode = ValidationMode.DEFAULT;
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
    public AnnotationValidation annotation(
            @Nonnull final AnnotationDefinition annotationDefinition) {
        annotationDefinitions.add(annotationDefinition);
        return this;
    }


    /**
     * Validates that no other Annotations are defined and only the defined params are present.
     *
     * @return the AnnotationValidator
     */
    @Nonnull
    public AnnotationValidation exactly() {
        validationMode = ValidationMode.EXACTLY;
        return this;
    }


    /**
     * Validates that no other Annotations are defined considering default values for undefined params.
     *
     * @return the AnnotationValidator
     */
    @Nonnull
    public AnnotationValidation only() {
        validationMode = ValidationMode.ONLY;
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
    public void forClass(
            @Nonnull final Class<?> annotatedClass) {
        forClassOrMethodOrField(annotatedClass);
    }


    public void forConstructor(@Nonnull final Constructor annotatedContructor) {
        forClassOrMethodOrField(annotatedContructor);
    }


    /**
     * Validates Annotations of the given Method and checks that:
     * <br> - all given Annotations are found
     * <br> - no other Annotations are on the given Method
     * <br> - Annotations are in correct order
     *
     * @param annotatedMethod Method to be validated
     */
    public void forMethod(
            @Nonnull final Method annotatedMethod) {
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
    public void forField(
            @Nonnull final Field annotatedField) {
        forClassOrMethodOrField(annotatedField);
    }


    /**
     * Validates the configured Annotations
     *
     * @param annotatedObject can be a Class, a Method or a Field
     */
    private void forClassOrMethodOrField(
            @Nonnull final Object annotatedObject) {
        final SoftAssertions softly = new SoftAssertions();
        final List<String> annotationsList = new ArrayList<>();
        allAnnotations = getAllAnnotationsFor(annotatedObject);

        for (final AnnotationDefinition annotationDefinition : annotationDefinitions) {
            // check if annotation is present
            final Optional<Annotation> foundAnnotation =
                    findAnnotationFor(annotationDefinition.getAnnotation());

            softly.assertThat(foundAnnotation)
                    .as("Expected Annotation %s not found", annotationDefinition.getAnnotation().getName())
                    .isPresent();

            foundAnnotation.ifPresent(annotation -> {
                annotationsList.add(annotation.annotationType().getName());

                // check all methods defined in annotation definition against current annotation's methods
                final List<String> validatedMethods =
                        validateAllMethodsOfAnnotationDefinition(softly, annotationDefinition, annotation);

                // check if there are undefined methods in annotation definition present in annotation
                checkForUndefinedMethodsInAnnotation(softly, annotation, validatedMethods);
            });
        }

        softly.assertThat(validationMode == ValidationMode.DEFAULT && annotationDefinitions.isEmpty())
                .as("Please add at least one Annotation to assert or enable strict validation.")
                .isFalse();

        if (validationMode != ValidationMode.DEFAULT) {
            softly.assertThat(allAnnotations)
                    .extracting(annotation -> annotation.annotationType().getName())
                    .containsExactlyElementsOf(annotationsList);
        }
        try {
            softly.assertAll();
        } catch (final SoftAssertionError sae) {
            throw new SoftAssertionErrorWithObjectDetails(sae.getErrors(), annotatedObject);
        }
    }


    private void addIfNotPresent(
            @Nonnull final Collection<Annotation> collection,
            @Nonnull final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (!collection.contains(annotation)) {
                collection.add(annotation);
            }
        }
    }


    private void checkForUndefinedMethodsInAnnotation(
            @Nonnull final SoftAssertions softly,
            @Nonnull final Annotation annotation,
            @Nonnull final List<String> validatedMethods) {
        final Method[] allMethods = annotation.annotationType().getDeclaredMethods();

        for (final Method declaredMethod : allMethods) {
            // we do not want these to be checked
            final boolean isBlacklisted = paramBlacklist.contains(declaredMethod.getName());
            // skip already validated methods
            final boolean isAlreadyValidated = validatedMethods.contains(declaredMethod.getName());

            if (isBlacklisted || isAlreadyValidated) {
                continue;
            }

            // all methods in current annotation which are not defined in annotation definition or blacklist are to be reported as error
            final Object methodResult;
            try {
                methodResult = declaredMethod.invoke(annotation);

                if (validationMode != ValidationMode.EXACTLY) {
                    Object defaultValue = declaredMethod.getDefaultValue();

                    softly.assertThat(methodResult)
                            .as("Unexpected value for Method '%s' found.", declaredMethod.getName())
                            .isEqualTo(defaultValue);
                } else {
                    if (Object[].class.isInstance(methodResult)) {
                        softly.assertThat((Object[]) methodResult)
                                .as("Unexpected values for %s found.", declaredMethod.getName()).isNullOrEmpty();
                    } else {
                        final String description = "Unexpected value for Method '%s' found.";
                        if (methodResult instanceof String) {
                            softly.assertThat((String) methodResult).as(description, declaredMethod.getName())
                                    .isNullOrEmpty();
                        } else {
                            softly.assertThat(methodResult).as(description, declaredMethod.getName()).isNull();
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                softly.fail(format(ACCESS_OR_INVOCATION_EXCEPTION_MESSAGE, declaredMethod.getName()));
            }
        }
    }


    private boolean equalParamTypes(
            @Nonnull final Class<?>[] typesOne,
            @Nonnull final Class<?>[] typesTwo) {
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
     */
    @Nonnull
    private Optional<Annotation> findAnnotationFor(
            @Nonnull final Class<? extends Annotation> annotation) {
        return Arrays.stream(allAnnotations)
                .filter(annotationFound -> annotationFound.annotationType().getName().equals(annotation.getName()))
                .findAny();
    }


    /**
     * Calls dependent on the type of the given Object:
     */
    @Nullable
    private Annotation[] getAllAnnotationsFor(
            @Nonnull final Object annotated) {
        if (annotated instanceof Field) {
            return ((Field) annotated).getAnnotations();
        }

        if (annotated instanceof Constructor) {
            return ((Constructor) annotated).getAnnotations();
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

                allMethods.stream()
                        .filter(method -> isSameMethod(method, annotatedMethod))
                        .forEachOrdered(method -> addIfNotPresent(allAnnotations, method.getAnnotations()));
            }

            return allAnnotations.toArray(new Annotation[]{});
        }

        final Class<?> annotatedClass = (Class<?>) annotated;
        final List<Class<?>> allClasses = new ArrayList<>();
        allClasses.add(annotatedClass);
        allClasses.addAll(ClassUtils.getAllSuperclasses(annotatedClass));

        final ArrayList<Annotation> allAnnotations = new ArrayList<>();

        for (final Class<?> aClass : allClasses) {
            addIfNotPresent(allAnnotations, aClass.getAnnotations());
            final List<Class<?>> interfaces = ClassUtils.getAllInterfaces(aClass);
            for (final Class<?> anInterface : interfaces) {
                addIfNotPresent(allAnnotations, anInterface.getAnnotations());
            }
        }

        return allAnnotations.toArray(new Annotation[]{});
    }


    private boolean isSameMethod(
            @Nonnull final Method one,
            @Nonnull final Method two) {
        return Objects.equals(one.getName(), two.getName()) && equalParamTypes(one.getParameterTypes(),
                two.getParameterTypes());
    }


    private List<String> validateAllMethodsOfAnnotationDefinition(
            @Nonnull final SoftAssertions softly,
            @Nonnull final AnnotationDefinition annotationDefinition,
            @Nonnull final Annotation annotation) {
        final List<String> validatedMethods = Lists.newArrayList();

        // check all methods defined in annotation definition
        for (final AnnotationDefinition.AnnotationMethodDefinition annotationMethodDefinition : annotationDefinition
                .getAnnotationMethodDefinitions()) {
            final String methodName = annotationMethodDefinition.getMethod();
            final Object[] expectedValues = annotationMethodDefinition.getValues();

            Method actualMethod = null;
            try {
                actualMethod = annotation.annotationType().getMethod(methodName);
            } catch (final NoSuchMethodException e) {
                softly.assertThat(actualMethod).as("Method %s not found.", methodName).isNotNull();
                continue;
            }

            // check if this annotation's actualMethod is an alias
            Method aliasMethod = null;
            Optional<Annotation> aliasForAnnotation = getAliasNameIfPresent(actualMethod);
            if (aliasForAnnotation.isPresent()) {
                try {
                    aliasMethod = getAliasMethod(annotation, aliasForAnnotation.get(), methodName);
                } catch (NoSuchMethodException e) {
                    softly.assertThat(aliasMethod)
                            .as("Referenced alias method %s not found.", aliasForAnnotation.get())
                            .isNotNull();
                    continue;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    softly.fail(format(ACCESS_OR_INVOCATION_EXCEPTION_MESSAGE, actualMethod.getName()));
                }
            }


            // check that actual actualMethod in annotation has defined return types
            final Object actualMethodResult;
            final Object aliasMethodResult;
            Object assertableResult = null;
            try {
                actualMethodResult = actualMethod.invoke(annotation);
                assertableResult = actualMethodResult;

                assertMethodResult(actualMethodResult, expectedValues);
            } catch (IllegalAccessException | InvocationTargetException e) {
                softly.fail(format(ACCESS_OR_INVOCATION_EXCEPTION_MESSAGE, actualMethod.getName()));
            } catch (AssertionError e) {
                if (aliasForAnnotation.isPresent()) {
                    try {
                        aliasMethodResult = aliasMethod.invoke(annotation);

                        assertMethodResult(aliasMethodResult, expectedValues);

                        assertableResult = aliasMethodResult;
                    } catch (IllegalAccessException | InvocationTargetException e1) {
                        softly.fail(ACCESS_OR_INVOCATION_EXCEPTION_MESSAGE, aliasMethod.getName());
                    } catch (AssertionError e1) {
                        // noop
                    }
                }
            }

            if (Object[].class.isInstance(assertableResult)) {
                // this produces readable descriptions on its own
                // all and only defined values must be returned in defined order
                softly.assertThat((Object[]) assertableResult).containsExactlyElementsOf(Arrays.asList(expectedValues));
            } else {
                // this produces readable descriptions on its own
                softly.assertThat(assertableResult).isEqualTo(expectedValues[0]);
            }

            validatedMethods.add(actualMethod.getName());
            if (aliasForAnnotation.isPresent()) {
                validatedMethods.add(aliasMethod.getName());
            }
        }

        return validatedMethods;
    }


    private Method getAliasMethod(Annotation originalAnnotation, Annotation aliasForAnnotation, String methodName)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method annotationMethod = aliasForAnnotation.annotationType().getDeclaredMethod("annotation");
        Class<? extends Annotation> annotationValue =
                (Class<? extends Annotation>) annotationMethod.invoke(aliasForAnnotation);

        String aliasMethodName;
        if (!annotationValue.equals(annotationMethod.getDefaultValue())) {
            aliasMethodName = aliasForAnnotation
                    .annotationType()
                    .getDeclaredMethod("attribute")
                    .invoke(aliasForAnnotation)
                    .toString();

            if (StringUtils.isEmpty(aliasMethodName)) {
                aliasMethodName = methodName;
            }

            return annotationValue.getDeclaredMethod(aliasMethodName);
        } else {
            aliasMethodName = aliasForAnnotation
                    .annotationType()
                    .getDeclaredMethod("value")
                    .invoke(aliasForAnnotation)
                    .toString();

            return originalAnnotation.annotationType().getDeclaredMethod(aliasMethodName);
        }
    }


    private void assertMethodResult(Object actualValues, Object[] expectedValues) {
        if (Object[].class.isInstance(actualValues)) {
            // this produces readable descriptions on its own
            // all and only defined values must be returned in defined order
            assertThat((Object[]) actualValues).containsExactlyElementsOf(Arrays.asList(expectedValues));
        } else {
            // this produces readable descriptions on its own
            assertThat(actualValues).isEqualTo(expectedValues[0]);
        }
    }


    private Optional<Annotation> getAliasNameIfPresent(Method method) {
        return Arrays.stream(method.getDeclaredAnnotations())
                .filter(annotationFound -> annotationFound.annotationType().getName().endsWith("AliasFor"))
                .findAny();
    }

}
