#!/bin/sh
# Run a command on the test VM (see docs/backlog.md §1 for what the VM is and what it has).
#
# Usage: tools/vm.sh 'command'
#
# The password is read from $XSSH_PASSWORD, or from ~/.fx-xserver-vm-pass - deliberately not in this
# repository. `expect` hangs on the password prompt in this environment, so the password goes through
# SSH_ASKPASS, which needs both the helper script and SSH_ASKPASS_REQUIRE=force.
set -e
: "${FX_XSERVER_VM_HOST:?set FX_XSERVER_VM_HOST to the verification VM address}"
: "${FX_XSERVER_VM_USER:=root}"

PASSWORD="${XSSH_PASSWORD:-}"
if [ -z "$PASSWORD" ] && [ -f "$HOME/.fx-xserver-vm-pass" ]; then
    PASSWORD="$(cat "$HOME/.fx-xserver-vm-pass")"
fi
if [ -z "$PASSWORD" ]; then
    echo "set XSSH_PASSWORD or write the password into ~/.fx-xserver-vm-pass (one line)" >&2
    exit 2
fi

ASKPASS="$(mktemp -t xs-askpass)"
trap 'rm -f "$ASKPASS"' EXIT
printf '#!/bin/sh\necho %s\n' "$PASSWORD" > "$ASKPASS"
chmod +x "$ASKPASS"

SSH_ASKPASS="$ASKPASS" SSH_ASKPASS_REQUIRE=force exec ssh -n \
    -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
    -o PreferredAuthentications=password -o PubkeyAuthentication=no \
    "$FX_XSERVER_VM_USER@$FX_XSERVER_VM_HOST" "$@"
