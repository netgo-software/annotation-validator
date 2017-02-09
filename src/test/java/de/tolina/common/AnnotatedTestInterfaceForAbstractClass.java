/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

@AnnotatedTestInterfaceForAbstractClassAnnotation
@SuppressWarnings("javadoc")
interface AnnotatedTestInterfaceForAbstractClass {
    @AnnotatedTestInterfaceForAbstractClassAnnotation
    void annotatedInterfaceMethodForAbstractClass();
}
