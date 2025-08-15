package com.okemwag.subscribe.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark methods that require business scope authorization */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessScopeCheck {

  /** Name of the parameter that contains the business ID to check */
  String parameterName() default "businessId";

  /**
   * Whether to allow access if no business ID is found in parameters (useful for methods that
   * operate on current business context only)
   */
  boolean allowCurrentBusinessOnly() default true;
}
