#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <windows.h>

// 拼接字符串的函数
char* str_concat(const char *str1, const char *str2) {
    // 计算拼接后的字符串长度
    size_t len1 = strlen(str1);
    size_t len2 = strlen(str2);
    // 加上字符串结束符 '\0'
    size_t total_len = len1 + len2 + 1;
    // 动态分配内存空间
    char *result = (char *)malloc(total_len * sizeof(char));
    if (result == NULL) {
        perror("Memory allocation failed");
        return NULL;
    }
    // 拼接字符串
    // 将 str1 复制到 result
    memcpy(result, str1, len1);
    // 将 str2（包括 '\0'）复制到 result 的尾部
    memcpy(result + len1, str2, len2 + 1);
    return result;
}

// 替换字符串的函数
char* str_replace(const char* original, const char* from, const char* to) {
    char* result;
    char* ins;
    char* tmp;
    int len_rep;
    int len_front;
    int count = 0;
    int len_to;
    int len_original;
    int i;

    // 初始化
    if (!original || !from || !to) {
        return NULL;
    }

    len_rep = strlen(from);
    len_to = strlen(to);
    len_original = strlen(original);

    // 计算原始字符串中子串出现的次数
    tmp = original;
    while ((tmp = strstr(tmp, from))) {
        count++;
        tmp += len_rep;
    }

    // 如果没有找到要替换的子串，则直接返回原始字符串的副本
    if (count == 0) {
        return strdup(original);
    }

    // 分配足够的内存给结果字符串
    // 原始字符串长度 + (替换字符串长度 - 被替换字符串长度) * 出现次数 + 结尾的'\0'
    result = (char*)malloc(len_original + (len_to - len_rep) * count + 1);
    if (!result) {
        return NULL;
    }

    // 构造替换后的字符串
    tmp = result;
    while (count--) {
        ins = strstr(original, from);
        len_front = ins - original;
        tmp = strncpy(tmp, original, len_front) + len_front;
        tmp = strcpy(tmp, to) + len_to;
        original += len_front + len_rep; // 移动到下一个可能的匹配位置
    }
    strcpy(tmp, original); // 拷贝剩余的字符串

    return result;
}

// 跨平台函数，用于去除路径的最后一部分
char* remove_last_part_of_path(const char *path) {
    if (path == NULL) {
        return NULL;
    }
    size_t len = strlen(path);
    if (len == 0) {
        // 如果路径为空，则返回空字符串
        return strdup("");
    }
    // Unix-like系统默认使用斜杠作为分隔符
    const char *separator = "/";
    // 查找最后一个斜杠
    char *last_separator = strrchr(path, '/');
    // Windows系统可能需要同时检查反斜杠
    if (!last_separator && strchr(path, '\\')) {
        separator = "\\";
        last_separator = strrchr(path, '\\');
    }
    if (last_separator == NULL) {
        // 如果没有找到分隔符，则整个字符串就是路径的最后一部分，返回空字符串
        return strdup("");
    } else if (last_separator == path) {
        // 如果分隔符是路径的第一个字符，则返回根目录（Unix的'/'或Windows的盘符+'\'）
        return strdup(separator);
    } else {
        // 分配内存来存储新的路径字符串（不包括最后一部分）
        size_t new_len = last_separator - path + 1; // 加1是为了包含分隔符本身
        char *new_path = (char*)malloc(new_len + 1); // 额外加1是为了字符串结束符'\0'
        if (new_path == NULL) {
            perror("Memory allocation failed");
            return NULL;
        }
        // 复制路径，不包括最后一部分
        strncpy(new_path, path, new_len);
        new_path[new_len] = '\0'; // 确保新字符串以null结尾
        return new_path;
    }
}

// 函数用于获取路径的最后一部分
char* get_last_part_of_path(const char* path) {
    if (path == NULL) {
        return NULL;
    }

    // 找到最后一个目录分隔符的位置
    const char* last_slash = strrchr(path, '/'); // Unix风格
    if (last_slash == NULL) {
        last_slash = strrchr(path, '\\'); // Windows风格，如果需要的话
    }

    // 如果没有找到分隔符，那么整个字符串就是路径的最后一部分
    if (last_slash == NULL) {
        return strdup(path); // 使用strdup复制整个字符串
    } else {
        // 计算最后一部分的长度
        size_t length = strlen(last_slash + 1);

        // 分配内存并复制最后一部分
        char* last_part = (char*)malloc(length + 1); // +1 是为了空字符'\0'
        if (last_part != NULL) {
            strncpy(last_part, last_slash + 1, length);
            last_part[length] = '\0'; // 确保字符串以空字符结尾
        }

        return last_part;
    }
}

// 用于获取当前执行文件的路径
char* get_current_file_path() {
    printf("get_current_file_path start.\n");
    char *path = NULL;
    path = (char*)malloc(PATH_MAX);
    if (GetModuleFileName(NULL, path, PATH_MAX) == 0) {
        free(path);
        perror("GetModuleFileName fail -2\n");
        return NULL;
    }
    printf("get_current_file_path finish, path:%s\n",path);
    return path;
}

// 用于获取当前执行文件的目录
char* get_current_file_dir() {
    // 获取文件路径
    char *fPath = get_current_file_path();
    // 获取文件目录
    char *fPath1 = remove_last_part_of_path(fPath);
    // 返回结果
    return fPath1;
}

// 获取执行路径
char* get_exec_path(char* appPath){
    return str_concat(appPath , "jre/bin/javaw.exe -jar app.jar");
}

// 获取执行指令
char* get_exec_cmd(){
    // 获取文件路径
    char *fPath = get_current_file_path();
    char *fName = get_last_part_of_path(fPath);
    char *fName1 = str_replace(fName, ".exe", ".conf");
    FILE *file;
    char line[1024];
    // 打开文件
    file = fopen(fName1, "r");
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
    printf("start----------");
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
    printf("exit----------");
    return 0;
}
