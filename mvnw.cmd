@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@setlocal

@set MAVEN_PROJECTBASEDIR=%~dp0

@if not "%MAVEN_OPTS%"=="" goto skipMavenOpts
@set MAVEN_OPTS=-Xmx512m
:skipMavenOpts

@set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@if exist %WRAPPER_JAR% goto runMaven

@echo Downloading Maven Wrapper...
@powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"

:runMaven
@java %MAVEN_OPTS% -jar %WRAPPER_JAR% %*
