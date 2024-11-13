package cn.oyzh.fx.pkg.packr;

import cn.oyzh.common.log.JulLog;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2023/3/8
 */
//@Slf4j
public class PackrPackager extends Packr {

    @Override
    public void pack(PackrConfig config) throws IOException, CompressorException, ArchiveException {
        super.pack(config);
        JulLog.info("outDir: {}", config.outDir);
    }
}
