#include <cstdlib> // 包含 system() 函数的头文件
#include <iostream>

int main() {
    // 构建 java 命令来启动 Java 应用
    // 注意：根据您的 Java 类的包和位置调整类路径和主类名
    const char* command = "/Users/oyzh/Desktop/EasyZK.app/Contents/MacOS/jre/bin/java -jar /Users/oyzh/Desktop/EasyZK.app/Contents/MacOS/easyzk-1.6.4_clip.jar &";

    // 使用 system() 函数执行 java 命令
    int result = system(command);

    // 检查 system() 的返回值，非零通常表示错误
    if (result != 0) {
        std::cerr << "Java application failed to start." << std::endl;
        return 1; // 返回错误代码
    }

    std::cout << "Java application started successfully." << std::endl;
    return 0; // 返回成功代码
}
