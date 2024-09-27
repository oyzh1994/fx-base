package cn.oyzh.fx.common.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryKeyColumn {

    private String columnName;

    private Object columnData;

}
