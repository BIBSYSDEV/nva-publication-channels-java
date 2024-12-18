# nva-publication-channels-java

A client to wrap HKDir DBH's publication channel databases.

[SWAGGER UI](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/BIBSYSDEV/nva-publication-channels-java/refs/heads/main/docs/openapi.yaml)

![Alt text](resources/publication-channels-v2.png)

## Cached data

Sometimes, you may wish to resolve many resources (semi-) simultaneously, which may degrade upstream
response times.
In these cases, follow these steps:

1. Upload the data set (csv format, see model
   `.../channelregistrycache/db/model/ChannelRegistryCacheDao.java`) to the s3
   bucket _channel-register-cache-{accountId}_
2. If channel registry data already exists in dynamoDB, empty the table
   _channel-register-cache-{stackName}_
3. Trigger `LoadCacheButtonHandler` lambda function
4. Configure the `ApplicationConfigurationProfile`:
    - Navigate to _AWS Systems Manager_ → _AppConfig_.
    - Choose _ApplicationConfig_ → _ApplicationConfigurationProfile_.
    - If there is no existing version with the value you want to deploy → navigate to Systems
      Manager, Parameter Store and update the SSM parameter named `ApplicationConfigurationParameter`.
      Update `publicationChannelCacheEnabled` value to `true` or `false`.
    - Return to _ApplicationConfig_ → _ApplicationConfigurationProfile_ and choose version of Config you
      want to deploy.
    - During deployment, select the environment you want to use. Currently, there is a single
      environment named _Live_. Choose the deployment strategy _AllAtOnce_.
    - Press _Start deployment_.
    - Please note that deployment may take some time.

```json
{
  "publicationChannelCacheEnabled": true
}
```

_!Important_: Caching is only supported for fetching publication channels by `id` and `year`, not
search. 