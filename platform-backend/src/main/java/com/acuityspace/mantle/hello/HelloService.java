package com.acuityspace.mantle.hello;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String getWelcomeMessage() {
        return "Welcome to Mantle.";
    }
}