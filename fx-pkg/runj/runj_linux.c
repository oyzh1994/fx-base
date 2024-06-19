#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <limits.h>
#include <string.h>
#include <sys/wait.h>

// 获取当前执行文件的路径
char* get_current_file_path() {
    printf("开始获取路径\n");
    size_t path_size = 0;
    // Unix-like平台使用readlink函数读取/proc/self/exe
    char *path = (char*)malloc(PATH_MAX);
    if (path == NULL) {
        perror("内存分配失败");
        return NULL;
    }
    ssize_t len = readlink("/proc/self/exe", path, PATH_MAX - 1);
    if (len == -1) {
        free(path);
        perror("readlink失败");
        return NULL;
    }
    path[len] = '\0'; // 确保路径字符串以null结尾
    return path;
}

// 拼接字符串的函数
char* str_concat(const char *str1, const char *str2) {
    // 计算拼接后的字符串长度
    size_t len1 = strlen(str1);
    size_t len2 = strlen(str2);
    size_t total_len = len1 + len2 + 1; // 加上字符串结束符 '\0'
    // 动态分配内存空间
    char *result = (char *)malloc(total_len * sizeof(char));
    if (result == NULL) {
        perror("Memory allocation failed");
        return NULL;
    }
    // 拼接字符串
    memcpy(result, str1, len1); // 将 str1 复制到 result
    memcpy(result + len1, str2, len2 + 1); // 将 str2（包括 '\0'）复制到 result 的尾部
    return result;
}

// 去除路径的最后一部分
char* remove_last_part_of_path(const char *path) {
    if (path == NULL) {
        return NULL;
    }
    size_t len = strlen(path);
    if (len == 0) {
        return strdup(""); // 如果路径为空，则返回空字符串
    }
    const char *separator = "/"; // Unix-like系统默认使用斜杠作为分隔符
    char *last_separator = strrchr(path, '/'); // 查找最后一个斜杠
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
    // 获取当前启动文件
    char* path = get_current_file_path();
    // 获取当前启动目录
    char* path1 = remove_last_part_of_path(path);
    // 获取执行路径
    char* line2 = str_concat(path1 , line1);
    // 返回执行路径
    return line2;
}

// 程序主入口
int main() {
    printf("start----------\n");
    // 获取程序执行路径
    char* execCmd = get_exec_cmd();
    printf("execCmd: %s\n", execCmd);
    // 使用system启动
    if (system(execCmd) == -1) {
        printf("exit----------\n");
        return -1;
    }
    printf("exit----------\n");
    return 0;
}
