package cn.oyzh.fx.common.date;

import cn.oyzh.fx.common.SysConst;
import cn.oyzh.fx.common.util.FileUtil;
import cn.oyzh.fx.common.util.ReflectUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author oyzh
 * @since 2024-09-27
 */
public class LocalZoneRulesProvider extends ZoneRulesProvider {

    private String versionId;

    private List<String> regionIds;

    public LocalZoneRulesProvider() {
        try {
            this.doLoad();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Set<String> provideZoneIds() {
        return new HashSet<>(this.regionIds);
    }

    @Override
    protected ZoneRules provideRules(String zoneId, boolean forCaching) {
        if (!this.regionIds.contains(zoneId)) {
            throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
        }
        try {
            return this.readCache(zoneId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
        TreeMap<String, ZoneRules> map = new TreeMap<>();
        ZoneRules rules = getRules(zoneId, false);
        if (rules != null) {
            map.put(this.versionId, rules);
        }
        return map;
    }

    private void doLoad() throws Exception {
        String libDir = System.getProperty("java.home") + File.separator + "lib";
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(libDir, "tzdb.dat"))));
        try (dis) {
            if (dis.readByte() != 1) {
                throw new StreamCorruptedException("File format not recognised");
            }
            // group
            String groupId = dis.readUTF();
            if (!"TZDB".equals(groupId)) {
                throw new StreamCorruptedException("File format not recognised");
            }
            // versions
            int versionCount = dis.readShort();
            for (int i = 0; i < versionCount; i++) {
                this.versionId = dis.readUTF().intern();
            }
            // regions
            int regionCount = dis.readShort();
            String[] regionArray = new String[regionCount];
            for (int i = 0; i < regionCount; i++) {
                regionArray[i] = dis.readUTF().intern();
            }
            this.regionIds = Arrays.asList(regionArray);
            // rules
            int ruleCount = dis.readShort();
            Object[] ruleArray = new Object[ruleCount];
            for (int i = 0; i < ruleCount; i++) {
                byte[] bytes = new byte[dis.readShort()];
                dis.readFully(bytes);
                ruleArray[i] = bytes;
            }
            // link version-region-rules
            for (int i = 0; i < versionCount; i++) {
                int versionRegionCount = dis.readShort();
                for (int j = 0; j < versionRegionCount; j++) {
                    String regionId = regionArray[dis.readShort()];
                    Object rule = ruleArray[dis.readShort() & 0xffff];
                    if (rule instanceof byte[] bytes) {
                        ZoneRules rules = this.readRules(bytes);
                        this.doCache(regionId,rules);
                    }
                }
            }
        }
    }

    private void doCache(String zoneId, ZoneRules rules) throws IOException {
        String cacheDir = System.getProperty(SysConst.CACHE_DIR);
        File file = new File(cacheDir, zoneId);
        FileUtil.touch(file);
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
        try (os) {
            os.writeObject(rules);
        }
    }

    private ZoneRules readRules(byte[] bytes) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        Class<?> clazz = Class.forName("java.time.zone.Ser");
        Method method = ReflectUtil.getMethod(clazz, "read", DataInput.class);
        method.setAccessible(true);
        return (ZoneRules) method.invoke(null, dis);
    }

    private ZoneRules readCache(String zoneId) throws IOException, ClassNotFoundException {
        String cacheDir = System.getProperty(SysConst.CACHE_DIR);
        File file = new File(cacheDir, zoneId);
        if (!file.exists()) {
            return null;
        }
        ZoneRules rules;
        ObjectInputStream os = new ObjectInputStream(new FileInputStream(file));
        try (os) {
            rules = (ZoneRules) os.readObject();
        }
        return rules;
    }

    @Override
    public String toString() {
        return "Local[" + versionId + "]";
    }
}
