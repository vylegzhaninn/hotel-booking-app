package io.github.vylegzhaninn.gateway.client;

import io.github.vylegzhaninn.gateway.model.AuthenticationRequest;
import io.github.vylegzhaninn.gateway.model.AuthenticationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service")
public interface AuthenticationClient {

  @GetMapping("/login")
  AuthenticationResponse login(@RequestBody AuthenticationRequest request);

  @PostMapping("/registration")
  AuthenticationResponse registration(@RequestBody AuthenticationRequest request);

  @PostMapping("/logout")
  void logout(@RequestBody AuthenticationRequest request);
}
