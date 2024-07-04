package cn.oyzh.fx.plus.controls.file;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.plus.controls.textfield.LimitTextField;

import java.io.File;

/**
 * 文件选择框
 *
 * @author oyzh
 * @since 2024/07/04
 */
public class FileTextField extends LimitTextField {

    {
        this.setEditable(false);
        this.setSkin(new FileTextFieldSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public FileTextFieldSkin skin() {
        return (FileTextFieldSkin) this.getSkin();
    }

    /**
     * 数据
     */
    private byte[] data;

    public byte[] getData() {
        File file = this.skin().file;
        if (file == null) {
            data = new byte[]{};
        } else {
            data = FileUtil.readBytes(file);
        }
        return data;
    }

    public void setData(Object val) {
        if (val instanceof byte[] bytes) {
            this.data = bytes;
        } else if (val instanceof Byte[] bytes) {
            this.data = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                this.data[i] = bytes[i];
            }
        }
    }
}
