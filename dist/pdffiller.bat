@echo off
setlocal

:: --- Argument Parsing ---
set "MODE=%~1"
set "PORT=%~2"

:: --- Script Body ---
set "SCRIPT_DIR=%~dp0"
set "SERVER_JAR_PATH=%SCRIPT_DIR%pdffiller.jar"
set "CLI_JAR_PATH=%SCRIPT_DIR%pdffiller-jar-with-dependencies.jar"

if /i "%MODE%"=="start" (
    :: Using a robust check for empty variable that avoids encoding issues
    if "x%PORT%"=="x" set "PORT=9001"
    
    echo "🔥 Starting HTTP server on port %PORT%..."
    java -jar "%SERVER_JAR_PATH%" --server.port=%PORT%
    goto :eof
)

if /i "%MODE%"=="fill" (
    echo "🛠️ Running CLI PDF filler..."
    
    setlocal EnableDelayedExpansion
    set "all_args=%*"
    set "cli_args=!all_args:%~1 =!"
    java -jar "%CLI_JAR_PATH%" !cli_args!
    endlocal
    goto :eof
)

echo "❌ Unknown command: %MODE%"
echo "✅ Usage:"
echo "    pdffiller.bat start [port]"
echo "    pdffiller.bat fill [options]"
