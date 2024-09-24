package cn.oyzh.fx.common.sqlite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
    @NoArgsConstructor
    @AllArgsConstructor
    public  class PrimaryKeyColumn {

        private String columnName;

        private Object columnData;

    }
