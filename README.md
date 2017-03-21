[![build status](https://travis-ci.org/arxes-tolina/annotation-validator.svg?branch=master)](https://github.com/arxes-tolina/annotation-validator/commits/master)
[![quality status](https://sonarqube.com/api/badges/gate?key=de.tolina.common.validation%3Aannotation-validator)](https://sonarqube.com/dashboard?id=de.tolina.common.validation%3Aannotation-validator)

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

If you would like to check the class to be annotated only with `@MyAnnotation` use the following

```
validate().exactly() //
	.annotation(type(MyAnnotation.class) //
		.param("foo", "default") //
	.forClass(AnnotatedTestClass.class);
```

If you would like to check the field to be annotated with `@MyAnnotation`, whereas `foo` has value `bar`, use the following

```
validate() //
	.annotation(type(MyAnnotation.class) //
		.param("foo", "bar") //
	.forField(AnnotatedTestClass.class.getDeclaredField("fieldWithAnnotations"));

```

If you would like to check the method to be annotated with `@AnotherAnnotation`, whereas `value` has value `TEST`, use the following

```
validate() //
	.annotation(type(AnotherTestAnnotation.class) //
		.param("value", TEST)) //
	.forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));

```