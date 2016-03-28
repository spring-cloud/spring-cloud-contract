import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

String console = "CONSOLE"
String logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSSZ, Europe/Warsaw} | %-5level | %X{correlationId} | %thread | %logger{1} | %m%n"

appender(console, ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = logPattern
    }
}

root(INFO, [console])
logger("com.ofg", DEBUG)
