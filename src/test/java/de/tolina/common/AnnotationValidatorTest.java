/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

import static de.tolina.common.AnnotationDefinition.type;
import static de.tolina.common.AnnotationValidator.validate;
import static de.tolina.common.TestEnum.TEST;

import org.junit.Test;

/**
 * Test for the {@link AnnotationValidator}
 */
@SuppressWarnings("javadoc")
public class AnnotationValidatorTest {
    @Test
    public void testValidateAnnotatedClass() throws NoSuchMethodException {
        validate().strictly() //
                .annotation(type(TestAnnotation.class) //
                        .param("testparameter", "default") //
                        .param("anotherTestParameter", "one", "two")) //
                .annotation(type(AnnotatedTestInterfaceAnnotation.class)) //
                .annotation(type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
                .annotation(type(AnnotatedAbstractTestClassAnnotation.class)) //
                .forClass(AnnotatedTestClass.class);
    }

    @Test
    public void testValidateAnnotatedMethod() throws NoSuchMethodException {
        validate().exactly() //
                .annotation(type(TestAnnotation.class) //
                        .param("testparameter", "testvalue") //
                        .param("anotherTestParameter", "anotherTestValue")) //
                .annotation(type(AnotherTestAnnotation.class) //
                        .param("testEnum", TEST)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
    }

    @Test
    public void testValidateAnnotatedInterfaceMethod() throws NoSuchMethodException {
        validate().exactly() //
                .annotation(type(AnnotatedTestInterfaceAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getMethod("annotatedInterfaceMethod"));
    }

    @Test
    public void testValidateAnnotatedInterfaceMethodFromSuperclass() throws NoSuchMethodException {
        validate().exactly() //
                .annotation(type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getMethod("annotatedInterfaceMethodForAbstractClass"));
    }

    @Test
    public void testValidateAnnotatedAbstractMethodFromSuperclass() throws NoSuchMethodException {
        validate().exactly() //
                .annotation(type(AnnotatedAbstractTestClassAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getDeclaredMethod("annotatedAbstractMethod"));
    }

    @Test
    public void testValidateAnnotatedField() throws NoSuchFieldException {
        validate().exactly() //
                .annotation(type(TestAnnotation.class) //
                        .param("testparameter", "testvalue") //
                        .param("anotherTestParameter", "one", "two")) //
                .forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));
    }

    @Test
    public void testValidateMethod() throws NoSuchMethodException {
        validate().strictly().forMethod(AnnotatedTestClass.class.getMethod("methodWithoutAnnotations"));
    }

    @Test
    public void testValidateField() throws NoSuchFieldException {
        validate().strictly().forField(AnnotatedTestClass.class.getDeclaredField("fieldWithoutAnnotations"));
    }
}
