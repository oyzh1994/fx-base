#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <limits.h>
#include <string.h>
#include <errno.h>
#include <libproc.h>

// 获取当前执行文件的路径
char* get_current_file_path() {
    printf("开始获取路径\n");
    pid_t pid = getpid();
    char path[PROC_PIDPATHINFO_MAXSIZE];
    int ret = proc_pidpath(pid, path, sizeof(path));
    if (ret <= 0) {
        fprintf(stderr, "Error retrieving process path: %s\n", strerror(errno));
        return NULL;
    }
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
char* get_exec_cmd(char* basePath){
    FILE *file;
    char line[1024];
    char* filePath=str_concat(basePath,"app.conf");
     printf("filePath----------%s\n",filePath);
    // 打开文件
    file = fopen(filePath, "r");
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
    // // 获取当前启动文件
    // char* path = get_current_file_path();
    // // 获取当前启动目录
    // char* path1 = remove_last_part_of_path(path);
    // // 获取执行路径
    // char* line2 = str_concat(path1 , line1);
    // // 返回执行路径
    // return line2;

    //  char* line2 = str_concat("/usr/bin/open -a ",line1);;
    return line1;
}

// // 程序主入口
// int main() {
//     printf("start----------\n");
//     printf("开始获取路径\n");
//     pid_t pid = getpid();
//     char path[PROC_PIDPATHINFO_MAXSIZE];
//     int ret = proc_pidpath(pid, path, sizeof(path));
//       printf("start1----------\n");
//     if (ret <= 0) {
//         fprintf(stderr, "Error retrieving process path: %s\n", strerror(errno));
//         return NULL;
//     }
//      printf("start2----------\n");
//       // // 获取当前启动文件
//     // char* path2 = get_current_file_path();
//     // 获取当前启动目录
//     char* path3 = remove_last_part_of_path(path);
//      printf("start3----------\n");
//      // 获取程序执行路径
//     char* execCmd = get_exec_cmd(path3);
//       printf("start4----------\n");

//     // 获取执行路径
//     char* line2 = str_concat(path3 , execCmd);
//     printf("execCmd: %s\n", line2);
//     // 使用system启动
//     if (system(execCmd) == -1) {
//         printf("exit----------\n");
//         return -1;
//     }
//     printf("exit----------\n");
//     return 0;
// }

// 程序主入口
int main() {
//     pid_t pid;
//    int status;
//    pid = fork();
//    if (pid < 0) {
//        perror("fork failed");
//        exit(EXIT_FAILURE);
//    } else if (pid == 0) {
        printf("start----------\n");
        printf("开始获取路径\n");
        pid_t pid = getpid();
        char path[PROC_PIDPATHINFO_MAXSIZE];
        int ret = proc_pidpath(pid, path, sizeof(path));
        printf("start1----------\n");
        if (ret <= 0) {
            fprintf(stderr, "Error retrieving process path: %s\n", strerror(errno));
            return NULL;
        }
        printf("start2----------\n");
        // // 获取当前启动文件
        // char* path2 = get_current_file_path();
        // 获取当前启动目录
        char* path3 = remove_last_part_of_path(path);
        printf("start3----------\n");
        // 获取程序执行路径
        char* execCmd = get_exec_cmd(path3);
        printf("start4----------\n");

     char* path4 = str_concat("/usr/bin/open -a ",path3);;
        // 获取执行路径
        char* line2 = str_concat(path4 , execCmd);
        printf("execCmd: %s\n", line2);
         FILE *fp;
        char path5[1035];

        fp = popen(execCmd, "r");
        if (fp == NULL) {
            printf("Execution failed\n");
            return 1;
        }

        // 读取输出直到结束
        while (fgets(path5, sizeof(path5)-1, fp) != NULL) {
            printf("%s", path5);
        }

        pclose(fp);
        //  int result = system(execCmd);
        //  sleep(20);
        // 使用system启动
        // if ( result== -1) {
        //     printf("exit1----------\n");
        //     return EXIT_FAILURE;
        // } else if (WIFEXITED(result)) {
        //     // java 命令执行完成，获取退出状态
        //     int exitStatus = WEXITSTATUS(result);
        //     printf("Java program exited with status %d\n", exitStatus);
        // } else if (WIFSIGNALED(result)) {
        //     // java 命令被信号终止
        //     int termSignal = WTERMSIG(result);
        //     printf("Java program was terminated by signal %d\n", termSignal);
        // }
getchar();

        printf("exit2----------\n");
//     } else {
//        if (waitpid(pid, &status, 0) == -1) {
//            perror("waitpid failed");
//            exit(EXIT_FAILURE);
//        }
//        if (WIFEXITED(status)) {
//            printf("Child process exited with code: %d\n", WEXITSTATUS(status));
//        } else if (WIFSIGNALED(status)) {
//            printf("Child process was terminated by signal: %d\n", WTERMSIG(status));
//        }
//    }
    return 0;
}
