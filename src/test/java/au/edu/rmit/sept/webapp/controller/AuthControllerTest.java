package au.edu.rmit.sept.webapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthController authController = new AuthController();

    @Test
    @DisplayName("login() with null auth returns login view")
    void loginWithNullAuthReturnsLoginView() {
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        
        String result = authController.login(null, redirectAttributes, model);
        
        assertThat(result).isEqualTo("login");
    }

    @Test
    @DisplayName("login() with authenticated user redirects to events")
    void loginWithAuthenticatedUserRedirects() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        
        String result = authController.login(auth, redirectAttributes, model);
        
        assertThat(result).isEqualTo("redirect:/events");
        verify(redirectAttributes).addFlashAttribute("info", "You are already logged in.");
    }
}
