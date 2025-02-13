package moadong.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import moadong.global.payload.Response;
import moadong.user.payload.UserRegisterRequest;
import moadong.user.service.UserCommandService;
import moadong.user.view.UserSwaggerView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/user")
@AllArgsConstructor
@Tag(name = "User", description = "동아리 담당자 계정 API")
public class UserController {
    private final UserCommandService userCommandService;

    @PostMapping("/register")
    @Operation(
            summary = UserSwaggerView.ADMIN_REGISTER_SUMMARY,
            description = UserSwaggerView.ADMIN_PWD_ROLE_DESCRIPTION
    )
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest request) {
        userCommandService.registerUser(request);
        return Response.ok("success register");
    }
}
