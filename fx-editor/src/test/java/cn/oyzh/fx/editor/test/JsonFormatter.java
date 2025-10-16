package cn.oyzh.fx.editor.test;

public class JsonFormatter {
    
    public static String removeJsonFormatting(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }
        
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            
            if (escaping) {
                // 当前字符是转义序列的一部分
                result.append(c);
                escaping = false;
                continue;
            }
            
            if (c == '\\') {
                // 开始转义序列
                result.append(c);
                escaping = true;
                continue;
            }
            
            if (c == '\"') {
                // 遇到引号
                result.append(c);
                inString = !inString; // 切换字符串状态
                continue;
            }
            
            if (inString) {
                // 在字符串内，保留所有字符
                result.append(c);
            } else {
                // 在字符串外，保留所有非空白字符和必要的结构字符
                // 只去除真正的格式空白（空格、制表符、换行、回车）
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                    result.append(c);
                }
            }
        }
        
        return result.toString();
    }
    
    // 更详细的版本，可以打印处理过程
    public static String removeJsonFormattingVerbose(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }
        
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            
            if (escaping) {
                result.append(c);
                escaping = false;
                System.out.println("转义字符: '" + c + "' -> 添加到结果");
                continue;
            }
            
            if (c == '\\') {
                result.append(c);
                escaping = true;
                System.out.println("遇到转义符 -> 设置escaping=true");
                continue;
            }
            
            if (c == '\"') {
                result.append(c);
                inString = !inString;
                System.out.println("遇到引号 -> inString=" + inString + ", 添加到结果");
                continue;
            }
            
            if (inString) {
                result.append(c);
                System.out.println("字符串内: '" + c + "' -> 添加到结果");
            } else {
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    System.out.println("结构空白: '" + c + "' -> 跳过");
                } else {
                    result.append(c);
                    System.out.println("结构字符: '" + c + "' -> 添加到结果");
                }
            }
        }
        
        return result.toString();
    }
    
    // 测试方法
    public static void main(String[] args) {
        // 测试用例
        String json = "{\n" +
                     "  \"name\": \"John\\nDoe\",\n" +
                     "  \"age\": 30,\n" +
                     "  \"address\": \"123\\nMain St\\nCity\",\n" +
                     "  \"array\": [\n" +
                     "    1,\n" +
                     "    2,\n" +
                     "    3\n" +
                     "  ],\n" +
                     "  \"nested\": {\n" +
                     "    \"field\": \"value\"\n" +
                     "  }\n" +
                     "}";
        
        System.out.println("原始JSON:");
        System.out.println(json);
        
        String formatted = removeJsonFormatting(json);
        System.out.println("\n处理后的JSON:");
        System.out.println(formatted);
        
        // 验证结果
        System.out.println("\n验证:");
        System.out.println("包含 { : " + formatted.contains("{"));
        System.out.println("包含 } : " + formatted.contains("}"));
        System.out.println("包含 [ : " + formatted.contains("["));
        System.out.println("包含 ] : " + formatted.contains("]"));
        System.out.println("包含 , : " + formatted.contains(","));
        System.out.println("包含 : : " + formatted.contains(":"));
        System.out.println("保留字符串换行: " + formatted.contains("\\n"));
        
        // 使用详细版本处理一个小例子
        System.out.println("\n=== 详细处理过程 ===");
        String simpleJson = "{\n  \"test\": \"value\"\n}";
        String simpleResult = removeJsonFormattingVerbose(simpleJson);
        System.out.println("最终结果: " + simpleResult);
    }
}