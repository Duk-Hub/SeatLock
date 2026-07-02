package com.seatlock.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ExceptionTestController {

    @GetMapping("/test/business")
    void business() {
        throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    @PostMapping("/test/validation")
    void validation(@RequestBody @Valid TestRequest request) {
    }

    @GetMapping("/test/unexpected")
    void unexpected() {
        throw new IllegalStateException("내부 예외 메시지");
    }

    record TestRequest(@NotBlank String name) {
    }
}
