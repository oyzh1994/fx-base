// package cn.oyzh.fx.plus.test.webview;
//
// import cn.oyzh.fx.plus.controller.StageController;
// import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
// import cn.oyzh.fx.plus.controls.web.WebViewPane;
// import cn.oyzh.fx.plus.window.StageAttribute;
// import javafx.fxml.FXML;
// import javafx.stage.Modality;
//
// import java.io.IOException;
// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.time.Duration;
//
// /**
//  * @author oyzh
//  * @since 2023/11/21
//  */
// @StageAttribute(
//         title = "webview测试",
//         modality = Modality.WINDOW_MODAL,
//         value = "/webview/test.fxml"
// )
// public class WebViewTestController extends StageController {
//
//     @FXML
//     private WebViewPane control;
//
//     @FXML
//     private ClearableTextField url;
//
//     @FXML
//     private void test1() {
//         this.control.loadUrl(url.getText());
//     }
//
//     @FXML
//     private void test2() {
//         String cookie = this.control.cookie();
//         System.out.println(cookie);
//     }
//
//     @FXML
//     private void test3() throws IOException, InterruptedException {
//         HttpClient client = HttpClient.newBuilder()
//                 .connectTimeout(Duration.ofSeconds(10))
//                 .followRedirects(HttpClient.Redirect.NORMAL)
//                 .build();
//
//         String pageUrl = this.control.pageUrl();
//
//         System.out.println(pageUrl);
//         URI uri = URI.create(pageUrl);
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(uri)
//                 .header("Cookie", this.control.cookie())
//                 .GET().build();
//
//         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//         // 输出响应的状态码和响应体
//         System.out.println("响应状态码：" + response.statusCode());
//         System.out.println("响应体：" + response.body());
//     }
// }
