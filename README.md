This project explores the following issues:
1) Enabling PeerClassLoading.
2) Cache can be created without table and SQL queries will not be run. Adding table for exist cache.

For second case the test consists of the following steps:
1. start server: ServerNodeSpringStartup#main
2. create cache without table: ClientNodeSpringStartup#createCacheWithoutTable
3. get data through iterator and SQL: ClientNodeSpringStartup#gettingValuesFromCache
    Through SQL, we get the expected exception.
4. create a table for the cache: ClientNodeSpringStartup#createTableForCache
5. re-call data retrieval: ClientNodeSpringStartup#gettingValuesFromCache
    Now we get them without exception.
