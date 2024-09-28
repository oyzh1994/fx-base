package cn.oyzh.fx.common.jdbc;

import lombok.Data;

@Data
public class OrderByParam {

    private String name;

    private String type = "asc";

    public OrderByParam(String name) {
        this.name = name;
    }

    public OrderByParam(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
