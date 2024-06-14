#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>
#include <limits.h>
int main() {
    pid_t pid;
    int status;
    pid = fork();
    if (pid < 0) {
        perror("fork failed");
        exit(EXIT_FAILURE);
    } else if (pid == 0) {
        char path[PATH_MAX];
        ssize_t len = readlink("/proc/self/exe", path, sizeof(path) - 1);
        printf("当前程序所在的磁盘位置: %s\n", path);

        char *const argv[] = {path+"/Resources/jre/bin/java", "-jar", "/Resources/app.jar", NULL};
        if (execvp(argv[0], (char *const *)argv) == -1) {
            perror("execvp failed");
            exit(EXIT_FAILURE);
        }
    } else {
        if (waitpid(pid, &status, 0) == -1) {
            perror("waitpid failed");
            exit(EXIT_FAILURE);
        }
        if (WIFEXITED(status)) {
            printf("Child process exited with code: %d\n", WEXITSTATUS(status));
        } else if (WIFSIGNALED(status)) {
            printf("Child process was terminated by signal: %d\n", WTERMSIG(status));
        }
    }
    return 0;
}
