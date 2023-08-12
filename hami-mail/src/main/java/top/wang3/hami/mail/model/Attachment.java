package top.wang3.hami.mail.model;

import jakarta.activation.DataSource;

/**
 * 附件包装类
 */
public class Attachment {

    private String name;
    private DataSource dataSource;

    public Attachment(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    public String getName() {
        return name;
    }

    public Attachment setName(String name) {
        this.name = name;
        return this;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Attachment setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }
}
