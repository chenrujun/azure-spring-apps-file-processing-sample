# Azure Spring Apps File Processing Sample

## 1. Scenario

### 1.1. Log Files Explanation

1. A system generates log files in folders named by date: `/var/log/system-a/${yyyy-MM-dd}`. 
2. The Log files are txt files named by hour and minute: `${hh-mm}.txt`.
3. Each line of the log files will have format like this: `name,favorite_number,favorite_color`.

   Here is a picture about folder structure and log file:

   ![log-file-and-folder](./pictures/log-file-and-folder.png)

### 1.2. File Processing Requirements

#### 1.2.1. Functional Requirements
1. All log files should be processed.
2. Each line should be processed into an [avro](https://avro.apache.org/docs/1.11.1/) object. Here is the format of avro:
   ```json
   {
     "namespace": "com.azure.spring.example.file.processing.avro.generated",
     "type": "record",
     "name": "User",
     "fields": [
       {
         "name": "name",
         "type": "string"
       },
       {
         "name": "favorite_number",
         "type": [
           "int",
           "null"
         ]
       },
       {
         "name": "favorite_color",
         "type": [
           "string",
           "null"
         ]
       }
     ]
   }
   ```
3. Send the avro object to [Azure Event Hubs](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-about).
4. After file processed, move the file to another folder `/var/log/system-a-processed/${yyyy-MM-dd}`

   Here is a picture about moving log files after processed:

   ![move-file-after-processed](./pictures/move-file-after-processed.png)

#### 1.2.2. Functional Requirements
1. The system must be robust. 
   - 1.1. Handle invalid file. Current application only handle txt files. For other file types like csv, it will be filtered out.
     ![filter-by-file-type](./pictures/filter-by-file-type.png)
   - 1.2. Handle invalid line. When there is a invalid data line in a file, output a warning log then continue processing.
     ![invalid-lines](./pictures/invalid-lines.png)
2. Easy to track.
   - 2.1. When there is invalid line, the log should contain these information:
      - Which file?
      - Which line?
   - 2.2. Track each step of a specific file.
      - Does this file be added in to processing candidate?
      - This file is filtered out, why?
      - How many line does this line have?


### 1.3. System Diagram

   ![system-diagram](./pictures/system-diagram.png)

1. **Azure Spring Apps**: Current application will run on Azure Spring Apps.
2. **Azure Storage Files**: Log files stored in Azure Storage files.
3. **Azure Event Hubs**: In log files, each valid line will be converted into avro format then send to Azure Event Hubs. 
4. **Log Analytics**: When current application run in Azure Spring Apps, the logs can be viewed by Log Analytics.

## 2. Run Current Sample on Azure Spring Apps Consumption Plan

### 2.1. Provision Required Azure Resources

1. Provision an Azure Spring Apps Standard consumption plan. Refs: [Provision an Azure Spring Apps Standard consumption plan service instance](https://learn.microsoft.com/en-us/azure/spring-apps/quickstart-provision-standard-consumption-service-instance?tabs=Azure-portal).
2. Create An app in created Azure Spring Apps.
3. Create an Azure Event Hub. Refs: [Create an event hub using Azure portal](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-create).
4. Create Azure Storage Account. Refs: [Create a storage account](https://learn.microsoft.com/en-us/azure/storage/common/storage-account-create?tabs=azure-portal).
5. Create a File Share in created Storage account.
6. Mount Azure Storage into Azure Spring Apps to `/var/log/`. Refs: [How to enable your own persistent storage in Azure Spring Apps with the Standard consumption plan](https://learn.microsoft.com/en-us/azure/spring-apps/how-to-custom-persistent-storage-with-standard-consumption#add-storage-to-an-app).

### 2.2. Deploy Current Sample

1. Set these environment variables for the app.
   ```properties
   logs-directory=/var/log/system-a
   processed-logs-directory=/var/log/system-a-processed
   spring.cloud.azure.eventhubs.connection-string=
   spring.cloud.azure.eventhubs.event-hub-name=
   ```

2. Upload some sample log files into Azure Storage Files.

3. Build package.
   ```shell
   ./mvnw clean package
   ```

4. Set necessary environment variables according to the created resources.
   ```shell
   RESOURCE_GROUP=
   LOCATION=
   AZURE_SPRING_APPS_INSTANCE=
   AZURE_CONTAINER_APPS_ENVIRONMENT=
   APP_NAME=
   STORAGE_ACCOUNT_NAME=
   FILE_SHARE_NAME=
   STORAGE_MOUNT_NAME=
   ```

5. Deploy app
   ```shell
   az spring app deploy \
     --resource-group $RESOURCE_GROUP \
     --service $AZURE_SPRING_APPS_INSTANCE \
     --name $APP_NAME \
     --artifact-path target/azure-spring-apps-file-processing-sample-0.0.1-SNAPSHOT.jar
   ```

6. Check log by [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/)
   ```shell
   az spring app logs \
     --resource-group $RESOURCE_GROUP \
     --service $AZURE_SPRING_APPS_INSTANCE \
     --name $APP_NAME
   ```

   Screenshot:
   ![check-log-by-azure-cli](./pictures/check-log-by-azure-cli.png)

7. Check logs by [Azure Portal](https://ms.portal.azure.com/) -> Monitoring -> Logs
 
   Query:
   ```
   AppEnvSpringAppConsoleLogs_CL
   | where ContainerAppName_s == 'app-rujche-0406-1'
   | project time_s, Log_s
   | order by time_s asc
   | limit 200
   ```

   Screenshot:
   ![check-log-by-portal](./pictures/check-log-by-portal.png)

### 2.3. Check the details about file processing

1. Get log of specific error.

   Query:
   ```
   AppEnvSpringAppConsoleLogs_CL
   | where ContainerAppName_s == 'app-rujche-0406-1'
   | where Log_s has "Convert txt string to User failed"
   | project time_s, Log_s
   | order by time_s asc
   | limit 200
   ```

   Screenshot:
   ![wrong-text-format](./pictures/wrong-text-format.png)

2. Get logs about a specific file.

   Query:
   ```
   AppEnvSpringAppConsoleLogs_CL
   | where ContainerAppName_s == 'app-rujche-0406-1'
   | where Log_s has "/var/log/system-a/2023-04-06/00-00.txt"
   | project time_s, Log_s
   | order by time_s asc
   | limit 200
   ```

   Screenshot:
   ![get-all-logs-about-a-specific-file](./pictures/get-all-logs-about-a-specific-file.png)

3. Check events in Azure Event Hubs.
   
   Events in Azure Event Hubs can be viewed by [ServiceBusExplorer](https://github.com/paolosalvatori/ServiceBusExplorer). (I'm using version 5.0.4.)

   Screenshot:
   ![service-bus-explorer](./pictures/service-bus-explorer.png)

## 3. Next Steps

### 3.1. Store Secrets in Azure Key Vault Secrets

Secret can be stored in [Azure Key Vault secrets](https://learn.microsoft.com/en-us/azure/key-vault/secrets/about-secrets) and used in this application. [spring-cloud-azure-starter-keyvault](https://learn.microsoft.com/en-us/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-key-vault) is a useful tool to get secrets from Azure KeyVault in Spring Boot applications. And `spring-cloud-azure-starter-keyvault` supports refresh the secrets in a fixed interval.

The following values can be treated as secrets in current file-processing application:
1. Connection string to Azure Event Hubs.
2. Passwords of a specific file. All file-secret can be stored a key-value map. And the key-value map can be serialized and stored in Azure Key Vault Secrets. Here is example of such map:
   ```json
   [
     {"00-00.txt": "password-0"},
     {"00-01.txt": "password-1"},
     {"00-02.txt": "password-2"}
   ]
   ```

### 3.2. Auto Scaling

### 3.2.1. Scale 0 - 1

1. **Design**
   1. Scale to 0 instance when:
      - There is no file need to be handled for more than 1 hour. 
   2. Scale to 1 instance when one of these requirements satisfied:
      - File exists for more than 1 hour.
      - File count > 100.
      - File total size > 1 GB.
2. **Implement**: Use **Azure Blob Storage** instead of **Azure File Share**. So related [KEDA Scaler](https://keda.sh/docs/2.9/scalers/azure-storage-blob/) can be used.

### 3.2.2. Scale 1 - n

1. **Design**: Scale instance number according to file count and total file size.
2. **Implement**: To avoid competition between instances, use some proven technology like [Master/slave module](https://en.wikipedia.org/wiki/Master/slave_(technology)).

