/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Combines an Annotation Class with optional {@link AnnotationMethodDefinition}s
 */
public class AnnotationDefinition {
    private Class annotation;
    private List<AnnotationMethodDefinition> annotationMethodDefinitions;

    /**
     * Describes an Annotation type
     *
     * @param annotatedClass - Type of the Annotation
     */
    @Nonnull
    public static AnnotationDefinition type(@Nonnull Class annotatedClass) {
        return new AnnotationDefinition(annotatedClass);
    }

    private AnnotationDefinition(@Nonnull Class annotation) {
        this.annotation = annotation;
        this.annotationMethodDefinitions = new ArrayList<>();
    }

    /**
     * Describes an Annotation parameter
     *
     * @param method Name of the Method that represents the Parameter
     * @param values Values of the parameter
     */
    @Nonnull
    public AnnotationDefinition param(@Nonnull String method, @Nullable Object... values) {
        AnnotationMethodDefinition annotationMethodDefinition = new AnnotationMethodDefinition(method, values);
        annotationMethodDefinitions.add(annotationMethodDefinition);
        return AnnotationDefinition.this;
    }

    @Nonnull
    Class getAnnotation() {
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

        private AnnotationMethodDefinition(@Nonnull String method, @Nullable Object[] values) {
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
