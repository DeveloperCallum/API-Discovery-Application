package com.beamedcallum.discovery.application.controller;

import common.discovery.messages.RegisterRequest;
import common.discovery.messages.ServiceFoundResponse;
import common.discovery.messages.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RegistrationController {
    private Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final Map<String, List<RegisterRequest>> registeredData = new HashMap<>();

    @PostMapping("services/register")
    public ResponseEntity<?> registerService(@RequestBody RegisterRequest registerRequest) {
        logger.info("service registered - Host: " + registerRequest.getHostname() + ", Url: " + registerRequest.getService() + ", Default Role: " + registerRequest.getDefaultRole());
        
        List<RegisterRequest> requests = registeredData.getOrDefault(registerRequest.getService(), new ArrayList<>());

        //Prevent services that get disconnected from adding to the list.
        if (requests.contains(registerRequest)){
            return new ResponseEntity<>(new SystemMessage("Service was already registered"), HttpStatus.OK);
        }

        logger.info("Service registered.");
        requests.add(registerRequest);

        //If the current recorded data does not contain request, then we add it.
        if (!registeredData.containsKey(registerRequest.getService())) {
            registeredData.put(registerRequest.getService(), requests);
        }

        System.out.println(registeredData);

        //TODO: Make sure request is from a whitelist of IPs.
        return new ResponseEntity<>(new SystemMessage("Registered Service"), HttpStatus.OK);
    }

    @GetMapping("services/{service}")
    public ResponseEntity<?> getServiceUrl(@PathVariable String service) {
        if (!registeredData.containsKey(service)) {
            return new ResponseEntity<>("Service is not registered", HttpStatus.NOT_FOUND);
        }

        List<RegisterRequest> hostnames = registeredData.get(service);

        if (hostnames.size() == 1){
            RegisterRequest request = hostnames.get(0);
            return new ResponseEntity<>(new ServiceFoundResponse(request.getHostname(), request.getService(), request.getDefaultRole()), HttpStatus.OK);
        }

        SecureRandom random = new SecureRandom();
        RegisterRequest request = hostnames.get(random.nextInt(hostnames.size()));
        return new ResponseEntity<>(new ServiceFoundResponse(request.getHostname(), request.getService(), request.getDefaultRole()), HttpStatus.OK);
    }
}
