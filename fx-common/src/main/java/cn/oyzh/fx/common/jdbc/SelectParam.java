package cn.oyzh.fx.common.jdbc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-26
 */
@Data
public class SelectParam {

    public static SelectParam EMPTY = new SelectParam();

    private List<String> queryColumns;

    private List<QueryParam> queryParams;

    public void addQueryParam(QueryParam queryParam) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        this.queryParams.add(queryParam);
    }

    public void addQueryColumn(String column) {
        if (this.queryColumns == null) {
            this.queryColumns = new ArrayList<>();
        }
        this.queryColumns.add(column);
    }

    public SelectParam() {

    }

    public SelectParam(List<QueryParam> queryParams) {
        this.queryParams = queryParams;
    }

    public SelectParam(List<QueryParam> queryParams, List<String> queryColumns) {
        this.queryParams = queryParams;
        this.queryColumns = queryColumns;
    }
}
