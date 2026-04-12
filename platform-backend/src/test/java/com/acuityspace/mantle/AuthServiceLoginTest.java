package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.exception.InvalidCredentialsException;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.LoginRequest;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthServiceLoginTest extends TestContainersBase {

    @Autowired
    private AuthService authService;

    private static final String TEST_EMAIL = "loginuser@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        authService.register(new RegisterRequest(
                TEST_EMAIL, TEST_PASSWORD, "Login User", "LoginOrg", OrgType.INDIVIDUAL));
    }

    @Test
    void login_validCredentials_returnsAuthResponseAndSetsCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthResponse auth = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), response);

        assertThat(auth).isNotNull();
        assertThat(auth.email()).isEqualTo(TEST_EMAIL);
        assertThat(auth.name()).isEqualTo("Login User");
        assertThat(auth.orgName()).isEqualTo("LoginOrg");
        assertThat(auth.orgType()).isEqualTo(OrgType.INDIVIDUAL);
        assertThat(auth.subOrgId()).isNotNull();
        assertThat(auth.role()).isEqualTo(UserRole.SUPER_ADMIN);

        String setCookieHeader = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("mantle-token=");
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("Path=/");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.login(new LoginRequest(TEST_EMAIL, "wrongpassword"), response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentialsException() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", TEST_PASSWORD), response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_errorMessage_doesNotLeakWhichFieldIsWrong() {
        MockHttpServletResponse r1 = new MockHttpServletResponse();
        MockHttpServletResponse r2 = new MockHttpServletResponse();

        InvalidCredentialsException wrongPasswordEx = null;
        InvalidCredentialsException unknownEmailEx = null;

        try {
            authService.login(new LoginRequest(TEST_EMAIL, "wrongpassword"), r1);
        } catch (InvalidCredentialsException e) {
            wrongPasswordEx = e;
        }

        try {
            authService.login(new LoginRequest("nobody@example.com", TEST_PASSWORD), r2);
        } catch (InvalidCredentialsException e) {
            unknownEmailEx = e;
        }

        assertThat(wrongPasswordEx).isNotNull();
        assertThat(unknownEmailEx).isNotNull();
        assertThat(wrongPasswordEx.getClass()).isEqualTo(unknownEmailEx.getClass());
        assertThat(wrongPasswordEx.getMessage()).isEqualTo(unknownEmailEx.getMessage());
    }
}
