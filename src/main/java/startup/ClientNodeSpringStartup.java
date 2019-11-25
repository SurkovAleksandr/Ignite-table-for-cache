package startup;

import data.A;
import java.util.List;
import javax.cache.Cache;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.junit.Test;

/**
 * Класс содержит тестовые методы, которые запускают клиентский узел.
 **/
public class ClientNodeSpringStartup {
    public static final String CACHE_NAME = "A_cache";
    public static final String TABLE_NAME = "A_TABLE";
    public static final String PUBLIC_SCHEMA = "PUBLIC";

    /**
     * Исследование особенностей работы свойства {@link IgniteConfiguration#setPeerClassLoadingEnabled(boolean)}
     **/
    @Test
    public void peerClassLoadingTest() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start("Cluster-client.xml")) {

            IgniteCluster cluster = ignite.cluster();
            IgniteCompute compute = ignite.compute();
            /**
             * todo изменение свойсвта {@link IgniteConfiguration#setPeerClassLoadingEnabled(boolean)} не вызывает обновления
             *  задачи на сервере
             * */
            compute.broadcast(() -> System.out.println("Hello node9: "));
        }
    }

    /**
     * Создание кэша с именем {@link #CACHE_NAME} и наполнение его данными.
     * Если раскомментировать часть кода, то кэш будет создан с таблицей.
     * */
    @Test
    public void createCache() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start("Cluster-client.xml")) {
            CacheConfiguration<Long, A> cacheCfg = new CacheConfiguration<>();
            cacheCfg
                .setName(CACHE_NAME)
                /*.setQueryEntities(Collections.singleton(new QueryEntity(long.class.getName(), A.class.getName())
                    .setTableName(TABLE_NAME)
                    .setFields(Stream.of(
                            new AbstractMap.SimpleEntry<>("id", long.class.getName()),
                            new AbstractMap.SimpleEntry<>("name", String.class.getName())
                        ).collect(Collectors.toMap(
                            AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> a, LinkedHashMap::new
                        ))
                    )
                ))*/;

            IgniteCache<Long, A> cache = ignite.getOrCreateCache(cacheCfg);
            //Наполнение кэша данными
            for (long i = 1; i < 10; i++) {
                cache.put(i, new A(i, "Vasja" + i));
            }
        }
    }

    /**
     * Получение данных их кэша различными способами:
     *  - через итератор
     *  - через SQL запрос. Работает, только если создана таблица для кэша
     * */
    @Test
    public void gettingValuesFromCache() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start("Cluster-client.xml")) {
            IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);

            System.out.println("Data through iterator");
            for (Cache.Entry<Object, Object> entry : cache) {
                System.out.println("Value from cache: " + entry);
            }

            /**
             * Если таблицы нет, то будет ошибка: org.h2.jdbc.JdbcSQLException: Таблица "A_TABLE" не найдена
             * Для создания таблицы надо вызвать {@link #createTableForCache()}
             * */
            System.out.println("Value through SQL");
            List<List<?>> all = cache.query(new SqlFieldsQuery("select * from " + TABLE_NAME)).getAll();
            for (List<?> object : all) {
                System.out.println("Value by SQL: " + object);
            }
        }
    }

    /**
     * todo Попытка создать таблицу для уже существующего кэша. Не получается.
     *
     * https://issues.apache.org/jira/browse/IGNITE-7113
     */
    @Test
    public void createTableForCache() {
        try (IgniteEx ignite = (IgniteEx)Ignition.start("Cluster-client.xml")) {
            String createSql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(id long PRIMARY KEY, " +
                "name varchar) " +
                "WITH \"CACHE_NAME=" + CACHE_NAME + ",template=partitioned,backups=1,affinity_key=id\";";

            List<List<?>> all = ignite.context().query().querySqlFields(new SqlFieldsQuery(createSql).setSchema(PUBLIC_SCHEMA), true).getAll();

            System.out.println(all);

            String insertSql = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?)";

            List<List<?>> insert1 = ignite.context().query().querySqlFields(new SqlFieldsQuery(insertSql)
                .setArgs(3, "name_3")
                .setSchema(PUBLIC_SCHEMA), true)
                .getAll();
            List<List<?>> insert2 = ignite.context().query().querySqlFields(new SqlFieldsQuery(insertSql)
                .setArgs(4, "name_4")
                .setSchema(PUBLIC_SCHEMA), true)
                .getAll();
        }
    }
}