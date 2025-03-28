//package cn.oyzh.fx.plus.validator;
//
//import lombok.Getter;
//import lombok.Setter;
//
///**
// * 最大长度检验机
// *
// * @author oyzh
// * @since 2023/1/29
// */
//public class MaxLenVerifier extends BaseVerifier {
//
//    @Setter
//    @Getter
//    private Long maxLen;
//
//    @Getter
//    @Setter
//    private int order = 2;
//
//    @Setter
//    private String failMsg = "内容长度不能大于";
//
//    public MaxLenVerifier(long maxLen) {
//        this.maxLen = maxLen;
//    }
//
//    @Override
//    public boolean doVerify(Object obj) {
//        if (this.maxLen != null && obj != null) {
//            if (obj instanceof String str) {
//                return str.length() <= this.maxLen;
//            }
//        }
//        return true;
//    }
//
//    public String getFailMsg() {
//        return this.failMsg + this.maxLen;
//    }
//}
