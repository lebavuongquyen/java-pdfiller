@echo off
setlocal enabledelayedexpansion

:: Tính đường dẫn tuyệt đối đến thư mục gốc
set SCRIPT_DIR=%~dp0
pushd "%SCRIPT_DIR%\.."
set ROOT_DIR=%CD%
popd

:: Đường dẫn đến file JAR
set JAR_PATH=%ROOT_DIR%\target\pdffiller.jar

set MODE=%1

:: Gán biến ngoài khối IF để đảm bảo Windows xử lý đúng
set PORT=%2
if "%PORT%"=="" set PORT=8080

set TEMPLATE=%2
set OUTPUT=%3
set IMAGE=%4

:: Xử lý lệnh
if "%MODE%"=="start" (
    echo 🔥 Starting HTTP server on port %PORT%...
    java -jar "%JAR_PATH%" --server.port=%PORT%
    goto :eof
)

if "%MODE%"=="fill" (
    if "%TEMPLATE%"=="" (
        echo ❌ Missing parameters.
        echo ✅ Usage: pdffiller.bat fill template.pdf output.pdf image.png
        goto :eof
    )
    echo 🛠️ Running CLI PDF filler...
    java -cp "%JAR_PATH%" vn.quyen.cli.PdfFillerCli %TEMPLATE% %OUTPUT% %IMAGE%
    goto :eof
)

echo ❌ Unknown command: %MODE%
echo ✅ Usage:
echo     pdffiller.bat start [port]
echo     pdffiller.bat fill template.pdf output.pdf image.png