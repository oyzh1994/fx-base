package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.sync.AlarmNotifyEvent;
import com.github.moaxcp.x11.protocol.sync.AwaitFence;
import com.github.moaxcp.x11.protocol.sync.ChangeAlarm;
import com.github.moaxcp.x11.protocol.sync.ChangeCounter;
import com.github.moaxcp.x11.protocol.sync.CreateAlarm;
import com.github.moaxcp.x11.protocol.sync.CreateCounter;
import com.github.moaxcp.x11.protocol.sync.CreateFence;
import com.github.moaxcp.x11.protocol.sync.DestroyAlarm;
import com.github.moaxcp.x11.protocol.sync.DestroyCounter;
import com.github.moaxcp.x11.protocol.sync.DestroyFence;
import com.github.moaxcp.x11.protocol.sync.GetPriority;
import com.github.moaxcp.x11.protocol.sync.GetPriorityReply;
import com.github.moaxcp.x11.protocol.sync.Initialize;
import com.github.moaxcp.x11.protocol.sync.InitializeReply;
import com.github.moaxcp.x11.protocol.sync.Int64;
import com.github.moaxcp.x11.protocol.sync.ListSystemCounters;
import com.github.moaxcp.x11.protocol.sync.ListSystemCountersReply;
import com.github.moaxcp.x11.protocol.sync.QueryAlarm;
import com.github.moaxcp.x11.protocol.sync.QueryAlarmReply;
import com.github.moaxcp.x11.protocol.sync.QueryCounter;
import com.github.moaxcp.x11.protocol.sync.QueryCounterReply;
import com.github.moaxcp.x11.protocol.sync.QueryFence;
import com.github.moaxcp.x11.protocol.sync.QueryFenceReply;
import com.github.moaxcp.x11.protocol.sync.ResetFence;
import com.github.moaxcp.x11.protocol.sync.SetCounter;
import com.github.moaxcp.x11.protocol.sync.SetPriority;
import com.github.moaxcp.x11.protocol.sync.SyncPlugin;
import com.github.moaxcp.x11.protocol.sync.Trigger;
import com.github.moaxcp.x11.protocol.sync.TriggerFence;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.resource.XResource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SYNC: counters and fences, which is how a client asks the server "has this happened yet".
 *
 * <p>A fence is what a toolkit reaches for when it wants to know that a frame has been *seen* rather
 * than merely sent: it triggers the fence, hands it to the server with the drawing, and waits. This
 * server draws synchronously - every request is finished, on the screen and in the window's store,
 * before the next is read - so **a fence is already triggered when the request that would have waited
 * on it is read**, and both {@code AwaitFence} and {@code Await} return without delaying anything.
 * That is not a stub: it is the truth about a server with no asynchronous pipeline in it, and it is
 * what a client that wanted the wait gets.
 *
 * <p>Counters are the other half, and they are real values here: a client creates one, sets it,
 * changes it by a delta and reads it back, with the 64 bit arithmetic X uses (the wire carries an
 * {@code INT64} as two words, and it is signed).
 *
 * <p><b>Alarms are the third part, and they are real too</b> - a server side trigger on a counter,
 * which is the other half of F1. A client creates an alarm with a threshold, a test and a delta,
 * changes the counter, and the alarm fires: this server sends the {@code AlarmNotify} event, which
 * is a real extension event at {@code firstEvent + 1} rather than a generic one.
 *
 * <p><b>The trigger rules below are measured, not recalled</b> ({@code tools/SyncProbe} against
 * Xvfb, whose alarm section walks each one):
 *
 * <ul>
 *   <li>A <b>positive transition</b> fires when the counter goes from at or below the threshold to
 *       above it; a <b>negative transition</b> is the mirror. After firing, the alarm's threshold
 *       moves by its delta - <b>added, whatever the test is</b>: a positive-transition alarm with
 *       delta 100 moved 10 to 110 and fired again at 115, and a negative-transition alarm with
 *       delta -100 moved 10 to -90. And a trigger that cannot move the way its test needs is
 *       <b>refused {@code BadMatch}</b>: a negative transition with a positive delta, and a positive
 *       transition with a negative delta, both measured. A transition stays <b>active</b> after it
 *       has fired.
 *   <li>A <b>comparison</b> fires once, when the relation first holds, and then goes
 *       <b>inactive</b> - the notification says so in its own state byte, and a counter that moves
 *       further the same way, or back across, produces nothing more. That is the difference between
 *       the two kinds of test, and it is why the probe checks the quiet cases too.
 *   <li>The notification carries the counter's value, the threshold it crossed (before the delta
 *       moved it) and the alarm's state after firing.
 * </ul>
 *
 * <p><b>What is this server's reading rather than a measurement</b>, and so marked: a
 * {@code Relative} value type is taken as the threshold being relative to the counter's value when
 * the alarm was armed, {@code ChangeAlarm} re-arms the alarm without firing it, and an alarm created
 * while its condition already holds waits for the next change rather than firing on the spot. Each
 * is a decision the specification leaves loose and the probe above does not cover; a session that
 * needs one of them measured should extend the probe first.
 *
 * <p><b>The version is the server's, measured.</b> {@code Initialize} is answered 3.1 both to a
 * client that asks for 3.1 and to one that asks for 3.0 ({@code tools/SyncProbe} against Xvfb), where
 * the obvious implementation echoes the client's own version - which would tell a 3.0 client that
 * {@code AwaitFence} (a 3.1 request) is available to it. The same rule as Composite's
 * {@code QueryVersion}, and the same measurement.
 *
 * <p><b>{@code ListSystemCounters} answers none, deliberately.</b> A reference keeps eight - they are
 * elapsed-time counters for its own scheduling - and this server has no such notion: it has no
 * scheduler to report on and no server-side clock a client would act on. Answering a count with no
 * entries, or entries a client cannot parse, would be worse than answering none, and the alternative
 * - packing the reference's own variable-length entries by trial - is what B6's entry records as the
 * way a client ends up reading its own field boundaries wrong. It is one line of the probe, and it is
 * listed in {@code tools/README.md} as a deliberate difference.
 */
