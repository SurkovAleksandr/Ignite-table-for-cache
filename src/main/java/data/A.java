package data;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class A {
    @QuerySqlField
    private long id;
    @QuerySqlField
    private String name;

    public A(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
