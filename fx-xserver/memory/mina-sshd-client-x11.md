---
name: mina-sshd-client-x11
description: "Apache MINA SSHD has no released client-side X11 forwarding; JSch and sshj do, and the community PR is buggy"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 20e98c73-c9b5-4cc8-9d2e-466cca178651
  modified: 2026-09-21T18:41:18.237Z
---

Apache MINA SSHD ships **only the server half** of X11 forwarding (`org.apache.sshd.server.x11.*`: `X11ForwardSupport`, `DefaultX11ForwardSupport`, `ChannelForwardedX11`, plus `server.forward.X11ForwardingFilter`). Verified by scanning every `sshd-*.jar` in `~/.m2` (2.8.0 … 2.19.0): no `org.apache.sshd.client.x11.*` class is published, and `ChannelSession` has no `setXForwarding`. `org.apache.sshd.client.x11.X11ChannelFactory` — imported by `oyzh/easyssh/.../ssh/ShellBaseSSHClient.java` and `oyzh/easyshell/.../ssh2/ShellBaseSSHClient.java` — only exists in MINA's **test** sources, so those files cannot compile against released jars.

The client half that must be written by hand:
1. send `x11-req` (single connection, auth protocol, auth cookie, screen number) with `Buffer` + `getRecipient()` + `writePacket()`, copying `ChannelSession.sendEnvVariables()`;
2. register a `ChannelFactory` whose name is `"x11"` and override `open(sender, rwsize, rmpsize, buffer)` — MINA's `AbstractConnectionService.channelOpen()` sends the `CHANNEL_OPEN_CONFIRMATION` itself, the payload is only originator address + port (mirror `ChannelForwardedX11.open()`), then bridge the socket to the channel the way `ChannelDirectTcpip` does.

Outcome in practice (user's own testing): JSch and sshj X11 forwarding both work for their window; the MINA community PR was too buggy to keep. For this project's product shape the client half can also be trivial — the X server is local and auth-free, so it can connect straight to `127.0.0.1:600N` / `/tmp/.X11-unix/XN` with a throwaway cookie and skip `DISPLAY`/`xauth` altogether.
