package cn.oyzh.fx.db.query;


import cn.oyzh.common.util.StringUtil;

/**
 * 查询词元，记录编辑器中当前光标处的文本片段及其起止位置
 *
 * @author oyzh
 * @since 2024/8/15
 */
public class DBQueryToken {

    /**
     * 结束位置
     */
    private int endIndex;

    /**
     * 开始位置
     */
    private int startIndex;

    /**
     * 内容
     */
    private String content;

    /**
     * 词元
     */
    private Character token;

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return StringUtil.isEmpty(this.content);
    }

    /**
     * 是否非空
     *
     * @return 结果
     */
    public boolean isNotEmpty() {
        return StringUtil.isNotEmpty(this.content);
    }

    /**
     * 获取结束索引。
     *
     * @return 结束索引
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * 设置结束索引。
     *
     * @param endIndex 结束索引
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * 获取开始索引。
     *
     * @return 开始索引
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * 设置开始索引。
     *
     * @param startIndex 开始索引
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * 获取内容。
     *
     * @return 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置内容。
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取令牌。
     *
     * @return 令牌
     */
    public Character getToken() {
        return token;
    }

    /**
     * 设置令牌。
     *
     * @param token 令牌
     */
    public void setToken(Character token) {
        this.token = token;
    }
}
