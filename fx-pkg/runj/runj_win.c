#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <windows.h>

// 获取执行指令
char* get_exec_cmd(){
    FILE *file;
    char line[1024];
    // 打开文件
    file = fopen("app.conf", "r");
    if (file == NULL) {
        perror("Error opening file");
        return NULL;
    }
    // 读取第一行内容
    if (fgets(line, sizeof(line), file) != NULL) {
        // 去除行尾的换行符（如果有的话）
        line[strcspn(line, "\n")] = 0;
        // 打印第一行内容
        printf("First line of the file: %s\n", line);
    } else {
        perror("Error reading first line of file");
    }
    // 关闭文件
    fclose(file);
    char* line1 = line;
    return line1;
}

// window的gui程序主入口
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    printf("start----------\n");
    // 获取程序执行路径
    char* execCmd = get_exec_cmd();
    printf("execCmd: %s\n", execCmd);
    // 使用CreateProcess函数启动程序
    STARTUPINFO si;
    PROCESS_INFORMATION pi;
    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));
    // 不创建新窗口
    si.wShowWindow = SW_HIDE;
    // 启动程序
    if (!CreateProcess(NULL,   // 不使用模块名
        execCmd,        // 命令行
        NULL,           // 进程句柄不可继承
        NULL,           // 线程句柄不可继承
        FALSE,          // 设置句柄不继承标志
        0,              // 无创建标志
        NULL,           // 使用父进程的环境块
        NULL,           // 使用父进程的驱动器和目录
        &si,            // 指向启动信息结构的指针
        &pi)           // 指向进程信息结构的指针
        ) {
        MessageBox(NULL, "start application fail!", "error", MB_ICONEXCLAMATION | MB_OK);
        return 1;
    }
    // 等待程序执行完成
    WaitForSingleObject(pi.hProcess, INFINITE);
    // 关闭进程和线程句柄
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    printf("exit----------\n");
    return 0;
}
