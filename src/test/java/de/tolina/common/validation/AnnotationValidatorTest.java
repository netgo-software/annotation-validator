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
package de.tolina.common.validation;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static de.tolina.common.validation.AnnotationValidator.validate;
import static org.junit.Assert.assertThat;

public class AnnotationValidatorTest {
    @Test
    public void testValidate_DefaultBlacklist() {
        final AnnotationValidation validate = validate();
        assertThat(validate.paramBlacklist, CoreMatchers.hasItems("equals", "toString", "hashCode", "annotationType"));
    }

}
