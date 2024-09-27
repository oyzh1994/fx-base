package cn.oyzh.fx.common.jdbc;

import lombok.Data;

@Data
public class QueryParam {

    private String name;

    private Object data;

    private String operator = "=";

    public QueryParam() {

    }

    public QueryParam(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public QueryParam(String name, Object data, String operator) {
        this.name = name;
        this.data = data;
        this.operator = operator;
    }
}
