#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <limits.h>

char *concatenate(const char *str1, const char *str2) {
    // 计算拼接后的字符串长度
    size_t len1 = strlen(str1);
    size_t len2 = strlen(str2);
    size_t len = len1 + len2 + 1; // +1 为'\0'空字符预留空间

    // 分配内存
    char *result = (char *)malloc(len * sizeof(char));
    if (result == NULL) {
        fprintf(stderr, "Memory allocation failed\n");
        exit(EXIT_FAILURE);
    }

    // 复制第一个字符串
    strncpy(result, str1, len1);

    // 追加第二个字符串
    strncpy(result + len1, str2, len2);

    // 确保字符串以空字符结尾
    result[len - 1] = '\0';

    return result;
}

char execPath() {
    char path[PATH_MAX];
#ifdef _WIN32
    if (GetModuleFileName(NULL, path, sizeof(path))) {
        printf("当前文件位置: %s\n", path);
        return path;
    } else {
        printf("获取文件位置失败\n");
        return NULL;
    }
#else
    ssize_t len = readlink("/proc/self/exe", path, sizeof(path) - 1);
    if (len != -1) {
        path[len] = '\0';
        printf("当前文件位置: %s\n", path);
        return path;
    } else {
        perror("readlink");
        return NULL;
    }
#endif
    return NULL;
}

int main() {
//    pid_t pid;
//    int status;
//    pid = fork();
//    if (pid < 0) {
//        perror("fork failed");
//        exit(EXIT_FAILURE);
//    } else if (pid == 0) {
        char path[PATH_MAX];
        ssize_t len = readlink("/proc/self/exe", path, sizeof(path) - 1);
        char str1=concatenate(path,"/jre/bin/java");
        char str2=concatenate(path,"app.jar);
        printf("当前程序所在的磁盘位置: %s\n", path);
        char *const argv[] = {str1, "-jar", str2, NULL};
//        if (execvp(argv[0], (char *const *)argv) == -1) {
//            perror("execvp failed");
//            exit(EXIT_FAILURE);
//        }
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
