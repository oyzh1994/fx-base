package cn.oyzh.fx.common.jdbc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-26
 */
@Data
public class SelectListParam {

    public static SelectListParam EMPTY = new SelectListParam();

    private List<QueryParam> queryParams;

    private List<String> queryColumns;

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


    public SelectListParam() {

    }

    public SelectListParam(List<QueryParam> queryParams) {
        this.queryParams = queryParams;
    }

    public SelectListParam(List<QueryParam> queryParams, List<String> queryColumns) {
        this.queryParams = queryParams;
        this.queryColumns = queryColumns;
    }
}
