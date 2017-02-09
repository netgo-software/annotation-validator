/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

@AnnotatedTestInterfaceAnnotation
@SuppressWarnings("javadoc")
interface AnnotatedTestInterface {
    @AnnotatedTestInterfaceAnnotation
    void annotatedInterfaceMethod();
}
