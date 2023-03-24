# Azure Spring Apps File Processing Sample

## Scenario

1. The app reads TXT files from [Azure Storage File Share](https://learn.microsoft.com/en-us/azure/storage/files/storage-files-introduction) and converts them to [Avro](https://avro.apache.org/docs/1.11.1/) format and then pushes it to [Azure Event Hubs](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-about).
2. The app runs at daily intervals, not a long-running process.
3. Unknown text files count, so the app or running environment must take care of the scaling
4. Operation friendly:
    - Auto recovery from error, for example:
      - An invalid text format.
      - Event Hub is unreachable.
      - Too many data to process in one interval.
      - Any other common errors.
    - Easy troubleshooting.
5. This is a classic [Enterprise Integration Pattern](http://www.eaipatterns.com/), so we use the [Spring Boot](https://spring.io/projects/spring-boot) + [Spring Integration](https://spring.io/projects/spring-integration) in this application.
