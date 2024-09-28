package cn.oyzh.fx.common.util;

import jakarta.annotation.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFile extends Properties {

    public PropertiesFile() {
        super();
    }

    public PropertiesFile(String fileName) throws IOException {
        super();
        InputStream stream = ResourceUtil.getResourceAsStream(fileName);
        this.load(stream);
    }


}
