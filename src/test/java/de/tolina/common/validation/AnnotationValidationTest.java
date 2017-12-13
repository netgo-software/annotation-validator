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

import static de.tolina.common.validation.AnnotationDefinition.type;
import static de.tolina.common.validation.AnnotationValidator.validate;
import static de.tolina.common.validation.TestEnum.TEST;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test for the {@link AnnotationValidator}
 */
public class AnnotationValidationTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testValidateAnnotatedClass() throws NoSuchMethodException {

		AnnotationValidator.validate().exactly() //
				.annotation(AnnotationDefinition.type(TestAnnotation.class) //
						.param("testparameter", "default") //
						.param("anotherTestParameter", "one", "two")) //
				.annotation(type(AnnotatedTestInterfaceAnnotation.class)) //
				.annotation(type(AnnotatedTestInterfaceForAbstractClassAnnotation.class)) //
				.annotation(type(AnnotatedAbstractTestClassAnnotation.class)) //
				.forClass(AnnotatedTestClass.class);
	}

	@Test
	public void testValidateAnnotatedClass_NotExactlyAndNoAnnotationsValidated() throws NoSuchMethodException {

		thrown.expect(AssertionError.class);
		thrown.expectMessage("Please add at least one Annotation to assert or enable strict validation.");
		validate() //
				.forClass(AnnotatedTestClass.class);
	}

	@Test
	public void testValidateAnnotatedClass_NotExactly() throws NoSuchMethodException {
		validate() //
				.annotation(type(AnnotatedTestInterfaceAnnotation.class)) //
				.forClass(AnnotatedTestClass.class);
	}

	@Test
	public void testValidateAnnotatedClass_NoSuchAnnotationMethod() throws NoSuchMethodException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Method noSuchMethod not found");

		validate().exactly() //
				.annotation(type(TestAnnotation.class) //
						.param("noSuchMethod", "default")) //
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
	public void testValidateAnnotatedMethod_NotAllAnnotationMethodsDefinedInAnnotationDefinition() throws NoSuchMethodException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Unexpected value for Method 'testparameter' found");

		validate() //
				.annotation(type(TestAnnotation.class) //
						.param("anotherTestParameter", "anotherTestValue")) //
				.forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
	}

	@Test
	public void testValidateAnnotatedMethod_NotAllAnnotationMethodsDefinedInAnnotationDefinition_NonStringValues() throws NoSuchMethodException {
		validate() //
				.annotation(type(AnotherTestAnnotation.class)) //
				.forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));
	}

	@Test
	public void testValidateAnnotatedMethod_OverloadedMethod() throws NoSuchMethodException {
		validate().exactly() //
				.annotation(type(AnotherTestAnnotation.class) //
						.param("testEnum", TEST) //
						.param("value", TEST)) //
				.forMethod(AnnotatedTestClass.class.getMethod("overloadedMethod", String.class, String.class));
	}

	@Test
	public void testValidateAnnotatedMethod_UseAlias() throws NoSuchMethodException {
		validate().exactly() //
				.annotation(type(AliasTestAnnotation.class) //
						.param("referencedTestEnum", TEST)) //
				.forMethod(AnnotatedTestClass.class.getMethod("methodWithAliasAnnotations"));
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
						.param("testparameter", "testvalue")) //
				.forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));
	}

	@Test
	public void testValidateMethod() throws NoSuchMethodException {
		validate().exactly()//
				.forMethod(AnnotatedTestClass.class.getMethod("methodWithoutAnnotations"));
	}

	@Test
	public void testValidateMethod_AnnotationNotPresent() throws NoSuchMethodException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Expected Annotation de.tolina.common.validation.TestAnnotation not found");
		validate().exactly()//
				.annotation(type(TestAnnotation.class)) //
				.forMethod(AnnotatedTestClass.class.getMethod("methodWithoutAnnotations"));
	}

	@Test
	public void testValidateMethod_NoSuchMethod() throws NoSuchMethodException {
		thrown.expect(NoSuchMethodException.class);
		validate().exactly()//
				.forMethod(AnnotatedTestClass.class.getMethod("noSuchMethod"));
	}

	@Test
	public void testValidateField() throws NoSuchFieldException {
		validate().exactly().forField(AnnotatedTestClass.class.getDeclaredField("fieldWithoutAnnotations"));
	}
	
	
	@Test
	public void testValidateLambdas() throws Exception {
//		TestInterface test1 = TestInterface::staticMethod;
//		Method annotatedMethod1= test1.getClass().getMethod("method");
//		validate().exactly().annotation(type(Deprecated.class)).forMethod(annotatedMethod1);
//
//		TestInterface test2 = test1::defaultMethod;
//		Method annotatedMethod2= test2.getClass().getMethod("defaultMethod");
//		validate().exactly().annotation(type(Deprecated.class)).forMethod(annotatedMethod2);
	}
	

	static interface TestInterface {
		
		@Deprecated
		void method();
		
		@Deprecated
		static void staticMethod(){
			System.out.println("staticMethod");
		}
		
		@Deprecated
		default void defaultMethod(){
			System.out.println("defaultMethod");
			
		}
	}
}
