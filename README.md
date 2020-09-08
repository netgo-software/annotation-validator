[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fjakop_annotation-validator&metric=alert_status)](https://sonarcloud.io/dashboard?id=fjakop_annotation-validator)

# Usage with Maven

Add the following dependency to your pom.xml

```
<dependency>
	<groupId>de.jakop.validation</groupId>
	<artifactId>annotation-validator</artifactId>
	<version>1.0</version>
	<scope>test</scope>
</dependency>
```

# Examples

Consider the following class

```
@MyAnnotation
class AnnotatedTestClass {

	@MyAnnotation(foo = "bar")
	private String fieldWithAnnotations;

	@AnotherAnnotation(TEST)
	public void methodWithAnnotations() {
		// noop
	}
}
```

If you would like to check the class to be annotated only with `@MyAnnotation` and only have the configured params use the following

```
import static AnnotationDefinition.*;

...

validate().exactly() //
	.annotation(type(MyAnnotation.class) //
		.param("foo", "default")) //
	.forClass(AnnotatedTestClass.class);
```

If you would like to check the class to be annotated only with `@MyAnnotation` but also want to consider default values for not configured params use the following

```
import static AnnotationDefinition.*;

...

validate().only() //
	.annotation(type(MyAnnotation.class)) //
	.forClass(AnnotatedTestClass.class);
```

If you would like to check the field to be annotated with `@MyAnnotation`, whereas `foo` has value `bar`, use the following

```
validate() //
	.annotation(type(MyAnnotation.class) //
		.param("foo", "bar")) //
	.forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));

```

If you would like to check the method to be annotated with `@AnotherAnnotation`, whereas `value` has value `TEST`, use the following

```
validate() //
	.annotation(type(AnotherAnnotation.class) //
		.param("value", TEST)) //
	.forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));

```
