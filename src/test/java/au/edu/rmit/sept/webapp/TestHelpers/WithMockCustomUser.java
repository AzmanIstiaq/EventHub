package au.edu.rmit.sept.webapp.TestHelpers;

import org.springframework.security.test.context.support.WithSecurityContext;

import au.edu.rmit.sept.webapp.model.UserType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
 String username() default "admin";
 UserType role() default UserType.ADMIN;
}
