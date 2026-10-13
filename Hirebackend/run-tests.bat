@echo off
echo ============================================
echo   HireSystem Unit Test Runner
echo ============================================
echo.

cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Java\jdk-17
set MAVEN_HOME=C:\Users\20453\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [ERROR] Maven not found: %MAVEN_HOME%
    echo Edit MAVEN_HOME in this script.
    pause
    exit /b 1
)

echo Running: mvn test ...
echo.
REM 中文路径下 Mockito inline mock maker 的 agent 自附加会失败（ByteBuddy 已知问题），
REM 通过 -javaagent 预加载 byte-buddy-agent 绕过。若更换了 byte-buddy 版本请同步修改此路径。
set AGENT=%USERPROFILE%\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.9\byte-buddy-agent-1.14.9.jar
call "%MAVEN_HOME%\bin\mvn.cmd" test "-DargLine=-javaagent:%AGENT%"

echo.
if %ERRORLEVEL% EQU 0 (
    echo ============================================
    echo   [SUCCESS] All tests passed!
    echo ============================================
) else (
    echo ============================================
    echo   [FAILED] See report above for failures.
    echo ============================================
)
pause
