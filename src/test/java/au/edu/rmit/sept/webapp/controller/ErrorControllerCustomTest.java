package au.edu.rmit.sept.webapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorControllerCustomTest {

    private final ErrorControllerCustom errorController = new ErrorControllerCustom();

    @Test
    @DisplayName("forbidden() returns 403 error view")
    void forbiddenReturns403ErrorView() {
        String result = errorController.forbidden();
        assertThat(result).isEqualTo("error/403");
    }
}
