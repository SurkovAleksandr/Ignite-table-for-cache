package startup;

import data.A;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.cache.Cache;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.junit.Test;

/**
 * Class contains test methods for start up client nodes
 **/
public class ClientNodeSpringStartup {
    private static final String clientCfgFile = "Cluster-client.xml";
    private static final String CACHE_NAME = "A_cache";
    private static final String TABLE_NAME = "A_TABLE";
    private static final String PUBLIC_SCHEMA = "PUBLIC";

    /** Investigation of features of property work {@link IgniteConfiguration#setPeerClassLoadingEnabled(boolean)} **/
    @Test
    public void peerClassLoadingTest() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start(clientCfgFile)) {

            IgniteCompute compute = ignite.compute();
            /**
             * In order to be able to send the task to the server node you need to set {@link IgniteConfiguration#setPeerClassLoadingEnabled(boolean)}.
             *
             * If compute task will be available in classpath of server then {@link IgniteConfiguration#setPeerClassLoadingEnabled(boolean)} does not matter.
             * It means that server should be started at other JVM
             * */
            compute.broadcast(() -> System.out.println("Hello node: "));
        }
    }

    /** Creating cache {@link #CACHE_NAME} WITHOUT table and populate it with data. */
    @Test
    public void createCacheWithoutTable() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start(clientCfgFile)) {
            CacheConfiguration<Long, A> cacheCfg = new CacheConfiguration<>();
            cacheCfg
                .setName(CACHE_NAME);

            IgniteCache<Long, A> cache = ignite.getOrCreateCache(cacheCfg);
            //Populating the cache with data.
            for (long i = 1; i < 10; i++) {
                cache.put(i, new A(i, "Vasja" + i));
            }
        }
    }

    /** Creating cache {@link #CACHE_NAME} WITH table and populate it with data. * */
    @Test
    public void createCacheWithTable() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start(clientCfgFile)) {
            CacheConfiguration<Long, A> cacheCfg = new CacheConfiguration<>();
            cacheCfg
                .setName(CACHE_NAME)
                .setQueryEntities(Collections.singleton(new QueryEntity(long.class.getName(), A.class.getName())
                    .setTableName(TABLE_NAME)
                    .setFields(Stream.of(
                                new AbstractMap.SimpleEntry<>("id", long.class.getName()),
                                new AbstractMap.SimpleEntry<>("name", String.class.getName())
                            ).collect(Collectors.toMap(
                                AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> a, LinkedHashMap::new
                            ))
                    )
                ));

            IgniteCache<Long, A> cache = ignite.getOrCreateCache(cacheCfg);
            //Populating the cache with data.
            for (long i = 1; i < 10; i++) {
                cache.put(i, new A(i, "Vasja" + i));
            }
        }
    }

    /**
     * Retrieving data from the cache different ways:
     *  - through an iterator
     *  - through SQL query. Only works if a table has been created for the cache.
     * */
    @Test
    public void gettingValuesFromCache() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start(clientCfgFile)) {
            IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);

            //Retrieving data from the cache through an iterator
            System.out.println("Data through iterator");
            for (Cache.Entry<Object, Object> entry : cache) {
                System.out.println("Value from cache: " + entry);
            }

            /**
             * Retrieving data from cache via SQL query.
             * If there is no table, then there will be an exception: org.h2.jdbc.JdbcSQLException
             * */
            System.out.println("Value through SQL");
            List<List<?>> all = cache.query(new SqlFieldsQuery("select * from " + TABLE_NAME)).getAll();
            for (List<?> object : all) {
                System.out.println("Value by SQL: " + object);
            }
        }
    }

    /**
     * Create a table for existing cache.
     *
     * https://issues.apache.org/jira/browse/IGNITE-7113
     */
    @Test
    public void createTableForCache() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start(clientCfgFile)) {
            String createSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(id long PRIMARY KEY, " +
                "name varchar) " +
                "WITH \"CACHE_NAME=" + CACHE_NAME + ",template=partitioned,backups=1,affinity_key=id\";";

            IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);
            int count = cache.query(new SqlFieldsQuery(createSql).setSchema(PUBLIC_SCHEMA)).getColumnsCount();

            System.out.println("Result: " + count);
        }
    }
}