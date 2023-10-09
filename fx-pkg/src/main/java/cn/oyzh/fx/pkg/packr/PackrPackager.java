package cn.oyzh.fx.pkg.packr;

import com.badlogicgames.packr.Packr;
import com.badlogicgames.packr.PackrConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2023/3/8
 */
@Slf4j
public class PackrPackager extends Packr {

    @Override
    public void pack(PackrConfig config) throws IOException, CompressorException, ArchiveException {
        super.pack(config);
        log.info("outDir: {}", config.outDir);
    }
}
