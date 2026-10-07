package cn.oyzh.fx.db.query;


import java.util.List;

/**
 * 查询词元解析器，负责解析当前词元并生成候选提示
 *
 * @author oyzh
 * @since 2025-01-21
 */
public abstract class DBQueryTokenAnalyzer<E extends DBQueryPromptItem, T extends DBQueryToken> {

    /**
     * 获取当前词元
     *
     * @param input        输入内容
     * @param currentIndex 当前索引
     * @return 当前词元
     */
    public abstract T currentToken(String input, int currentIndex);

    /**
     * 初始化提示项
     *
     * @param token   词元
     * @param minCorr 最低相关度
     * @return 提示项列表
     */
    public abstract List<E> initPrompts(T token, float minCorr);
}
