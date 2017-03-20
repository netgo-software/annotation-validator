[![build status](https://gitlab.arxes-tolina.de/ron.peters/annotation-validator/badges/master/build.svg)](https://gitlab.arxes-tolina.de/ron.peters/annotation-validator/commits/master)

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
		.param("testEnum", TEST)) //
	.forMethod(AnnotatedTestClass.class.getMethod("methodWithAnnotations"));

```