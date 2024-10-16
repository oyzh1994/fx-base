package cn.oyzh.fx.common.jdbc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-26
 */
@Data
public class DeleteParam {

    private Long limit;

    private List<QueryParam> queryParams;

    private List<OrderByParam> orderByParams;

    public void addQueryParam(QueryParam queryParam) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        this.queryParams.add(queryParam);
    }

    public void addQueryParams(List<QueryParam> queryParams) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        this.queryParams.addAll(queryParams);
    }

    public void addOrderByParam(OrderByParam orderByParam) {
        if (this.orderByParams == null) {
            this.orderByParams = new ArrayList<>();
        }
        this.orderByParams.add(orderByParam);
    }

}
