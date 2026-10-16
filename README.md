# fx-base

[English](README_EN.md) | 简体中文

`fx-base` 是一个 JavaFX 集成库，提供 UI 增强、数据库组件、富文本编辑、终端仿真、TTY、VNC、RDP、X11/XServer 以及其他 JavaFX 桌面应用基础能力。

## 环境要求

- JDK 25
- Maven 3.9 或更高版本
- [`base`](https://gitee.com/oyzh1994/base) 基础工程

## 模块结构

| 模块 | 说明 |
|---|---|
| `fx-db` | JavaFX 数据库组件，提供数据访问、查询、导入导出和数据库 UI 基础能力 |
| `fx-editor` | JavaFX 编辑器和语法高亮能力 |
| `fx-plus` | JavaFX 控件、主题、窗口、字体、校验和通用 UI 增强 |
| `fx-rdp` | JavaFX RDP 远程桌面客户端集成 |
| `fx-rich` | JavaFX 富文本编辑能力 |
| `fx-terminal` | JavaFX 终端界面和终端管理能力 |
| `fx-tty` | 基于 PTY 和 JediTerm 的终端仿真能力 |
| `fx-vnc` | JavaFX VNC 远程桌面客户端 |
| `fx-xserver` | Java 实现并渲染到 JavaFX 的 X11/XServer，可为真实 X11 客户端提供显示服务 |

## Maven

安装全部模块：

```bash
mvn clean install -DskipTests
```

运行测试：

```bash
mvn test
```

## 注意事项

- 请确保终端中的 `java -version` 与项目要求一致，否则可能出现 `invalid target release: 25`。
- `fx-base` 依赖 [`base`](https://gitee.com/oyzh1994/base)，请先构建并安装 `base`，或直接从本地多工程环境解析依赖。
- `fx-xserver` 的详细运行参数和 X11/VNC 后端说明见 [`fx-xserver/README.md`](fx-xserver/README.md)。

## 许可证

项目包含 `APACHE-LICENSE-2.0.txt` 和 `MIT-LICENSE.txt`，使用前请查看相应许可条款。
