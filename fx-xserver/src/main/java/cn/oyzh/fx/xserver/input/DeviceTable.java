package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.event.XInputMasks;

import java.util.ArrayList;
import java.util.List;

/**
 * The devices this server has, which a client can change (backlog A48's second half).
 *
 * <p>Until now the device model was four constants: the two masters and the two XTEST slaves, with
 * their ids, types and names written into {@code XInputExtension}. That is enough to *describe* a
 * reference's first four devices, and not enough for the request that changes them -
 * {@code XIChangeHierarchy} - which is why the model is a table now.
 *
 * <p><b>What a reference does with each change was measured on 2026-10-04</b> (a selection for the
 * hierarchy event and a change list of one, then its device list read back):
 *
 * <ul>
 *   <li><b>{@code XIAddMaster}</b> with a name creates <b>four</b> devices, not two: {@code
 *       <name> pointer} and {@code <name> keyboard} (the new master pair, attached to each other)
 *       and {@code <name> XTEST pointer} and {@code <name> XTEST keyboard} (a slave pair attached to
 *       them). On a fresh reference the first four added ids were 8, 9, 10 and 11 - where its own
 *       six devices end - and the list went from six to ten.
 *   <li>Each new device also emitted an {@code XI_PropertyEvent} naming it. This server has **no
 *       device properties** (its {@code XIListProperties} answers none, deliberately), so there is
 *       no property for such an event to be about and none is sent - the difference is the absence of
 *       the property family, not of the device.
 *   <li>Removing the <b>core</b> master is refused, and so is a slave that is not a slave: the
 *       first is {@code BadValue} (measured: error 2, value 0) and the second is this extension's
 *       {@code BadDevice} naming the device.
 * </ul>
 *
 * <p>The table is server-wide, like a reference's device list. The *pointer positions* are not: they
 * live in each screen's {@link InputState}, which is this server's own structure and is unchanged by
 * a device appearing.
 */
public final class DeviceTable {

    /** One device, as the table keeps it: what {@code XIQueryDevice} reads back. */
    public record Device(int id, int type, int attachment, boolean enabled, String name,
                         int classSource) {

        public boolean isMaster() {
            return type == XInputMasks.MASTER_POINTER || type == XInputMasks.MASTER_KEYBOARD;
        }

        public boolean isPointer() {
            return type == XInputMasks.MASTER_POINTER || type == XInputMasks.SLAVE_POINTER;
        }
    }

    private final List<Device> devices = new ArrayList<>();

    /**
     * The next device id to hand out, which is where a reference's own first four end: this server
     * has devices 2 to 5, so an added master starts at 6 - a reference's starts at 8 because it also
     * has its backend's pair.
     */
    private int nextId = XInputMasks.HOST_KEYBOARD_DEVICE + 1;

    public DeviceTable() {
        // The four a reference leads with: the two masters and the two XTEST slaves (A48's first
        // half, measured with `XiProbe device 0`). A master's class records name the slave the class
        // comes from - device 2's button class says sourceid 4, exactly as a reference answers.
        devices.add(new Device(XInputMasks.POINTER_DEVICE, XInputMasks.MASTER_POINTER,
                XInputMasks.KEYBOARD_DEVICE, true, XInputMasks.POINTER_NAME,
                XInputMasks.XTEST_POINTER_DEVICE));
        devices.add(new Device(XInputMasks.KEYBOARD_DEVICE, XInputMasks.MASTER_KEYBOARD,
                XInputMasks.POINTER_DEVICE, true, XInputMasks.KEYBOARD_NAME,
                XInputMasks.XTEST_KEYBOARD_DEVICE));
        devices.add(new Device(XInputMasks.XTEST_POINTER_DEVICE, XInputMasks.SLAVE_POINTER,
                XInputMasks.POINTER_DEVICE, true, XInputMasks.XTEST_POINTER_NAME,
                XInputMasks.XTEST_POINTER_DEVICE));
        devices.add(new Device(XInputMasks.XTEST_KEYBOARD_DEVICE, XInputMasks.SLAVE_KEYBOARD,
                XInputMasks.KEYBOARD_DEVICE, true, XInputMasks.XTEST_KEYBOARD_NAME,
                XInputMasks.XTEST_KEYBOARD_DEVICE));
        // **And the host's own pair**, which is where a reference keeps its backend's: "Xvfb mouse"
        // is 6 and "Xvfb keyboard" 7, attached to the core pair. This server's input from the host
        // (the JavaFX window, or whatever backend is showing the screen) arrives on these two, so an
        // event's source id says XTEST or the host rather than always naming the master.
        devices.add(new Device(XInputMasks.HOST_POINTER_DEVICE, XInputMasks.SLAVE_POINTER,
                XInputMasks.POINTER_DEVICE, true, XInputMasks.HOST_POINTER_NAME,
                XInputMasks.HOST_POINTER_DEVICE));
        devices.add(new Device(XInputMasks.HOST_KEYBOARD_DEVICE, XInputMasks.SLAVE_KEYBOARD,
                XInputMasks.KEYBOARD_DEVICE, true, XInputMasks.HOST_KEYBOARD_NAME,
                XInputMasks.HOST_KEYBOARD_DEVICE));
    }

