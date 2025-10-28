package cn.oyzh.fx.editor.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

/**
 *
 * @author oyzh
 * @since 2025-10-16
 */
public class JsonTest {

    @Test
    public void test1() {
        String json = """
                {
                	"logicDB":"qa_db",
                	"master":"qa_db",
                	"weight":"30",
                	"slaves":[
                		{
                			"slave":"qa_db_bak",
                			"slaveMode":"readonly",
                			"weight":"70"
                		}
                	],
                }
                """;

        String json1 = JSON.toJSONString(json);
        String json2 = json1.replaceAll("\\s+", "");

        char[] chars = json1.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char aChar : chars) {
            if (aChar != '\n' && aChar != '\r' && aChar != '\t') {
                builder.append(aChar);
            }
        }
        String json3 = builder.toString();

        // System.out.println(json);
        // System.out.println(json1);
        // System.out.println(json2);
        System.out.println(json3);
        System.out.println(json3.contains("\n"));
        System.out.println(json3.contains("\r"));
        System.out.println(json3.contains("\t"));
        // System.out.println(json.contains("\n"));
        // System.out.println(json.contains("\r"));
        // System.out.println(json.contains("\t"));
    }
}
