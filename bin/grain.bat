@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Grain based websites startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto checkGrainHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:checkGrainHome
if not "%GRAIN_HOME%" == "" goto findGrainFromGrainHome

echo.
echo ERROR: GRAIN_HOME is not set
echo.
echo Please set the GRAIN_HOME variable in your environment to match the
echo location of your Grain installation.

goto fail

:findGrainFromGrainHome
set GRAIN_JAR=%GRAIN_HOME%/dist/grain-standalone-1.0.jar

if exist "%GRAIN_JAR%" goto init

echo.
echo ERROR: GRAIN_HOME is set to an invalid directory: %GRAIN_HOME%
echo.
echo Please set the GRAIN_HOME variable in your environment to match the
echo location of your Grain installation.

goto fail

:init
@rem Get command-line arguments, handling Windowz variants

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

:execute
@rem Setup the command line

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -jar "%GRAIN_JAR%" %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
pause
exit 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal
pause

:omega
