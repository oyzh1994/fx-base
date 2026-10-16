#!/bin/sh
# Run a command on the test VM (see docs/backlog.md §1 for what the VM is and what it has).
#
# Usage: tools/vm.sh 'command'
#
# The password is read from $XSSH_PASSWORD, or from ~/.xserver-vm-pass - deliberately not in this
# repository. `expect` hangs on the password prompt in this environment, so the password goes through
# SSH_ASKPASS, which needs both the helper script and SSH_ASKPASS_REQUIRE=force.
set -e
: "${XSERVER_VM_HOST:=192.168.22.152}"
: "${XSERVER_VM_USER:=root}"

PASSWORD="${XSSH_PASSWORD:-}"
if [ -z "$PASSWORD" ] && [ -f "$HOME/.xserver-vm-pass" ]; then
    PASSWORD="$(cat "$HOME/.xserver-vm-pass")"
fi
if [ -z "$PASSWORD" ]; then
    echo "set XSSH_PASSWORD or write the password into ~/.xserver-vm-pass (one line)" >&2
    exit 2
fi

ASKPASS="$(mktemp -t xs-askpass)"
trap 'rm -f "$ASKPASS"' EXIT
printf '#!/bin/sh\necho %s\n' "$PASSWORD" > "$ASKPASS"
chmod +x "$ASKPASS"

SSH_ASKPASS="$ASKPASS" SSH_ASKPASS_REQUIRE=force exec ssh -n \
    -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
    -o PreferredAuthentications=password -o PubkeyAuthentication=no \
    "$XSERVER_VM_USER@$XSERVER_VM_HOST" "$@"
