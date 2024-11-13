// package cn.oyzh.fx.plus.store;
//
// import cn.oyzh.common.jdbc.ColumnDefinition;
// import cn.oyzh.common.sqlite.SqlLiteUtil;
// import cn.oyzh.common.sqlite.SqliteStore;
// import cn.oyzh.common.jdbc.TableDefinition;
// import cn.oyzh.fx.plus.domain.TreeGroup;
//
// import java.util.HashMap;
// import java.util.Map;
//
//
// /**
//  * @author oyzh
//  * @since 2024/09/23
//  */
// public abstract class GroupStore<E extends TreeGroup> extends SqliteStore<E> {
//
//     // @Override
//     // protected TableDefinition tableDefinition() {
//     //     TableDefinition definition = new TableDefinition();
//     //     definition.setTableName("t_group");
//     //     ColumnDefinition gid = new ColumnDefinition();
//     //     gid.setColumnName("gid");
//     //     gid.setColumnType("text");
//     //     gid.setPrimaryKey(true);
//     //     ColumnDefinition name = new ColumnDefinition();
//     //     name.setColumnName("name");
//     //     name.setColumnType("text");
//     //     ColumnDefinition extend = new ColumnDefinition();
//     //     extend.setColumnName("extend");
//     //     extend.setColumnType("integer");
//     //     definition.addColumn(gid);
//     //     definition.addColumn(name);
//     //     definition.addColumn(extend);
//     //     return definition;
//     // }
//
//     // @Override
//     // protected Map<String, Object> toRecord(E model) {
//     //     Map<String, Object> record = new HashMap<>();
//     //     record.put("gid", model.getGid());
//     //     record.put("name", model.getName());
//     //     record.put("extend", model.getExpand());
//     //     return record;
//     // }
//     //
//     // @Override
//     // protected E toModel(Map<String, Object> record) {
//     //     E model = this.newModel();
//     //     model.setGid((String) record.get("gid"));
//     //     model.setName((String) record.get("name"));
//     //     model.setExpand(SqlLiteUtil.toBool(record.get("extend")));
//     //     return model;
//     // }
// }
