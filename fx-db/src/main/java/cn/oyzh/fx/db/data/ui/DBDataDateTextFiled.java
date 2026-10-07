package cn.oyzh.fx.db.data.ui;


import cn.oyzh.fx.gui.text.field.SelectTextFiled;

/**
 * 数据库数据日期格式输入框，提供常用日期时间格式的可选项
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBDataDateTextFiled extends SelectTextFiled<String> {

    @Override
    public void initNode() {
        this.addItem("yyyy-MM-dd HH:mm:ss");
        this.addItem("yyyy/MM/dd HH:mm:ss");

        this.addItem("yyyy-M-d HH:mm:ss");
        this.addItem("yyyy/M/d HH:mm:ss");

        this.addItem("dd-MM-yyyy HH:mm:ss");
        this.addItem("dd/MM/yyyy HH:mm:ss");

        this.addItem("MM-dd-yyyy HH:mm:ss");
        this.addItem("MM/dd/yyyy HH:mm:ss");

        this.addItem("M-d-yyyy HH:mm:ss");
        this.addItem("M/d/yyyy HH:mm:ss");

        this.addItem("d-M-yyyy HH:mm:ss");
        this.addItem("d/M/yyyy HH:mm:ss");
        super.initNode();
    }
}
