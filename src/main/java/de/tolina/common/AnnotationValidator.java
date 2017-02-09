/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Annotation Validation
 */
public class AnnotationValidator {
    private final HashSet<String> paramBlacklist;
    private List<AnnotationDefinition> annotationDefinitions;
    private boolean strictValidation;

    private AnnotationValidator(@Nonnull HashSet<String> paramBlacklist) {
        strictValidation = false;
        this.paramBlacklist = paramBlacklist;
        annotationDefinitions = new ArrayList<>();
    }

    /**
     * Returns an AnnotationValidator
     * <br> - uses SoftAssertions
     * <br> - validates all given parameters for an Annotation and fails if there are differences
     * <br> - respects parameter aliases
     *
     * @param ignoreParams Annotation params to ignore
     * @return ClassAnnotationValidator to validate for a specific Class
     */
    @Nonnull
    public static AnnotationValidator validate(@Nullable String... ignoreParams) {
        HashSet<String> paramBlacklist = new HashSet<>();
        paramBlacklist.add("equals");
        paramBlacklist.add("toString");
        paramBlacklist.add("hashCode");
        paramBlacklist.add("annotationType");
        if (ignoreParams != null) {
            Collections.addAll(paramBlacklist, ignoreParams);
        }
        return new AnnotationValidator(paramBlacklist);
    }

    /**
     * Adds an {@link AnnotationDefinition} to the Validator
     *
     * @param annotationDefinition the Annotation
     * @return the AnnotationValidator
     */
    @Nonnull
    public AnnotationValidator annotation(@Nonnull AnnotationDefinition annotationDefinition) {
        annotationDefinitions.add(annotationDefinition);
        return AnnotationValidator.this;
    }

