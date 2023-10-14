package com.bullseyeaem.core.cfrecs.annotations;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use this annotation to associate a Sling Model to a content fragment model.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface ContentFragmentSlingModel {

    /**
     * The content fragment model path that this Sling Model matches up with.
     * @return a content fragment model path
     */
    String value() default StringUtils.EMPTY;
}
