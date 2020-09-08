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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;

import static de.jakop.validation.annotations.TestEnum.TEST;
import static de.jakop.validation.annotations.TestEnum.TEST2;

/**
 * Test for the {@link AnnotationValidator}
 */
public class AnnotationValidationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testValidateAnnotatedClass_exactly_defaultsAreNotEvaluated() throws NoSuchMethodException {
        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("testparameter", "default") //
                        .param("anotherTestParameter", "one", "two")) //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceAnnotation.class)) //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
                .annotation(AnnotationDefinition.type(AnnotatedAbstractTestClassAnnotation.class)) //
                .forClass(AnnotatedTestClass.class);
    }

    @Test
    public void testValidateAnnotatedClass_only_defaultsAreEvaluated() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class)) //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceAnnotation.class)) //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
                .annotation(AnnotationDefinition.type(AnnotatedAbstractTestClassAnnotation.class)) //
                .forClass(AnnotatedTestClass.class);
    }


    @Test
    public void testValidateAnnotatedClass_NotExactlyAndNoAnnotationsValidated() throws NoSuchMethodException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Please add at least one Annotation to assert or enable strict validation.");
        AnnotationValidator.validate() //
                .forClass(AnnotatedTestClass.class);
    }


    @Test
    public void testValidateAnnotatedClass_NotExactly() throws NoSuchMethodException {
        AnnotationValidator.validate() //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceAnnotation.class)) //
                .forClass(AnnotatedTestClass.class);
    }


    @Test
    public void testValidateAnnotatedClass_NoSuchAnnotationMethod() throws NoSuchMethodException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Method noSuchMethod not found");

        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("noSuchMethod", "default")) //
                .forClass(AnnotatedTestClass.class);
    }


    @Test
    public void testValidateAnnotatedMethod_exactly_defaultsAreNotEvaluated() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("testparameter", "testvalue") //
                        .param("anotherTestParameter", "anotherTestValue")) //
                .annotation(AnnotationDefinition.type(AnotherTestAnnotation.class) //
                        .param("testEnum", TEST2)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
    }


    @Test
    public void testValidateAnnotatedMethod_only_defaultsAreEvaluated() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("testparameter", "testvalue") //
                        .param("anotherTestParameter", "anotherTestValue")) //
                .annotation(AnnotationDefinition.type(AnotherTestAnnotation.class) //
                        .param("testEnum", TEST2)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
    }


    @Test
    public void testValidateAnnotatedMethod_NotAllAnnotationMethodsDefinedInAnnotationDefinition()
            throws NoSuchMethodException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Unexpected value for Method 'testparameter' found");

        AnnotationValidator.validate() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("anotherTestParameter", "anotherTestValue")) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
    }


    @Test
    public void testValidateAnnotatedMethod_NotAllAnnotationMethodsDefinedInAnnotationDefinition_NonStringValues()
            throws NoSuchMethodException {
        AnnotationValidator.validate() //
                .annotation(AnnotationDefinition.type(AnotherTestAnnotation.class).param("value", TEST2)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
    }


    @Test
    public void testValidateAnnotatedMethod_OverloadedMethod() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(AnotherTestAnnotation.class) //
                        .param("testEnum", TEST) //
                        .param("value", TEST)) //
                .forMethod(AnnotatedTestClass.class.getMethod("overloadedMethod", String.class, String.class));
    }


    @Test
    public void testValidateAnnotatedMethod_UseAlias() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(AliasTestAnnotation.class) //
                        .param("referencedTestEnum", TEST2)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAliasAnnotations"));
    }


    @Test
    public void testValidateAnnotatedMethod_UseAlias_WithoutAttribute() throws NoSuchMethodException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(AliasTestAnnotation.class) //
                        .param("anotherValue", TEST)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithAnOtherAliasAnnotations"));
    }


    @Test
    public void testValidateAnnotatedInterfaceMethod() throws NoSuchMethodException {
        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getMethod("annotatedInterfaceMethod"));
    }


    @Test
    public void testValidateAnnotatedInterfaceMethodFromSuperclass() throws NoSuchMethodException {
        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getMethod("annotatedInterfaceMethodForAbstractClass"));
    }


    @Test
    public void testValidateAnnotatedAbstractMethodFromSuperclass() throws NoSuchMethodException {
        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(AnnotatedAbstractTestClassAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getDeclaredMethod("annotatedAbstractMethod"));
    }


    @Test
    public void testValidateAnnotatedField_exactly_defaultsAreNotEvaluated() throws NoSuchFieldException {
        AnnotationValidator.validate().exactly() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("testparameter", "testvalue")
                        .param("anotherTestParameter", "one", "two")) //
                .forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));
    }


    @Test
    public void testValidateAnnotatedField_only_defaultsAreEvaluated() throws NoSuchFieldException {
        AnnotationValidator.validate().only() //
                .annotation(AnnotationDefinition.type(TestAnnotation.class) //
                        .param("testparameter", "testvalue")) //
                .forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));
    }


    @Test
    public void testValidateMethod() throws NoSuchMethodException {
        AnnotationValidator.validate().exactly()//
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithoutAnnotations"));
    }


    @Test
    public void testValidateMethod_AnnotationNotPresent() throws NoSuchMethodException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Expected Annotation de.jakop.validation.annotations.TestAnnotation not found");
        AnnotationValidator.validate().exactly()//
                .annotation(AnnotationDefinition.type(TestAnnotation.class)) //
                .forMethod(AnnotatedTestClass.class.getMethod("methodWithoutAnnotations"));
    }


    @Test
    public void testValidateMethod_NoSuchMethod() throws NoSuchMethodException {
        thrown.expect(NoSuchMethodException.class);
        AnnotationValidator.validate().exactly()//
                .forMethod(AnnotatedTestClass.class.getMethod("noSuchMethod"));
    }


    @Test
    public void testValidateField() throws NoSuchFieldException {
        AnnotationValidator.validate().exactly().forField(AnnotatedTestClass.class.getDeclaredField("fieldWithoutAnnotations"));
    }


    @Test
    public void testValidateLambdas() throws Exception {
        TestInterface test1 = TestInterface::staticMethod;
        Method annotatedMethod1 = test1.getClass().getMethod("method");
        AnnotationValidator.validate().annotation(AnnotationDefinition.type(Deprecated.class)).forMethod(annotatedMethod1);

        TestInterface test2 = test1::defaultMethod;
        Method annotatedMethod2 = test2.getClass().getMethod("defaultMethod");
        AnnotationValidator.validate().annotation(AnnotationDefinition.type(Deprecated.class)).forMethod(annotatedMethod2);
    }


    interface TestInterface {

        @Deprecated
        static void staticMethod() {
            System.out.println("staticMethod");
        }

        @Deprecated
        void method();

        @Deprecated
        default void defaultMethod() {
            System.out.println("defaultMethod");

        }
    }
}
