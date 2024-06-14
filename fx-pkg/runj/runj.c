#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <limits.h>
#include <string.h>

#ifdef __APPLE__
#include <mach-o/dyld.h> // 包含 _NSGetExecutablePath
#endif

// 拼接字符串的函数
char* concatenate_strings(const char *str1, const char *str2) {
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

// 跨平台函数，用于去除路径的最后一部分
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

// 跨平台函数，用于获取当前执行文件的路径
char* get_current_file_path() {
      printf("开始获取路径\n");
    char *path = NULL;
    size_t path_size = 0;

#ifdef _WIN32
    // Windows平台使用GetModuleFileName函数
    path_size = GetModuleFileName(NULL, NULL, 0);
    if (path_size == 0) {
        perror("GetModuleFileName失败");
        return NULL;
    }
    path = (char*)malloc(path_size);
    if (GetModuleFileName(NULL, path, path_size) == 0) {
        free(path);
        perror("GetModuleFileName失败");
        return NULL;
    }
#elif defined(__APPLE__)
 printf("_MACOS\n");
    path = (char*)malloc(PATH_MAX);
    uint32_t size = sizeof(path);
    if (_NSGetExecutablePath(path, &size) == 0) {

    } else {
        perror("_NSGetExecutablePath failed");
        return NULL;
    }
#else
    // Unix-like平台使用readlink函数读取/proc/self/exe
    path = (char*)malloc(PATH_MAX);
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
#endif
  printf("结束获取路径\n");
    return path;
}

// 跨平台函数，用于获取当前执行文件的路径
char* get_current_file_path1() {
    // 获取__FILE__宏展示的字符串
    const char* file = __FILE__;

    // 获取目录分隔符的位置
    int len = strlen(file);
    int i;
    for (i = len - 1; i >= 0; i--) {
        if (file[i] == '/') {
            break;
        }
    }

    return file;
}


// std::string GetFullExecutionFilePath() noexcept {
//     #if defined(_WIN32)
//         char exe[8096]; /* MAX_PATH */
//         GetModuleFileNameA(NULL, exe, sizeof(exe));
//         return exe;
//     #elif defined(_MACOS)
//         char path[PATH_MAX];
//         uint32_t size = sizeof(path);
//         if (_NSGetExecutablePath(path, &size) == 0) {
//             return path;
//         }
//     #if defined(PROC_PIDPATHINFO_MAXSIZE)
//         char pathbuf[PROC_PIDPATHINFO_MAXSIZE];
//         proc_pidpath(getpid(), pathbuf, sizeof(pathbuf));
//         return pathbuf;
//     #else
//          return "";
//     #endif
//     #else
//         char sz[PATH_MAX + 1];
//         int dw = readlink("/proc/self/exe", sz, PATH_MAX);
//         sz[dw] = '\x0';
//         return dw < 1 ? "" : sz;
//     #endif
// }

char* get_exec_path(basePath){
    #ifdef __linux__
        return concatenate_strings(basePath,"./jre/bin/java");
    #elif defined(__APPLE__) && defined(__MACH__)
        return concatenate_strings(basePath,"../Resources/jre/bin/java");
    #else
        printf("Unknown operating system\n");
    #endif
}

char* get_app_path(basePath){
    #ifdef __linux__
        return concatenate_strings(basePath,"app.jar");
    #elif defined(__APPLE__) && defined(__MACH__)
        return concatenate_strings(basePath,"../app.jar");
    #else
        printf("Unknown operating system\n");
    #endif
}

int main() {
//    pid_t pid;
//    int status;
//    pid = fork();
//    if (pid < 0) {
//        perror("fork failed");
//        exit(EXIT_FAILURE);
//    } else if (pid == 0) {
     printf("程序开始执行\n");
        // const char *path = get_current_file_path1();
    //    char *path1= get_current_file_path1(path);
        char *path1= get_current_file_path();
    // 调用拼接字符串函数
        char* execPath = get_exec_path(path1);
        char* appApth = get_app_path(path1);
        printf("当前程序所在的磁盘位置: %s\n", path1);
        printf("jdk路径: %s\n", execPath);
        printf("程序路径: %s\n", appApth);
        char *const argv[] = {execPath, "-jar", appApth, NULL};
        if (execvp(argv[0], (char *const *)argv) == -1) {
            perror("execvp failed");
            exit(EXIT_FAILURE);
        }
//    } else {
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
