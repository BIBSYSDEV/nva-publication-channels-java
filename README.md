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
2. Trigger `LoadCacheHandler`
3. Configure the `ApplicationConfigurationProfile` with
   the following profile:

```json
{
  "publicationChannelCacheEnabled": true
}
```

_!Important_: Caching is only supported for fetching publication channels by `id` and `year`, not
search. 