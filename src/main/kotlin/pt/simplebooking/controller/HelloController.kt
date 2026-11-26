package pt.simplebooking.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import pt.simplebooking.config.AppLogger

@RestController
class HelloController {

    private val logger = AppLogger.getLogger(javaClass)

    @GetMapping("/hello")
    fun hello(): String {
        logger.info("Hello endpoint called")
        return "Hello from SimpleBooking!"
    }
}