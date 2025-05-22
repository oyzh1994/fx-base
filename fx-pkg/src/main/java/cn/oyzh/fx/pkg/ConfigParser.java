package cn.oyzh.fx.pkg;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.IOUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * 配置解析器
 *
 * @author oyzh
 * @since 2024/6/14
 */
public interface ConfigParser<C> {

    /**
     * 解析配置
     *
     * @param configFile 配置文件
     * @return 配置类
     */
    default C parse(String configFile) {
        String extName = FileNameUtil.extName(configFile);
        JSONObject object = null;
        if (FileNameUtil.isJsonType(extName)) {
            object = JSONObject.parse(FileUtil.readUtf8String(configFile));
        } else if (FileNameUtil.isYamlType(extName)) {
            Yaml yaml = new Yaml();
            InputStream in = cn.oyzh.common.file.FileUtil.getInputStream(configFile);
            Map<String, Object> yamlData = yaml.load(in);
            IOUtil.close(in);
            object = JSONObject.parseObject(JSON.toJSONString(yamlData));
        }
        return this.parse(object);
    }

    /**
     * 解析配置
     *
     * @param object 配置内容
     * @return 配置类
     */
    C parse(JSONObject object);
}
