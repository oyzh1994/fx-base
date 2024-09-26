package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.util.ReflectUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public abstract class SqliteStore<M extends Serializable> {

    private final SqliteOperator operator;

    public SqliteStore() {
        try {
            TableDefinition tableDefinition = this.tableDefinition();
            this.operator = new SqliteOperator(tableDefinition);
            this.operator.initTable();
            this.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void init() {

    }

    protected abstract M newModel();

    protected abstract Class<M> modelClass();

    protected TableDefinition tableDefinition() {
        if (this.operator != null) {
            return this.operator.getTableDefinition();
        }
        return TableDefinition.ofClass(this.modelClass());
    }

    protected M toModel(Map<String, Object> record) throws Exception {
        if (CollUtil.isEmpty(record)) {
            return null;
        }
        TableDefinition definition = this.tableDefinition();
        if (definition == null) {
            return null;
        }
        M model = this.newModel();
        for (ColumnDefinition column : definition.getColumns()) {
            Field field = ReflectUtil.getField(model.getClass(), column.getFieldName(), true);
            field.setAccessible(true);
            Object sqlData = record.get(column.getColumnName());
            Object javaValue = SqlLiteUtil.toJavaValue(field.getType(), sqlData);
            if (javaValue != null) {
                field.set(model, javaValue);
            }
        }
        return model;
    }

    protected Map<String, Object> toRecord(M model) throws Exception {
        TableDefinition definition = this.tableDefinition();
        if (definition == null) {
            return null;
        }
        Map<String, Object> record = new HashMap<>();
        for (ColumnDefinition column : definition.getColumns()) {
            Field field = ReflectUtil.getField(model.getClass(), column.getFieldName(), true);
            field.setAccessible(true);
            record.put(column.getColumnName(), field.get(model));
        }
        return record;
    }

    public boolean insert(M model) {
        if (model != null) {
            try {
                // 处理主键值
                this.tableDefinition().handlePrimaryKeyValue(model);
                return this.operator.insert(this.toRecord(model)) > 0;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean update(M model) {
        if (model != null) {
            try {
                TableDefinition tableDefinition = this.tableDefinition();
                PrimaryKeyColumn primaryKey = tableDefinition.primaryKeyColumn(model);
                if (primaryKey == null) {
                    return false;
                }
                Map<String, Object> record = this.toRecord(model);
                return this.operator.update(record, primaryKey) > 0;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean exist(Object primaryKey) {
        if (primaryKey != null) {
            try {
                return this.operator.exist(primaryKey);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean exist(Map<String, Object> params) {
        try {
            return this.operator.exist(params);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public M selectOne(Object primaryKey) {
        try {
            return this.toModel(this.operator.selectOne(primaryKey));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public M selectOne(QueryParam param) {
        try {
            return this.toModel(this.operator.selectOne(param));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<M> selectList() {
        return this.selectList(Collections.emptyList());
    }

    public List<M> selectList(QueryParam param) {
        return this.selectList(List.of(param));
    }

    public List<M> selectList(List<QueryParam> params) {
        try {
            List<Map<String, Object>> list = this.operator.selectList(params);
            if (CollUtil.isNotEmpty(list)) {
                List<M> models = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    models.add(this.toModel(map));
                }
                return models;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public long selectCount(List<QueryParam> params) {
        try {
            return this.operator.selectCount(params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0L;
    }

    public long selectCount(String kw, List<String> columns) {
        try {
            return this.operator.selectCount(kw, columns);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0L;
    }

    public List<M> selectPage(String kw, List<String> columns, PageParam pageParam) {
        try {
            List<Map<String, Object>> list = this.operator.selectPage(kw, columns, pageParam);
            if (CollUtil.isNotEmpty(list)) {
                List<M> models = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    models.add(this.toModel(map));
                }
                return models;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public boolean delete(Object primaryKey) {
        if (primaryKey != null) {
            try {
                return this.operator.delete(primaryKey) > 0;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean delete(Map<String, Object> params) {
        return this.delete(params, 1L);
    }

    public boolean delete(Map<String, Object> params, long limit) {
        try {
            return this.operator.delete(params, limit) > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
