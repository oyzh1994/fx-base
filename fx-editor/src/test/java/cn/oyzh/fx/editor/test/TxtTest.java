package cn.oyzh.fx.editor.test;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.parser.AutoDetectParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * @author oyzh
 * @since 2025-08-01
 */
public class TxtTest {

    @Test
    public void test1() throws IOException {
        InputStream stream = ResourceUtil.getResourceAsStream("test.properties");
        Tika tika = new Tika();
        System.out.println(tika.detect(stream));
    }

    @Test
    public void test2() throws IOException {
        InputStream stream = ResourceUtil.getResourceAsStream("test.css");
        Tika tika = new Tika();
        System.out.println(tika.detect(stream));
    }

    @Test
    public void test3() throws IOException {
        InputStream stream = ResourceUtil.getResourceAsStream("test.yaml");
        Tika tika = new Tika();
        System.out.println(tika.detect(stream));
    }

    @Test
    public void test4() throws IOException {
        InputStream stream = ResourceUtil.getResourceAsStream("test.json");
        Tika tika = new Tika();
        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = new DefaultDetector();
        AutoDetectParser parser = new AutoDetectParser(config);
        tika = new Tika(detector, parser);
        System.out.println(tika.detect(new ByteArrayInputStream(IOUtil.readBytes(stream))));
        System.out.println(tika.detect(stream));
    }

    @Test
    public void test5() throws MagicMatchNotFoundException, MagicException, MagicParseException {
        InputStream stream = ResourceUtil.getResourceAsStream("test.xml");
        String bytes = IOUtil.readUtf8String(stream);
        MagicMatch match = Magic.getMagicMatch(bytes.getBytes());
        System.out.println(match.getMimeType()); // 输出: text/html
    }
}