    /** Every device, in id order. */
    public List<Device> all() {
        return List.copyOf(devices);
    }

    public Device byId(int id) {
        for (Device device : devices) {
            if (device.id() == id) {
                return device;
            }
        }
        return null;
    }

    /** Whether this id is one of the devices, as opposed to a selector or a nonsense number. */
    public boolean isDevice(int id) {
        return byId(id) != null;
    }

    /** The devices a selector names: everything, or the masters alone. */
    public List<Device> forSelector(int selector) {
        if (selector == XInputMasks.ALL_DEVICES) {
            return all();
        }
        List<Device> masters = new ArrayList<>();
        for (Device device : devices) {
            if (device.isMaster()) {
                masters.add(device);
            }
        }
        return masters;
    }

    /**
     * {@code XIAddMaster}: the four devices a reference creates, with its names.
     *
     * @return the two masters' ids, the pointer first
     */
    public int[] addMaster(String name) {
        int pointer = nextId++;
        int keyboard = nextId++;
        int xtestPointer = nextId++;
        int xtestKeyboard = nextId++;
        devices.add(new Device(pointer, XInputMasks.MASTER_POINTER, keyboard, true,
                name + " pointer", xtestPointer));
        devices.add(new Device(keyboard, XInputMasks.MASTER_KEYBOARD, pointer, true,
                name + " keyboard", xtestKeyboard));
        devices.add(new Device(xtestPointer, XInputMasks.SLAVE_POINTER, pointer, true,
                name + " XTEST pointer", xtestPointer));
        devices.add(new Device(xtestKeyboard, XInputMasks.SLAVE_KEYBOARD, keyboard, true,
                name + " XTEST keyboard", xtestKeyboard));
        return new int[] {pointer, keyboard};
    }

    /**
     * {@code XIRemoveMaster}: the master and the pair of slaves that came with it go away.
     *
     * <p>The slaves a client had attached elsewhere are its own business: this server's own input
     * comes from the core pair's XTEST slaves, and those are not removable (the core masters are not
     * either - see {@link #isRemovable}).
     */
    public void removeMaster(int id) {
        Device master = byId(id);
        if (master == null) {
            return;
        }
        int pair = master.attachment();
        devices.removeIf(device -> device.id() == id || device.id() == pair
                || (device.isMaster() && device.attachment() == id)
                || (device.isMaster() && device.attachment() == pair)
                || (!device.isMaster()
                        && (device.attachment() == id || device.attachment() == pair)));
    }

    /** Whether a master may be removed: the core pair may not, as a reference refuses it. */
    public boolean isRemovable(int id) {
        Device device = byId(id);
        return device != null && device.isMaster()
                && id != XInputMasks.POINTER_DEVICE && id != XInputMasks.KEYBOARD_DEVICE;
    }

    /** {@code XIAttachSlave}: a slave belongs to another master of the same kind. */
    public void attachSlave(int slaveId, int masterId) {
        Device slave = byId(slaveId);
        if (slave == null || slave.isMaster()) {
            return;
        }
        replace(slave, new Device(slave.id(), slave.type(), masterId, slave.enabled(), slave.name(),
                slave.classSource()));
    }

    /** {@code XIDetachSlave}: the slave becomes floating, which is attachment {@code 0}. */
    public void detachSlave(int slaveId) {
        Device slave = byId(slaveId);
        if (slave == null || slave.isMaster()) {
            return;
        }
        replace(slave, new Device(slave.id(), slave.type(), 0, slave.enabled(), slave.name(),
                slave.classSource()));
    }

    private void replace(Device was, Device now) {
        devices.set(devices.indexOf(was), now);
    }
}
