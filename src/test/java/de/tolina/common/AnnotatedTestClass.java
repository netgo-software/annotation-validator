/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

import static de.tolina.common.TestEnum.TEST;

@TestAnnotation
@SuppressWarnings("javadoc")
class AnnotatedTestClass extends AnnotatedAbstractTestClass implements AnnotatedTestInterface {

    @TestAnnotation(testparameter = "testvalue")
    private String fieldWithAnnotations;

    private String fieldWithoutAnnotations;

    @TestAnnotation(testparameter = "testvalue", anotherTestParameter = "anotherTestValue")
    @AnotherTestAnnotation(TEST)
    public void methodWithAnnotations() {
        // noop
    }

    public void methodWithoutAnnotations() {
        // noop
    }

    @Override
    public void annotatedInterfaceMethod() {
        // noop
    }

    @Override
    public void annotatedInterfaceMethodForAbstractClass() {
        // noop
    }

    @Override
    protected void annotatedAbstractMethod() {
        // noop
    }
}
