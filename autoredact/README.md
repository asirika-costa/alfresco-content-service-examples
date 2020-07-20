#### This article explains the steps required to deal with auto redacting content with Alfresco Enterprise viewer. 

### Use-Case / Requirement
Contents stored in ACS have to be auto redacted after uploading. Therefore use case have been achieved using a java based scheduled jobs.

### Pre- requisites
Alfresco Enterprice Viewer has to be installed and configured in your environment.

## Configuration Steps

### ACS : Custom Script Development
1. Navigate to autoredact > autoredact-platform in command line and execute  'mvn clean install'.
A Maven project for scheduler job is [available here](source-code/alfresco-scheduled-script-action).
2. view the `target` folder.
![jar-file-image](assets/target-jar.png)
