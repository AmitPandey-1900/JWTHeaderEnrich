@startuml

package com.lseg {
    package config {
        class RedisConfig {
            +redisConnectionFactory() : RedisConnectionFactory
            +redisTemplate(RedisConnectionFactory) : RedisTemplate<String, Object>
        }
        note right of RedisConfig
            Configures Redis connection and Redis template for caching
        end note
    }

    package service {
        class JwtService {
            -redisTemplate : RedisTemplate<String, Object>
            +getJwtToken() : String
            +refreshJwtToken() : String
            -restTemplate : RestTemplate
        }
        note right of JwtService
            Handles JWT token retrieval from external service,
            caching it in Redis, and refreshing it periodically.
        end note
    }

    package controller {
        class ExternalServiceController {
            -jwtService : JwtService
            -env : Environment
            +callExternalService(userId: String) : String
            -restTemplate : RestTemplate
        }
        note right of ExternalServiceController
            Receives user requests, retrieves JWT token from JwtService,
            and forwards the request to the external service with the
            necessary headers.
        end note
    }

    package main {
        class Application {
            +main(String[] args) : void
        }
        note right of Application
            Main entry point of the Spring Boot application.
            Enables scheduling for periodic tasks.
        end note
    }
}

config.RedisConfig --> service.JwtService : provides
service.JwtService --> controller.ExternalServiceController : uses
controller.ExternalServiceController --> service.JwtService : uses
main.Application --> config.RedisConfig : configures
main.Application --> service.JwtService : initializes
main.Application --> controller.ExternalServiceController : initializes

@enduml
