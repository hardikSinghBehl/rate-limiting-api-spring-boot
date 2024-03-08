package com.behl.overseer.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare API endpoints as public i.e non-secured, allowing then
 * to be accessed without a valid Authorization header in HTTP request.
 *  
 * When applied to a controller method, requests to the method will be exempted
 * from authentication checks by the {@link com.behl.overseer.filter.JwtAuthenticationFilter}
 * 
 * @see com.behl.overseer.configuration.SecurityConfiguration
 * @see com.behl.overseer.filter.JwtAuthenticationFilter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {

}