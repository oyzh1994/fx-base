# 项目
###### 项目说明
这是一个使用javafx的集成库，支持终端能力、svg能力、打包能力、富文本能力、fx版本的托盘实现、主题能力、各种组件和fx相关解决方案

###### 依赖说明
1. base工程  
 https://gitee.com/oyzh1994/base
2. jdk版本要求25

###### 结构说明 
fx-editor -> javafx的编辑器实现，语法高亮编辑器实现  
fx-pkg -> javafx的打包实现，各平台打包功能实现  
fx-plus -> javafx的增强和扩展，以及gui相关组件集合  
fx-rich -> javafx的富文本实现，目前已废弃，改由fx-editor替代   
fx-terminal -> javafx的终端实现

# Maven
###### 安装
mvn -X clean install -DskipTests

###### 注意
检查cmd里面java -version的版本号和项目版本号是否一致，否则可能出现无效的目标版本号24之类的问题