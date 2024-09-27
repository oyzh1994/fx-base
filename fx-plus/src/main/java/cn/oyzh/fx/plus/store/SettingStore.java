// package cn.oyzh.fx.plus.store;
//
// import cn.oyzh.fx.common.jdbc.ColumnDefinition;
// import cn.oyzh.fx.common.sqlite.SqliteStore;
// import cn.oyzh.fx.common.jdbc.TableDefinition;
// import cn.oyzh.fx.plus.domain.Setting;
//
// import java.util.HashMap;
// import java.util.Map;
//
//
// /**
//  * @author oyzh
//  * @since 2024/09/23
//  */
// public abstract class SettingStore<E extends Setting> extends SqliteStore<E> {
//
//     protected static final String DATA_UID = "current";
//
//     // @Override
//     // protected TableDefinition tableDefinition() {
//     //     TableDefinition definition = new TableDefinition();
//     //     definition.setTableName("t_setting");
//     //
//     //     ColumnDefinition uid = new ColumnDefinition();
//     //     uid.setColumnName("uid");
//     //     uid.setColumnType("text");
//     //     uid.setPrimaryKey(true);
//     //     definition.addColumnDefinition(uid);
//     //
//     //     // 主题
//     //     ColumnDefinition theme = new ColumnDefinition();
//     //     theme.setColumnName("theme");
//     //     theme.setColumnType("text");
//     //     ColumnDefinition bgColor = new ColumnDefinition();
//     //     bgColor.setColumnName("bgColor");
//     //     bgColor.setColumnType("text");
//     //     ColumnDefinition fgColor = new ColumnDefinition();
//     //     fgColor.setColumnName("fgColor");
//     //     fgColor.setColumnType("text");
//     //     ColumnDefinition accentColor = new ColumnDefinition();
//     //     accentColor.setColumnName("accentColor");
//     //     accentColor.setColumnType("text");
//     //     definition.addColumnDefinition(theme);
//     //     definition.addColumnDefinition(bgColor);
//     //     definition.addColumnDefinition(fgColor);
//     //     definition.addColumnDefinition(accentColor);
//     //
//     //     // 基本
//     //     ColumnDefinition locale = new ColumnDefinition();
//     //     locale.setColumnName("locale");
//     //     locale.setColumnType("text");
//     //     ColumnDefinition exitMode = new ColumnDefinition();
//     //     exitMode.setColumnName("exitMode");
//     //     exitMode.setColumnType("integer");
//     //     ColumnDefinition opacity = new ColumnDefinition();
//     //     opacity.setColumnName("opacity");
//     //     opacity.setColumnType("double");
//     //     definition.addColumnDefinition(locale);
//     //     definition.addColumnDefinition(exitMode);
//     //     definition.addColumnDefinition(opacity);
//     //
//     //     // tab
//     //     ColumnDefinition tabLimit = new ColumnDefinition();
//     //     tabLimit.setColumnName("tabLimit");
//     //     tabLimit.setColumnType("integer");
//     //     ColumnDefinition tabStrategy = new ColumnDefinition();
//     //     tabStrategy.setColumnName("tabStrategy");
//     //     tabStrategy.setColumnType("text");
//     //     definition.addColumnDefinition(tabLimit);
//     //     definition.addColumnDefinition(tabStrategy);
//     //
//     //     // 字体
//     //     ColumnDefinition fontSize = new ColumnDefinition();
//     //     fontSize.setColumnName("fontSize");
//     //     fontSize.setColumnType("integer");
//     //     ColumnDefinition fontWeight = new ColumnDefinition();
//     //     fontWeight.setColumnName("fontWeight");
//     //     fontWeight.setColumnType("integer");
//     //     ColumnDefinition fontFamily = new ColumnDefinition();
//     //     fontFamily.setColumnName("fontFamily");
//     //     fontFamily.setColumnType("text");
//     //     definition.addColumnDefinition(fontSize);
//     //     definition.addColumnDefinition(fontWeight);
//     //     definition.addColumnDefinition(fontFamily);
//     //
//     //     // 页面
//     //     ColumnDefinition rememberPageSize = new ColumnDefinition();
//     //     rememberPageSize.setColumnName("rememberPageSize");
//     //     rememberPageSize.setColumnType("integer");
//     //     ColumnDefinition rememberPageResize = new ColumnDefinition();
//     //     rememberPageResize.setColumnName("rememberPageResize");
//     //     rememberPageResize.setColumnType("integer");
//     //     ColumnDefinition rememberPageLocation = new ColumnDefinition();
//     //     rememberPageLocation.setColumnName("rememberPageLocation");
//     //     rememberPageLocation.setColumnType("integer");
//     //     definition.addColumnDefinition(rememberPageSize);
//     //     definition.addColumnDefinition(rememberPageResize);
//     //     definition.addColumnDefinition(rememberPageLocation);
//     //     return definition;
//     // }
//     //
//     // @Override
//     // protected Map<String, Object> toRecord(E model) {
//     //     Map<String, Object> record = new HashMap<>();
//     //     // 数据id
//     //     record.put("uid", DATA_UID);
//     //
//     //     // 主题
//     //     record.put("theme", model.getTheme());
//     //     record.put("bgColor", model.getBgColor());
//     //     record.put("fgColor", model.getFgColor());
//     //     record.put("accentColor", model.getAccentColor());
//     //
//     //     // 基本
//     //     record.put("locale", model.getLocale());
//     //     record.put("exitMode", model.getExitMode());
//     //     record.put("opacity", model.getOpacity());
//     //
//     //     // tab
//     //     record.put("tabLimit", model.getTabLimit());
//     //     record.put("tabStrategy", model.getTabStrategy());
//     //
//     //     // 字体
//     //     record.put("fontSize", model.getFontSize());
//     //     record.put("fontWeight", model.getFontWeight());
//     //     record.put("fontFamily", model.getFontFamily());
//     //
//     //     // 页面
//     //     record.put("rememberPageSize", model.getRememberPageSize());
//     //     record.put("rememberPageResize", model.getRememberPageResize());
//     //     record.put("rememberPageLocation", model.getRememberPageLocation());
//     //     return record;
//     // }
//     //
//     // @Override
//     // protected E toModel(Map<String, Object> record) {
//     //     E model = this.newModel();
//     //
//     //     // 主题
//     //     model.setTheme((String) record.get("theme"));
//     //     model.setBgColor((String) record.get("bgColor"));
//     //     model.setFgColor((String) record.get("fgColor"));
//     //     model.setAccentColor((String) record.get("accentColor"));
//     //
//     //     // 基本
//     //     model.setLocale((String) record.get("locale"));
//     //     model.setOpacity((Double) record.get("opacity"));
//     //     model.setExitMode((Integer) record.get("exitMode"));
//     //
//     //     // tab
//     //     model.setTabLimit((Integer) record.get("tabLimit"));
//     //     model.setTabStrategy((String) record.get("tabStrategy"));
//     //
//     //     // 字体
//     //     model.setFontSize((Integer) record.get("fontSize"));
//     //     model.setFontFamily((String) record.get("fontFamily"));
//     //     model.setFontWeight((Integer) record.get("fontWeight"));
//     //
//     //     // 页面
//     //     model.setRememberPageSize((Integer) record.get("rememberPageSize"));
//     //     model.setRememberPageResize((Integer) record.get("rememberPageResize"));
//     //     model.setRememberPageLocation((Integer) record.get("rememberPageLocation"));
//     //
//     //     return model;
//     // }
// }