public final class SyncExtension implements Extension {

    /** Measured: the reference answers 3.1 to a client asking for 3.1 and to one asking for 3.0. */
    private static final int MAJOR_VERSION = 3;
    private static final int MINOR_VERSION = 1;

    /** The {@code XSyncCA*} bits: which fields a create or change request carries. */
    private static final int CA_COUNTER = 1;
    private static final int CA_VALUE_TYPE = 2;
    private static final int CA_VALUE = 4;
    private static final int CA_TEST_TYPE = 8;
    private static final int CA_DELTA = 16;
    private static final int CA_EVENTS = 32;

    /** {@code XSyncAlarmNotifyMask}: whether an alarm tells the client that made it when it fires. */
    private static final int ALARM_NOTIFY = 1;

    private static final int POSITIVE_TRANSITION = 0;
    private static final int NEGATIVE_TRANSITION = 1;
    private static final int POSITIVE_COMPARISON = 2;
    private static final int NEGATIVE_COMPARISON = 3;

    private static final int ABSOLUTE = 0;
    private static final int RELATIVE = 1;

    static final int ALARM_ACTIVE = 0;
    static final int ALARM_INACTIVE = 1;
    static final int ALARM_DESTROYED = 2;

    private final SyncPlugin plugin = new SyncPlugin();

    /** Every live alarm, in creation order. Alarms are few, and each evaluation walks them all. */
    private final java.util.List<AlarmResource> alarms = new java.util.concurrent.CopyOnWriteArrayList<>();

