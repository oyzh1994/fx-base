// package cn.oyzh.fx.editor;
//
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
// import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
// import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
// import org.fife.ui.rsyntaxtextarea.Token;
// import org.fife.ui.rsyntaxtextarea.TokenImpl;
// import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
// import org.fife.ui.rsyntaxtextarea.TokenMap;
// import org.fife.ui.rsyntaxtextarea.TokenTypes;
//
// import javax.swing.text.Segment;
// import java.util.Collection;
// import java.util.HashSet;
// import java.util.Set;
//
// /**
//  * @author oyzh
//  * @since 2025-08-05
//  */
// public class EditorPromptTokenMaker extends AbstractTokenMaker {
//
//     public EditorPromptTokenMaker(){
//         System.out.println("---------------------");
//     }
//
//     private final Set<String> keywords = new HashSet<>();
//
//     public Set<String> getKeywords() {
//         return this.keywords;
//     }
//
//     public void setKeywords(Set<String> keywords) {
//         this.keywords.clear();
//         if (CollectionUtil.isNotEmpty(keywords)) {
//             this.keywords.addAll(keywords);
//         }
//     }
//
//     @Override
//     public TokenMap getWordsToHighlight() {
//         TokenMap tokenMap = new TokenMap(10, true);
//         tokenMap.put("Google", TokenTypes.IDENTIFIER);
//         tokenMap.put("Analytics", TokenTypes.IDENTIFIER);
//         return tokenMap;
//     }
//
//     private static final String[] KEYWORDS = {"Google", "Analytics"};
//
//
//     @Override
//     public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
//         resetTokenList();
//         char[] chars = text.array;
//         int offset = text.offset;
//         int count = text.count;
//         int end = offset + count;
//         // 边界检查 - 防止数组越界
//         if (end > chars.length) {
//             end = chars.length;
//         }
//         int tokenStart = offset;
//         int currentType = initialTokenType;
//         for (int i = offset; i < end; i++) {
//             char c = chars[i];
//
//             switch (currentType) {
//                 case Token.NULL:
//                     tokenStart = i;
//                     if (isWhitespace(c)) {
//                         currentType = Token.WHITESPACE;
//                     } else if (isLetter(c)) {
//                         currentType = Token.IDENTIFIER;
//                     } else {
//                         currentType = Token.OPERATOR;
//                     }
//                     break;
//
//                 case Token.WHITESPACE:
//                     if (!isWhitespace(c)) {
//                         addToken(text, tokenStart, i - 1, Token.WHITESPACE, startOffset + tokenStart - offset);
//                         tokenStart = i;
//                         currentType = Token.NULL;
//                         i--; // 重新检查当前字符
//                     }
//                     break;
//
//                 case Token.IDENTIFIER:
//                     if (!isIdentifierChar(c) || (i - tokenStart) > 2000) {
//                         String word = safeSubstring(chars, tokenStart, i);
//                         int tokenType = isKeyword(word) ? Token.RESERVED_WORD : Token.IDENTIFIER;
//
//                         addToken(text, tokenStart, i - 1, tokenType, startOffset + tokenStart - offset);
//                         tokenStart = i;
//                         currentType = Token.NULL;
//                         i--; // 重新检查当前字符
//                     }
//                     break;
//
//                 case Token.OPERATOR:
//                     if (i + 1 < end && isOperatorChar(chars[i + 1])) {
//                         // 处理多字符操作符 (如 ==, !=)
//                         continue;
//                     }
//                     addToken(text, tokenStart, i, Token.OPERATOR, startOffset + tokenStart - offset);
//                     tokenStart = i + 1;
//                     currentType = Token.NULL;
//                     break;
//             }
//         }
//
//         // 安全处理最后一个token
//         if (currentType != Token.NULL && tokenStart < end) {
//             int endPos = end - 1;
//             if (currentType == Token.IDENTIFIER) {
//                 String word = safeSubstring(chars, tokenStart, end);
//                 currentType = isKeyword(word) ? Token.RESERVED_WORD : Token.IDENTIFIER;
//             }
//             // 确保不越界
//             endPos = Math.min(endPos, chars.length - 1);
//             addToken(text, tokenStart, endPos, currentType, startOffset + tokenStart - offset);
//         }
//
//         addNullToken();
//         return firstToken;
//     }
//
//     // 安全截取字符串，防止索引越界
//     private String safeSubstring(char[] chars, int start, int end) {
//         if (start < 0 || end > chars.length || start >= end) {
//             return "";
//         }
//         return new String(chars, start, Math.min(end - start, 2000));
//     }
//
//     private boolean isWhitespace(char c) {
//         return c == ' ' || c == '\t' || c == '\n' || c == '\r';
//     }
//
//     private boolean isLetter(char c) {
//         return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
//     }
//
//     private boolean isIdentifierChar(char c) {
//         return isLetter(c) || (c >= '0' && c <= '9');
//     }
//
//     private boolean isOperatorChar(char c) {
//         return "=!<>+-*/&|".indexOf(c) >= 0;
//     }
//
//     private boolean isKeyword(String word) {
//         for (String keyword : KEYWORDS) {
//             if (keyword.equalsIgnoreCase(word)) {
//                 return true;
//             }
//
//         }
//         return false;
//     }
//
//     public static final String SYNTAX_NAME = "text/prompt";
//
//     public static void register() {
//         if (TokenMakerFactory.getDefaultInstance() instanceof AbstractTokenMakerFactory factory) {
//             if (!factory.keySet().contains(SYNTAX_NAME)) {
//                 factory.putMapping(SYNTAX_NAME, EditorPromptTokenMaker.class.getName());
//             }
//         }
//     }
// }
