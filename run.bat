@echo off
setlocal enabledelayedexpansion

set "PROJECT_ROOT=%cd%"
set "BACKEND=%PROJECT_ROOT%\backend"
set "WEB=%PROJECT_ROOT%\web"
set "BUILD_WEB=false"
set "WEB_DIR=%PROJECT_ROOT%\web-output"
set "WEB_TYPE=dev"

rem Check for Java version
Set "JV="
For /F "Tokens=3" %%A In ('java -version 2^>^&1') Do If Not Defined JV Set "JV=%%~A"
If /I "!JV!"=="not" (
    echo Java is not installed
    exit /b 1
) Else (
    echo Java Version "!JV!"
)

rem Check for Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo You must have Maven installed. See the wiki for help!
    exit /b 1
)

rem Check to see if we are building for web
if "%~1"=="web" (
    set "BUILD_WEB=true"
    rem Make sure jekyll is installed
    where jekyll >nul 2>nul || (
        echo You must have Jekyll installed to build for web. See the wiki for help!
        exit /b 1
    )

    rem Make sure type was specified
    if "%~2"=="" (
        echo You must specify either 'dev' or 'production' for the web build.
        exit /b 1
    )

    set "WEB_TYPE=%~2"

    rem Make sure it's either dev or production
    if /i "!WEB_TYPE!" NEQ "dev" if /i "!WEB_TYPE!" NEQ "production" (
        echo Invalid web type specified. You must specify either 'dev' or 'production'.
        exit /b 1
    )

    rem If we are doing production, check for output folder
    if "!WEB_TYPE!"=="production" (
        rem Check if the third argument (output folder) is provided
        if "%~3"=="" (
            echo You must specify an output folder for the production build.
            exit /b 1
        ) else (
            rem Check for the output folder
            if not exist "%~3" (
                echo The output folder does not exist, creating "%~3"...
                mkdir "%~3"
            )
            set "WEB_DIR=%~3"
        )
    )
)

rem Pull all changes first
git pull

rem Build the jar file
pushd "!BACKEND!"
call mvn clean package
echo Finished building jar

rem Check if jar exists before moving
if exist "!BACKEND!\target\CobaltTester-latest.jar" (
    move "!BACKEND!\target\CobaltTester-latest.jar" "!BACKEND!"
    echo Jar file moved successfully.
) else (
    echo Jar file does not exist after Maven build.
    popd
    exit /b 1
)
popd

rem Run the jar based on if we want to generate web or not
if "!BUILD_WEB!"=="true" (
    rem Change to the backend directory to run the jar
    pushd "!BACKEND!"
    rem java -jar "CobaltTester-latest.jar" web (for testing atm)
    echo Finished running cobalt tests, building web...
    
    rem Copy instance.json for web
    copy "instances.json" "!WEB!"
    popd

    rem Change to the WEB directory
    pushd "!WEB!"
    rem Make sure we have all dependencies
    call bundle install

    rem Build or serve based on what was provided
    if "!WEB_TYPE!"=="production" (
        set "JEKYLL_ENV=production"
        bundle exec jekyll build
        popd
        rem Move static files into output folder
        del /q "%WEB_DIR%\*"
        xcopy "%WEB%\_site\*" "%WEB_DIR%\" /E /I /Y
        echo Build done! Static files saved to %WEB_DIR%
    ) else (
        bundle exec jekyll serve
    )
)
) else (
    rem If we are not building for web, just run the jar only
    pushd "!BACKEND!"
    java -jar "CobaltTester-latest.jar"
    echo Finished running cobalt tests. Results are located at !BACKEND!\instances.json
    popd
)

rem End of the script
exit /b 0