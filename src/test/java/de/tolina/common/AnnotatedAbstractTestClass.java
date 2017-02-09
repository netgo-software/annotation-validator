/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

@AnnotatedAbstractTestClassAnnotation
@SuppressWarnings("javadoc")
abstract class AnnotatedAbstractTestClass implements AnnotatedTestInterface, AnnotatedTestInterfaceForAbstractClass {
    @AnnotatedAbstractTestClassAnnotation
    protected abstract void annotatedAbstractMethod();
}
