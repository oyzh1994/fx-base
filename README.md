# fx-base

这是一个使用 JavaFX 的集成库，支持终端仿真、SVG 渲染、打包、富文本编辑、TTY for JavaFX、VNC for JavaFX、FX 版本的托盘实现、主题、各种 UI 组件和 FX 相关解决方案。

---

## 依赖说明

1. **base** 工程  
   https://gitee.com/oyzh1994/base
2. **JDK 版本**：要求 25

---

## 结构说明

| 模块 | 说明 |
|---|---|
| `fx-editor` | JavaFX 编辑器实现，语法高亮编辑器 |
| `fx-pkg` | JavaFX 打包实现，各平台打包功能 |
| `fx-plus` | JavaFX 增强和扩展，以及 GUI 相关组件集合 |
| `fx-rich` | JavaFX 富文本实现 |
| `fx-terminal` | JavaFX 模拟终端实现 |
| `fx-tty` | JavaFX 仿真终端实现（基于 PTY） |
| `fx-vnc` | JavaFX VNC 客户端实现 |

---

## Maven

### 安装
```bash
mvn -X clean install -DskipTests
```

### 注意
- 检查 cmd 里面 `java -version` 的版本号和项目版本号是否一致，否则可能出现无效的目标版本号 25 之类的问题