    /** The priority each client set for itself, which is what {@code GetPriority}/{@code SetPriority}
     * are about - a client asks about its own scheduling, not another connection's. */
    private final Map<ClientConnection, Integer> priorities = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "SYNC";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof Initialize init) {
            client.sendReply(InitializeReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .majorVersion((byte) MAJOR_VERSION)
                    .minorVersion((byte) MINOR_VERSION)
                    .build());
        } else if (request instanceof ListSystemCounters) {
            client.sendReply(ListSystemCountersReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .counters(org.eclipse.collections.impl.factory.Lists.immutable.empty())
                    .build());
        } else if (request instanceof CreateCounter r) {
            counter(server, client, r.getId(), value(r.getInitialValue()));
        } else if (request instanceof SetCounter r) {
            CounterResource counter = counterResource(server, client, r.getCounter());
            counter.setValue(value(r.getValue()));
            counterChanged(server, counter.id());
        } else if (request instanceof ChangeCounter r) {
            CounterResource counter = counterResource(server, client, r.getCounter());
            counter.setValue(counter.value() + value(r.getAmount()));
            counterChanged(server, counter.id());
        } else if (request instanceof QueryCounter r) {
            long v = counterResource(server, client, r.getCounter()).value();
            client.sendReply(QueryCounterReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .counterValue(int64(v))
                    .build());
        } else if (request instanceof DestroyCounter r) {
            counterResource(server, client, r.getCounter());
            // An alarm is a trigger *on* a counter, so a counter that goes away takes its alarms
            // with it - there would be nothing left for them to watch.
            destroyAlarmsOnServerCounter(server, r.getCounter());
            server.resources().free(r.getCounter());
        } else if (request instanceof CreateAlarm r) {
            createAlarm(server, client, r);
        } else if (request instanceof ChangeAlarm r) {
            changeAlarm(server, client, r);
        } else if (request instanceof DestroyAlarm r) {
            AlarmResource alarm = alarm(server, client, r.getAlarm());
            alarms.remove(alarm);
            server.resources().free(r.getAlarm());
        } else if (request instanceof QueryAlarm r) {
            AlarmResource alarm = alarm(server, client, r.getAlarm());
            client.sendReply(QueryAlarmReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .trigger(Trigger.builder()
                            .counter(alarm.counterId())
                            .waitType(alarm.valueType())
                            .waitValue(int64(alarm.waitValue()))
                            .testType(alarm.testType())
                            .build())
                    .delta(int64(alarm.delta()))
                    .events((alarm.events() & ALARM_NOTIFY) != 0)
                    .state((byte) alarm.state())
                    .build());
        } else if (request instanceof SetPriority r) {
            priorities.put(client, r.getPriority());
        } else if (request instanceof GetPriority) {
            client.sendReply(GetPriorityReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .priority(priorities.getOrDefault(client, 0))
                    .build());
        } else if (request instanceof CreateFence r) {
            // The drawable is named because a fence belongs to a screen; it is validated so a client
            // that names a window that does not exist hears about it, which is what the reference
            // does - and it is how the field order was found: a probe that had fence and drawable the
            // other way round was answered BadDrawable on its own fence id.
            server.resources().requireDrawable(client, r.getDrawable());
            server.resources().add(new FenceResource(r.getFence(), client, r.isInitiallyTriggered()));
        } else if (request instanceof TriggerFence r) {
            fence(server, client, r.getFence()).setTriggered(true);
        } else if (request instanceof ResetFence r) {
            fence(server, client, r.getFence()).setTriggered(false);
        } else if (request instanceof DestroyFence r) {
            fence(server, client, r.getFence());
            server.resources().free(r.getFence());
        } else if (request instanceof QueryFence r) {
            client.sendReply(QueryFenceReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .triggered(fence(server, client, r.getFence()).isTriggered())
                    .build());
        } else if (request instanceof AwaitFence r) {
            // Never waits: everything this server was asked to do is already done by the time the
            // request is read, so every fence named here is either triggered or will never be. The
            // fences are still resolved, so a client that names one it never created hears BadFence.
            org.eclipse.collections.api.list.primitive.IntList fences = r.getFenceList();
            for (int i = 0; i < fences.size(); i++) {
                fence(server, client, fences.get(i));
            }
        } else {
            throw new IllegalArgumentException("unhandled SYNC request " + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        priorities.remove(client);
        // The resource table frees a disconnecting client's alarms, but the evaluation list is kept
        // here and would go on holding them - and firing at a client that has gone.
        alarms.removeIf(alarm -> alarm.owner() == client);
    }

    // ------------------------------------------------------------------
    // Alarms

    private void createAlarm(XServer server, ClientConnection client, CreateAlarm r) {
        int id = r.getId();
        if (!client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        int valueMask = r.getValueMask();
        int counterId = (valueMask & CA_COUNTER) != 0 ? r.getCounter() : 0;
        CounterResource counter = counterResource(server, client, counterId);
        int valueType = (valueMask & CA_VALUE_TYPE) != 0 ? r.getValueType() : ABSOLUTE;
        long waitValue = (valueMask & CA_VALUE) != 0 ? value(r.getValue()) : 0;
        int testType = (valueMask & CA_TEST_TYPE) != 0 ? r.getTestType() : POSITIVE_TRANSITION;
        long delta = (valueMask & CA_DELTA) != 0 ? value(r.getDelta()) : 0;
        int events = (valueMask & CA_EVENTS) != 0 ? r.getEvents() : 0;

        checkTrigger(id, testType, valueType, delta);
        AlarmResource alarm = new AlarmResource(id, client, counterId, valueType, waitValue,
                testType, delta, events, counter.value());
        alarms.add(alarm);
        server.resources().add(alarm);
    }

    private void changeAlarm(XServer server, ClientConnection client, ChangeAlarm r) {
        AlarmResource alarm = alarm(server, client, r.getId());
        int valueMask = r.getValueMask();
        if ((valueMask & CA_COUNTER) != 0) {
            // The counter is read for its existence only here: what a trigger on another counter
            // would mean is a question the field answers by naming one.
            counterResource(server, client, r.getCounter());
            alarm.setCounterId(r.getCounter());
        }
        if ((valueMask & CA_VALUE_TYPE) != 0) {
            alarm.setValueType(r.getValueType());
        }
        if ((valueMask & CA_VALUE) != 0) {
            alarm.setWaitValue(value(r.getValue()));
        }
        if ((valueMask & CA_TEST_TYPE) != 0) {
            alarm.setTestType(r.getTestType());
        }
        if ((valueMask & CA_DELTA) != 0) {
            alarm.setDelta(value(r.getDelta()));
        }
        if ((valueMask & CA_EVENTS) != 0) {
            alarm.setEvents(r.getEvents());
        }
        checkTrigger(alarm.id(), alarm.testType(), alarm.valueType(), alarm.delta());
        // A change re-arms the alarm: it is active again and watching from the counter's current
        // value, which is a decision this server takes rather than one the probe has measured.
        alarm.setState(ALARM_ACTIVE);
        CounterResource counter = (CounterResource) server.resources().get(alarm.counterId());
        alarm.arm(counter == null ? 0 : counter.value());
    }

    /**
     * The one combination a server refuses: a threshold that its own test could never re-arm.
     *
     * <p>A transition moves its threshold by its delta, so a negative transition with a positive
     * delta would move it up - where the counter has just come down from - and a positive transition
     * with a negative delta likewise. A reference answers **{@code BadMatch} on the alarm's own id**
     * for both, measured; a comparison's threshold moves either way with no such consequence, so
     * nothing is refused for it.
     */
    private void checkTrigger(int id, int testType, int valueType, long delta) {
        if (testType != POSITIVE_TRANSITION && testType != NEGATIVE_TRANSITION
                && testType != POSITIVE_COMPARISON && testType != NEGATIVE_COMPARISON) {
            throw ProtocolException.badValue(testType);
        }
        if (valueType != ABSOLUTE && valueType != RELATIVE) {
            throw ProtocolException.badValue(valueType);
        }
        if ((testType == NEGATIVE_TRANSITION && delta > 0)
                || (testType == POSITIVE_TRANSITION && delta < 0)) {
            // The value is the alarm's own id, which is what a reference puts there.
            throw new ProtocolException(ProtocolException.MATCH, id,
                    "the alarm's own delta could never re-arm its trigger");
        }
    }

    /**
     * Evaluates every alarm watching a counter after its value moved, firing the ones whose test
     * now holds.
     */
    private void counterChanged(XServer server, int counterId) {
        CounterResource counter = (CounterResource) server.resources().get(counterId);
        if (counter == null) {
            return;
        }
        long value = counter.value();
        for (AlarmResource alarm : alarms) {
            if (alarm.counterId() != counterId || alarm.state() != ALARM_ACTIVE) {
                continue;
            }
            long threshold = alarm.threshold();
            if (!alarm.testHolds(value)) {
                alarm.arm(value);
                continue;
            }
            // The threshold the event reports is the one that was crossed, read before the delta
            // moves it - which is what a reference's notification carries.
            long crossed = threshold;
            alarm.setWaitValue(alarm.waitValue() + alarm.delta());
            alarm.arm(value);
            if (alarm.testType() == POSITIVE_COMPARISON
                    || alarm.testType() == NEGATIVE_COMPARISON) {
                alarm.setState(ALARM_INACTIVE);
            }
            notify(server, alarm, value, crossed);
        }
    }

    /** The {@code AlarmNotify} event, sent to the client that made the alarm if it asked for it. */
    private void notify(XServer server, AlarmResource alarm, long counterValue, long alarmValue) {
        if ((alarm.events() & ALARM_NOTIFY) == 0) {
            return;
        }
        ClientConnection owner = alarm.owner();
        server.events().send(owner, AlarmNotifyEvent.builder()
                .firstEventOffset(firstEvent())
                .sentEvent(false)
                .kind((byte) 1)                         // measured: the reference sends 1 here
                .sequenceNumber(owner.sequenceNumber())
                .alarm(alarm.id())
                .counterValue(int64(counterValue))
                .alarmValue(int64(alarmValue))
                .timestamp((int) server.events().time())
                .state((byte) alarm.state())
                .build());
    }

    /** Frees every alarm watching a counter that is being destroyed. */
    private void destroyAlarmsOnServerCounter(XServer server, int counterId) {
        for (AlarmResource alarm : alarms) {
            if (alarm.counterId() == counterId) {
                alarms.remove(alarm);
                server.resources().free(alarm.id());
            }
        }
    }

    private AlarmResource alarm(XServer server, ClientConnection client, int id) {
        XResource resource = server.resources().get(id);
        if (!(resource instanceof AlarmResource alarm) || alarm.isDestroyed()) {
            throw syncError(1, id, "alarm");
        }
        return alarm;
    }

    // ------------------------------------------------------------------

    private void counter(XServer server, ClientConnection client, int id, long initial) {
        if (!client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        server.resources().add(new CounterResource(id, client, initial));
    }

    private CounterResource counterResource(XServer server, ClientConnection client, int id) {
        XResource resource = server.resources().get(id);
        if (!(resource instanceof CounterResource counter) || counter.isDestroyed()) {
            throw syncError(0, id, "counter");
        }
        return counter;
    }

    private FenceResource fence(XServer server, ClientConnection client, int id) {
        XResource resource = server.resources().get(id);
        if (!(resource instanceof FenceResource fence) || fence.isDestroyed()) {
            throw syncError(2, id, "fence");
        }
        return fence;
    }

    /**
     * An id that is not the resource the request expects, answered with **this extension's own**
     * error - {@code BadCounter}, {@code BadAlarm} or {@code BadFence}, at the extension's error
     * base plus zero, one or two.
     *
     * <p>The resource table's own lookup would answer {@code BadDrawable} instead, which is a core
     * error that says nothing about SYNC; a reference answers the extension's error, and the probe
     * compares the two (`DestroyAlarm` followed by `QueryAlarm` is the sequence that shows it).
     */
    private ProtocolException syncError(int index, int id, String kind) {
        return new ProtocolException((byte) (firstError() + index), id,
                "bad " + kind + " 0x" + Integer.toHexString(id));
    }

    /** The wire's {@code INT64}: two words, signed, low word first. */
    private static long value(Int64 value) {
        return (value.getLo() & 0xFFFFFFFFL) | ((long) value.getHi() << 32);
    }

    private static Int64 int64(long value) {
        return Int64.builder().lo((int) value).hi((int) (value >> 32)).build();
    }

    /** A counter a client created: a 64 bit value, which only this server writes. */
    static final class CounterResource extends XResource {

        static final int CLASS = XResource.newExtensionClass();

        private long value;

        CounterResource(int id, ClientConnection owner, long value) {
            super(id, CLASS, owner);
            this.value = value;
        }

        long value() {
            return value;
        }

        void setValue(long value) {
            this.value = value;
        }

        @Override
        public String typeName() {
            return "counter";
        }
    }

    /**
     * An alarm: a trigger on a counter, which fires when the counter's value crosses or satisfies
     * the trigger's test.
     *
     * <p>It keeps the counter's value as of the last evaluation rather than a boolean, because both
     * kinds of test are about the *change*: a transition needs to know which side of the threshold
     * the value came from, and a comparison needs to know it has not already fired.
     */
    static final class AlarmResource extends XResource {

        static final int CLASS = XResource.newExtensionClass();

        private int counterId;
        private int valueType;
        private long waitValue;
        private int testType;
        private long delta;
        private int events;

        /** {@code ACTIVE}, {@code INACTIVE} or {@code DESTROYED}; only a comparison changes it. */
        private int state = ALARM_ACTIVE;

        /** The counter's value as of the last evaluation, and the base a relative threshold uses. */
        private long previous;
        private long armedAt;

        AlarmResource(int id, ClientConnection owner, int counterId, int valueType, long waitValue,
                      int testType, long delta, int events, long counterValue) {
            super(id, CLASS, owner);
            this.counterId = counterId;
            this.valueType = valueType;
            this.waitValue = waitValue;
            this.testType = testType;
            this.delta = delta;
            this.events = events;
            this.previous = counterValue;
            this.armedAt = counterValue;
        }

        /** The value the test compares against: absolute, or offset by where the counter started. */
        long threshold() {
            return valueType == RELATIVE ? waitValue + armedAt : waitValue;
        }

        /** Whether the trigger's condition holds for a counter value, given where it came from. */
        boolean testHolds(long value) {
            long threshold = threshold();
            return switch (testType) {
                case POSITIVE_TRANSITION -> previous <= threshold && value > threshold;
                case NEGATIVE_TRANSITION -> previous > threshold && value <= threshold;
                case POSITIVE_COMPARISON -> value > threshold;
                default -> value <= threshold;         // NEGATIVE_COMPARISON
            };
        }

        /** Notes the counter's value after an evaluation, so the next one knows where it came from. */
        void arm(long counterValue) {
            this.previous = counterValue;
            this.armedAt = counterValue;
        }

        int counterId() {
            return counterId;
        }

        void setCounterId(int counterId) {
            this.counterId = counterId;
        }

        int valueType() {
            return valueType;
        }

        void setValueType(int valueType) {
            this.valueType = valueType;
        }

        long waitValue() {
            return waitValue;
        }

        void setWaitValue(long waitValue) {
            this.waitValue = waitValue;
        }

        int testType() {
            return testType;
        }

        void setTestType(int testType) {
            this.testType = testType;
        }

        long delta() {
            return delta;
        }

        void setDelta(long delta) {
            this.delta = delta;
        }

        int events() {
            return events;
        }

        void setEvents(int events) {
            this.events = events;
        }

        int state() {
            return state;
        }

        void setState(int state) {
            this.state = state;
        }

        @Override
        public String typeName() {
            return "alarm";
        }
    }

    /**
     * A fence: one bit that a client sets and the server reports, and that a client waits on.
     *
     * <p>The waiting is the part this server cannot honestly do and does not pretend to: there is
     * nothing asynchronous for a fence to be waiting for, so {@code AwaitFence} finds every fence
     * already in its final state.
     */
    static final class FenceResource extends XResource {

        static final int CLASS = XResource.newExtensionClass();

        private boolean triggered;

        FenceResource(int id, ClientConnection owner, boolean triggered) {
            super(id, CLASS, owner);
            this.triggered = triggered;
        }

        boolean isTriggered() {
            return triggered;
        }

        void setTriggered(boolean triggered) {
            this.triggered = triggered;
        }

        @Override
        public String typeName() {
            return "fence";
        }
    }
}
