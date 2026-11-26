package pt.simplebooking.config

import org.slf4j.LoggerFactory

object AppLogger {
    fun getLogger(clazz: Class<*>) =
        LoggerFactory.getLogger(clazz)
}
