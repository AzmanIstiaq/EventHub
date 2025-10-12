package au.edu.rmit.sept.webapp.TestHelpers;

import org.springframework.security.test.context.support.WithSecurityContextFactory;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

public class WithMockCustomUserSecurityContextFactory
  implements WithSecurityContextFactory<WithMockCustomUser> {

 @Override
 public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
  SecurityContext context = new SecurityContextImpl();
  User user = new User();

  user.setUserId(100L); // match your test user
  user.setName(annotation.username());
  user.setRole(annotation.role());

  CustomUserDetails customUser = new CustomUserDetails(user);
  

  var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
    customUser, customUser.getPassword(), customUser.getAuthorities());

  context.setAuthentication(auth);
  return context;
 }
}
