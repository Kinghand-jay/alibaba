package com.jay.alibaba.nacosorder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
public class OrderController {

    @Value("${service.url}")
    private String url ;

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/consumer/{id}")
    public String testOrder(@PathVariable("id")Integer id){
        return restTemplate.getForObject(url+"/payment/nacos/"+id,String.class);
    }
}
