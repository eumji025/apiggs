package com.github.apiggs.example.advanced;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * will be ignore
 * @ignore
 */
@RestController
@RequestMapping("/ignore")
public class IgnoreController {


    @RequestMapping
    public void ignoreThis(){
    }


}
