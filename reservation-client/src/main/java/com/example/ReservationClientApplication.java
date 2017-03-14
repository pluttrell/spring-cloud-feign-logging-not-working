package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableCircuitBreaker
@EnableFeignClients
public class ReservationClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReservationClientApplication.class, args);
  }

  @Bean
  @LoadBalanced
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

}

@FeignClient("reservation-service")
interface ReservationReader {

  @RequestMapping(method = RequestMethod.GET, value = "/reservations")
  Resources<Reservation> read();

}


@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

  private final RestTemplate restTemplate;
  private final ReservationReader reservationReader;

  public ReservationApiGatewayRestController(RestTemplate restTemplate, ReservationReader reservationReader) {
    this.restTemplate = restTemplate;
    this.reservationReader = reservationReader;
  }


  public Collection<String> fallback() {
    return new ArrayList<>();
  }

  @GetMapping("/names")
  @HystrixCommand(fallbackMethod = "fallback")
  public Collection<String> names() {

    return reservationReader.read()
        .getContent()
        .stream()
        .map(Reservation::getReservationName)
        .collect(Collectors.toList());

  }
}

class Reservation {

  private String reservationName;

  public String getReservationName() {
    return reservationName;
  }

  public void setReservationName(String reservationName) {
    this.reservationName = reservationName;
  }
}