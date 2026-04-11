package org.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PrototypeController {
    @PostMapping("/echo")
    public EchoResponse echo(@RequestBody EchoRequest request) {
        return new EchoResponse(request.text());
    }

    record EchoRequest(String text){}
    record EchoResponse(String result) {}
}