/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.oyzh.fx.tty;

import cn.oyzh.fx.plus.FXConst;
import com.jediterm.terminal.model.hyperlinks.HyperlinkFilter;
import com.jediterm.terminal.model.hyperlinks.LinkInfo;
import com.jediterm.terminal.model.hyperlinks.LinkResult;
import com.jediterm.terminal.model.hyperlinks.LinkResultItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 终端超链接过滤器，识别文本行中的 URL 并生成可点击的链接。
 *
 * @author yole
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyHyperlinkFilter implements HyperlinkFilter {

    /** 匹配 URL 的正则表达式 */
    private static final Pattern URL_PATTERN = Pattern.compile("\\b(mailto:|(news|(ht|f)tp(s?))://|((?<![\\p{L}0-9_.])"
            + "(www\\.)))[-A-Za-z0-9+$&@#/%?=~_|!:,.;]*[-A-Za-z0-9+$&@#/%=~_|]");

    /**
     * 判断文本行是否可能包含 URL。
     *
     * @param line 文本行
     * @return 若为 false 表示该行不含 URL；为 true 时需使用更耗时的 {@link #URL_PATTERN} 进一步匹配
     */
    public static boolean canContainUrl(@NotNull String line) {
        return line.contains("mailto:") || line.contains("://") || line.contains("www.");
    }

    @Nullable
    @Override
    public LinkResult apply(String line) {
        if (!canContainUrl(line)) {
            return null;
        }
        int textStartOffset = 0;
        Matcher m = URL_PATTERN.matcher(line);
        List<LinkResultItem> items = null;
        while (m.find()) {
            String url = m.group();
            LinkInfo linkInfo = new LinkInfo(() -> this.openUrl(url));
            LinkResultItem item = new LinkResultItem(textStartOffset + m.start(), textStartOffset + m.end(), linkInfo);
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);
        }
        return items != null ? new LinkResult(items) : null;
    }

    /**
     * 在系统默认浏览器中打开链接。
     *
     * @param url 链接地址
     */
    private void openUrl(@NotNull String url) {
        try {
            FXConst.getHostServices().showDocument(url);
        } catch (Exception e) {
            //pass
        }
    }
}
