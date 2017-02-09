/*
 * (c) tolina GmbH, 2017
 */

package de.tolina.common;

import static de.tolina.common.TestEnum.TEST;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.springframework.core.annotation.AliasFor;

@Retention(RUNTIME)
@SuppressWarnings("javadoc")
public @interface AnotherTestAnnotation {

    @AliasFor("testEnum")
    TestEnum value() default TEST;

    @AliasFor("value")
    TestEnum testEnum() default TEST;
}
