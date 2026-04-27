package br.lab.testesubmissao.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserControllerSecurityAnnotationsTest {

    @Test
    void adminEndpointsUseExpectedRoleExpression() throws NoSuchMethodException {
        assertPreAuthorizeValue("listAll", "hasRole('ADMIN')");
        assertPreAuthorizeValue("findById", "hasRole('ADMIN')", java.util.UUID.class);
        assertPreAuthorizeValue("adminUpdate", "hasRole('ADMIN')",
                java.util.UUID.class,
                br.lab.testesubmissao.Dto.user.AdminUpdateUserRequest.class,
                org.springframework.security.core.Authentication.class);
    }

    private void assertPreAuthorizeValue(String methodName, String expectedValue, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = UserController.class.getMethod(methodName, parameterTypes);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, () -> "Esperava @PreAuthorize em " + methodName);
        assertEquals(expectedValue, annotation.value());
    }
}
