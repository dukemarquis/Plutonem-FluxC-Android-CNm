package com.plutonem.android.fluxc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a valid REST endpoint.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Endpoint {
    String value();
}

