package ru.vasili4.reactive_video.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reactive-video")
public class ReactiveVideoController {

    @GetMapping
    public String test() {
        return "test";
    }


}
