# fx-base

A JavaFX integration library providing terminal emulation, SVG rendering, packaging, rich text editing, TTY for JavaFX, VNC for JavaFX, system tray implementation for JavaFX, theming, various UI components, and FX-related solutions.

---

## Dependencies

1. **base** project  
   https://gitee.com/oyzh1994/base
2. **JDK Version**: 25 is required

---

## Module Structure

| Module | Description |
|---|---|
| `fx-editor` | JavaFX editor implementation with syntax highlighting |
| `fx-pkg` | JavaFX packaging implementation with cross-platform packaging support |
| `fx-plus` | JavaFX enhancements, extensions, and GUI component collection |
| `fx-rich` | JavaFX rich text implementation |
| `fx-terminal` | JavaFX simulated terminal implementation |
| `fx-tty` | JavaFX terminal emulator implementation (PTY-based) |
| `fx-vnc` | JavaFX VNC client implementation |

---

## Maven

### Build
```bash
mvn -X clean install -DskipTests
```

### Notes
- Ensure that `java -version` in your terminal matches the project's JDK version. Mismatches cause errors like "invalid target release: 25".
