@echo off
echo "🚀 Creating distribution package for Windows..."

:: Go to project root from script location
pushd "%~dp0\.."
set ROOT_DIR=%CD%

echo "Running Maven build..."
call mvn clean package
if %errorlevel% neq 0 (
    echo "❌ Maven build failed!"
    popd
    exit /b 1
)

echo "Creating dist directory..."
set DIST_DIR=%ROOT_DIR%\dist
if exist "%DIST_DIR%" (
    rmdir /s /q "%DIST_DIR%"
)
mkdir "%DIST_DIR%"

echo "Copying artifacts..."
copy "%ROOT_DIR%\target\pdffiller.jar" "%DIST_DIR%\"
copy "%ROOT_DIR%\target\pdffiller-jar-with-dependencies.jar" "%DIST_DIR%\"
copy "%ROOT_DIR%\shell\pdffiller.dist.bat" "%DIST_DIR%\pdffiller.bat"
copy "%ROOT_DIR%\shell\pdffiller.dist.sh" "%DIST_DIR%\pdffiller.sh"
copy "%ROOT_DIR%\README.md" "%DIST_DIR%\"

echo "✅ Distribution package created in 'dist' folder."
echo "To run the application, navigate to the 'dist' folder and use:"
echo "pdffiller.bat start"
echo "or"
echo "pdffiller.bat fill ..."

popd