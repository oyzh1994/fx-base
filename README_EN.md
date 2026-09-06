# fx-base

This is a JavaFX integration library that supports terminal emulation, SVG rendering, packaging, rich text editing, TTY for JavaFX, VNC for JavaFX, an FX version of the system tray implementation, themes, various UI components, and FX-related solutions.

---

## Dependencies

1. **base** project
   https://github.com/oyzh1994/base
2. **JDK version**: requires 25

---

## Module Structure

| Module | Description                                                                   |
|--------|-------------------------------------------------------------------------------|
| `fx-db`       | JavaFX database implementation, various database base components and wrappers |
| `fx-editor`   | JavaFX editor implementation, syntax-highlighted editor                       |
| `fx-pkg`      | JavaFX packaging implementation, cross-platform packaging features            |
| `fx-plus`     | JavaFX enhancements and extensions, as well as GUI component collections      |
| `fx-rich`     | JavaFX rich text implementation                                               |
| `fx-terminal` | JavaFX simulated terminal implementation                                      |
| `fx-tty`      | JavaFX pseudo-terminal implementation (PTY-based)                             |
| `fx-vnc`      | JavaFX VNC client implementation                                              |

---

## Maven

### Install
```bash
mvn -X clean install -DskipTests
```

### Notes
- Make sure the `java -version` output matches the project version; otherwise you may encounter errors such as "invalid target release 25"
