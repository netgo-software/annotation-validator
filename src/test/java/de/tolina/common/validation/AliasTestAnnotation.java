/**
 * Copyright © 2016 arxes-tolina GmbH (entwicklung@arxes-tolina.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tolina.common.validation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;

import static de.tolina.common.validation.TestEnum.TEST;
import static de.tolina.common.validation.TestEnum.TEST2;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@SuppressWarnings("javadoc")
@AnotherTestAnnotation
public @interface AliasTestAnnotation {
    @AliasFor(annotation = AnotherTestAnnotation.class, attribute = "testEnum")
    TestEnum referencedTestEnum() default TEST;

    @AliasFor(annotation = AnotherTestAnnotation.class)
    TestEnum anotherValue() default TEST2;
}
