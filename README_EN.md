# fx-base

English | [简体中文](README.md)

`fx-base` is a JavaFX integration library providing UI enhancements, database components, rich-text editing, terminal emulation, TTY, VNC, RDP, X11/XServer, and other foundations for JavaFX desktop applications.

## Requirements

- JDK 25
- Maven 3.9 or newer
- The [`base`](https://gitee.com/oyzh1994/base) project

## Module Structure

| Module | Description |
|---|---|
| `fx-db` | JavaFX database components for data access, queries, import/export, and database UI |
| `fx-editor` | JavaFX editor and syntax-highlighting support |
| `fx-plus` | JavaFX controls, themes, windows, fonts, validation, and general UI enhancements |
| `fx-rdp` | JavaFX RDP remote-desktop client integration |
| `fx-rich` | JavaFX rich-text editing |
| `fx-terminal` | JavaFX terminal UI and terminal management |
| `fx-tty` | PTY- and JediTerm-based terminal emulation |
| `fx-vnc` | JavaFX VNC remote-desktop client |
| `fx-xserver` | An X11/XServer implemented in Java and rendered through JavaFX for real X11 clients |

## Maven

Install all modules:

```bash
mvn clean install -DskipTests
```

Run tests:

```bash
mvn test
```

## Notes

- Make sure `java -version` matches the project requirement; otherwise Maven may report `invalid target release: 25`.
- `fx-base` depends on [`base`](https://gitee.com/oyzh1994/base). Build and install `base` first, or resolve both projects from a local multi-project environment.
- See [`fx-xserver/README.md`](fx-xserver/README.md) for X11/VNC backends and runtime options.

## License

The project includes `APACHE-LICENSE-2.0.txt` and `MIT-LICENSE.txt`. Review the applicable terms before use.
