package pt.simplebooking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleBookingApplication

fun main(args: Array<String>) {
	runApplication<SimpleBookingApplication>(*args)
}
