package ru.vasili4.reactive_video.web.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;
import ru.vasili4.reactive_video.web.dto.response.FileResponseEntity;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-user")
public class UserReactiveController {

    private final UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@RequestBody UserRequestEntity dto) {
        return userService.register(dto).map(id -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

}
