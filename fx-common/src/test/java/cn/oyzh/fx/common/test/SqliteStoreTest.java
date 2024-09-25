package cn.oyzh.fx.common.test;

import cn.oyzh.fx.common.sqlite.ColumnDefinition;
import cn.oyzh.fx.common.sqlite.PrimaryKeyColumn;
import cn.oyzh.fx.common.sqlite.QueryParam;
import cn.oyzh.fx.common.sqlite.SqliteStore;
import cn.oyzh.fx.common.sqlite.TableDefinition;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public class SqliteStoreTest {

    // @Test
    // public void test1() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    // }
    //
    // @Test
    // public void test2() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     for (int i = 0; i < 10000; i++) {
    //         Map<String, Object> record = new HashMap<>();
    //         record.put("id", i);
    //         record.put("name", "test" + i);
    //         record.put("desc", "测试" + i);
    //         record.put("count", i);
    //         boolean success = store1.insert(record) > 0;
    //         System.out.println(success);
    //     }
    // }
    //
    // @Test
    // public void test3() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     List<QueryParam> params = new ArrayList<>();
    //     List<Map<String, Object>> records = store1.selectList(params);
    //     System.out.println(records);
    // }
    //
    // @Test
    // public void test4() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     PrimaryKeyColumn column = new PrimaryKeyColumn();
    //     column.setColumnName("id");
    //     column.setColumnData(3);
    //     Map<String, Object> record = store1.selectOne(column);
    //     System.out.println(record);
    // }
    //
    // @Test
    // public void test5() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     PrimaryKeyColumn column = new PrimaryKeyColumn();
    //     column.setColumnName("id");
    //     column.setColumnData(3);
    //     boolean exist = store1.exist(column);
    //     System.out.println(exist);
    // }
    //
    // @Test
    // public void test6() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     boolean exist = store1.exist(3);
    //     System.out.println(exist);
    // }
    //
    // @Test
    // public void test7() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     Map<String, Object> record = store1.selectOne(3);
    //     System.out.println(record);
    // }
    //
    // @Test
    // public void test8() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     int result = store1.delete(3);
    //     System.out.println(result);
    // }
    //
    // @Test
    // public void test9() throws Exception {
    //     SqliteStore.initStore("d://test2.db");
    //     SqliteStore1 store1 = new SqliteStore1();
    //     PrimaryKeyColumn column = new PrimaryKeyColumn("id", 4);
    //     int result = store1.delete(column);
    //     System.out.println(result);
    // }

    public class SqliteStore1 extends SqliteStore {

        public SqliteStore1() throws Exception {
            super();
        }

        @Override
        protected Serializable newModel() {
            return null;
        }

        @Override
        protected Class modelClass() {
            return null;
        }

        @Override
        protected TableDefinition tableDefinition() {
            TableDefinition definition = new TableDefinition();
            definition.setTableName("users");

            ColumnDefinition definition1 = new ColumnDefinition();
            definition1.setColumnName("id");
            definition1.setColumnType("integer");
            definition1.setPrimaryKey(true);

            ColumnDefinition definition2 = new ColumnDefinition();
            definition2.setColumnName("name");
            definition2.setColumnType("text");

            ColumnDefinition definition3 = new ColumnDefinition();
            definition3.setColumnName("desc");
            definition3.setColumnType("text");

            ColumnDefinition definition4 = new ColumnDefinition();
            definition4.setColumnName("count");
            definition4.setColumnType("integer");

            definition.addColumn(definition1);
            definition.addColumn(definition2);
            definition.addColumn(definition3);
            definition.addColumn(definition4);

            return definition;
        }

        @Override
        protected Map<String, Object> toRecord(Serializable model) {
            return Map.of();
        }

        @Override
        protected Serializable toModel(Map record) {
            return null;
        }
    }

}
