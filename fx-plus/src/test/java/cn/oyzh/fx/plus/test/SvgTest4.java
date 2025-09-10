// package cn.oyzh.fx.plus.test;
//
// import org.apache.batik.transcoder.TranscoderException;
// import org.apache.batik.transcoder.TranscoderInput;
// import org.apache.batik.transcoder.TranscoderOutput;
// import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
// import org.junit.Test;
//
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
//
// public class SvgTest4 {
//
//     @Test
//     public void test1() throws IOException, TranscoderException {
//
//         FileInputStream fis = new FileInputStream("/Users/oyzh/IdeaProjects/oyzh/fx-base/fx-plus/src/main/resources/fx-svg/add.svg");
//         SVGTranscoder transcoder = new SVGTranscoder();
// //        PNGTranscoder transcoder = new PNGTranscoder();
// //        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_WIDTH, 400);
// //        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_HEIGHT, 400);
//         TranscoderInput input = new TranscoderInput(fis);
//         FileOutputStream outputStream = new FileOutputStream("/Users/oyzh/IdeaProjects/oyzh/fx-base/fx-plus/src/main/resources/fx-svg/add-1.svg");
// //        FileOutputStream outputStream = new FileOutputStream("/Users/oyzh/IdeaProjects/oyzh/fx-base/fx-plus/src/main/resources/fx-svg/add-1.png");
//         TranscoderOutput output = new TranscoderOutput(outputStream);
//         transcoder.transcode(input, output);
//         outputStream.flush();
//         outputStream.close();
//
//     }
// }