    /**
     * Validates Annotations of the given Class and checks that:
     * <br> - all given Annotations are found
     * <br> - no other Annotations are on the given Class
     * <br> - Annotations are in correct order
     *
     * @param annotatedClass Class to be validated
     */
    public void forClass(@Nonnull Class annotatedClass) {
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
    public void forMethod(@Nonnull Method annotatedMethod) {
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
    public void forField(@Nonnull Field annotatedField) {
        forClassOrMethodOrField(annotatedField);
    }

    /**
     * Validates that no other Annotations are defined
     *
     * @return the AnnotationValidator
     */
    @Nonnull
    public AnnotationValidator strictly() {
        return exactly();
    }

    /**
     * Validates that no other Annotations are defined
     *
     * @return the AnnotationValidator
     */
    @Nonnull
    public AnnotationValidator exactly() {
        strictValidation = true;
        return this;
    }

    /**
     * Validates the configured Annotations
     *
     * @param annotatedObject can be a Class, a Method or a Field
     */
    private void forClassOrMethodOrField(@Nonnull Object annotatedObject) {
        SoftAssertions softly = new SoftAssertions();
        List<String> annotationsList = new ArrayList<>();
        for (AnnotationDefinition annotationDefinition : annotationDefinitions) {
            Annotation annotation = findAnnotationFor(annotatedObject, annotationDefinition.getAnnotation());
            softly.assertThat(annotation).as("Expected Annotation %s not found", annotationDefinition.getAnnotation().getName()).isNotNull();

            if (annotation == null) {
                continue;
            }

            annotationsList.add(annotation.annotationType().getName());

            ArrayList<String> validatedMethods = new ArrayList<>();
            for (AnnotationDefinition.AnnotationMethodDefinition annotationMethodDefinition : annotationDefinition.getAnnotationMethodDefinitions()) {
                String methodName = annotationMethodDefinition.getMethod();
                Object[] values = annotationMethodDefinition.getValues();

                Method method = null;
                try {
                    method = annotation.annotationType().getMethod(methodName);
                } catch (NoSuchMethodException e) {
                    // noop
                }

                softly.assertThat(method).as("Method %s not found.", methodName).isNotNull();

                if (method == null) {
                    continue;
                }

                Object methodResult = ReflectionUtils.invokeMethod(method, annotation);
                if (Object[].class.isInstance(methodResult)) {
                    // this produces readable descriptions on its own
                    softly.assertThat((Object[]) methodResult).containsExactlyElementsOf(Arrays.asList(values));
                } else {
                    // this produces readable descriptions on its own
                    softly.assertThat(methodResult).isEqualTo(values[0]);
                }
                validatedMethods.add(method.getName());
                AliasFor alias = AnnotationUtils.findAnnotation(method, AliasFor.class);
                if (alias != null) {
                    validatedMethods.add(alias.value());
                }

            }

            Method[] allMethods = annotation.getClass().getDeclaredMethods();

            for (Method declaredMethod : allMethods) {
                if (paramBlacklist.contains(declaredMethod.getName())) {
                    continue;
                }
                if (!validatedMethods.contains(declaredMethod.getName())) {
                    AliasFor alias = AnnotationUtils.findAnnotation(declaredMethod, AliasFor.class);
                    if (alias != null) {
                        if (validatedMethods.contains(alias.value())) {
                            continue;
                        }
                    }
                    Object methodResult = ReflectionUtils.invokeMethod(declaredMethod, annotation);
                    if (Object[].class.isInstance(methodResult)) {
                        softly.assertThat((Object[]) methodResult).as("Unexpected values for %s found.", declaredMethod.getName()).isNullOrEmpty();
                    } else {
                        String description = "Unexpected value for Method '%s' found.";
                        if (methodResult instanceof String) {
                            softly.assertThat((String) methodResult).as(description, declaredMethod.getName()).isNullOrEmpty();
                        } else {
                            softly.assertThat(methodResult).as(description, declaredMethod.getName()).isNull();
                        }
                    }
                }
            }

        }

        if (!strictValidation && CollectionUtils.isEmpty(annotationDefinitions)) {
            softly.assertThat(!strictValidation && CollectionUtils.isEmpty(annotationDefinitions)).as("Please add at least one Annotation to assert or enable strict validation.")
                    .isFalse();
        }

        if (strictValidation) {
            Annotation[] allAnnotations = getAllAnnotationsFor(annotatedObject);
            softly.assertThat(allAnnotations).extracting(annotation -> annotation.annotationType().getName()).containsExactlyElementsOf(annotationsList);
        }
        try {
            softly.assertAll();
        } catch (SoftAssertionError sae) {
            throw new SoftAssertionError(sae.getErrors()) {
                // if you are in a test loop and do not know witch object is under test, let's add this information to errormessage   
                @Override
                public String getMessage() {
                    return ("\nError on Validating " + annotatedObject + "\n" + super.getMessage());
                }
            };
        }
    }

    /**
     * Calls dependent on the type of the given Object:
     * <br> - {@link AnnotationUtils#findAnnotation(Method, Class)} or
     * <br> - {@link AnnotationUtils#findAnnotation(AnnotatedElement, Class)} or
     * <br> - {@link AnnotationUtils#findAnnotation(Class, Class)}
     */
    @Nullable
    private Annotation findAnnotationFor(@Nonnull Object annotated, @Nonnull Class annotation) {
        if (annotated instanceof Method) {
            return findAnnotation((Method) annotated, annotation);
        }
        if (annotated instanceof Field) {
            return findAnnotation((Field) annotated, annotation);
        }
        return findAnnotation((Class) annotated, annotation);
    }

    /**
     * Calls dependent on the type of the given Object:
     * <br> - {@link AnnotationUtils#getAnnotations(Method)} or
     * <br> - {@link AnnotationUtils#getAnnotations(AnnotatedElement)}
     */
    @Nullable
    private Annotation[] getAllAnnotationsFor(@Nonnull Object annotated) {
        if (annotated instanceof Field) {
            return getAnnotations((Field) annotated);
        }

        if (annotated instanceof Method) {
            Method annotatedMethod = ((Method) annotated);
            Class declaringClass = annotatedMethod.getDeclaringClass();
            List<Class<?>> allClasses = new ArrayList<>();
            allClasses.add(declaringClass);
            allClasses.addAll(ClassUtils.getAllSuperclasses(declaringClass));

            ArrayList<Annotation> allAnnotations = new ArrayList<>();

            for (Class<?> aClass : allClasses) {
                ArrayList<Method> allMethods = new ArrayList<>();
                CollectionUtils.addAll(allMethods, aClass.getDeclaredMethods());

                List<Class<?>> interfaces = ClassUtils.getAllInterfaces(aClass);
                for (Class anInterface : interfaces) {
                    CollectionUtils.addAll(allMethods, anInterface.getDeclaredMethods());
                }

                allMethods.stream().filter(method -> isSameMethod(method, annotatedMethod)).forEachOrdered(method -> addIfNotPresent(allAnnotations, getAnnotations(method)));
            }

            return allAnnotations.toArray(new Annotation[] {});
        }

        Class annotatedClass = (Class) annotated;
        List<Class<?>> allClasses = new ArrayList<>();
        allClasses.add(annotatedClass);
        allClasses.addAll(ClassUtils.getAllSuperclasses(annotatedClass));

        ArrayList<Annotation> allAnnotations = new ArrayList<>();

        for (Class<?> aClass : allClasses) {
            addIfNotPresent(allAnnotations, getAnnotations(aClass));
            List<Class<?>> interfaces = ClassUtils.getAllInterfaces(aClass);
            for (Class anInterface : interfaces) {
                addIfNotPresent(allAnnotations, getAnnotations(anInterface));
            }
        }

        return allAnnotations.toArray(new Annotation[] {});
    }

    private void addIfNotPresent(@Nonnull Collection<Annotation> collection, @Nonnull Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (!collection.contains(annotation)) {
                collection.add(annotation);
            }
        }
    }

    private boolean isSameMethod(@Nonnull Method one, @Nonnull Method two) {
        return (Objects.equals(one.getName(), two.getName())) && one.getReturnType().equals(two.getReturnType())
                && equalParamTypes(one.getParameterTypes(), two.getParameterTypes());
    }

    private boolean equalParamTypes(@Nonnull Class<?>[] typesOne, @Nonnull Class<?>[] typesTwo) {
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
}
