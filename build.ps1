param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot
try {
    Write-Host "==> Cleaning build directory" -ForegroundColor Cyan
    if (Test-Path build) { Remove-Item -Recurse -Force build }
    New-Item -ItemType Directory -Force build\classes, build\test-classes, dist | Out-Null

    Write-Host "==> Compiling application" -ForegroundColor Cyan
    $mainSources = @(Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object { $_.FullName })
    if ($mainSources.Count -eq 0) { throw "Không tìm thấy source Java" }
    & javac -encoding UTF-8 --release 17 -d build\classes $mainSources
    if ($LASTEXITCODE -ne 0) { throw "javac thất bại" }

    if (Test-Path src\main\resources) {
        Copy-Item -Path "src\main\resources\*" -Destination build\classes -Recurse -Force
    }

    Write-Host "==> Creating dist\SlangDictionary.jar" -ForegroundColor Cyan
    $jarPath = Join-Path $PSScriptRoot "dist\SlangDictionary.jar"
    if (Test-Path $jarPath) { Remove-Item -Force $jarPath }
    & jar --create --file $jarPath --main-class vn.edu.hcmus.slang.App -C build\classes .
    if ($LASTEXITCODE -ne 0) { throw "Đóng gói jar thất bại" }

    if (-not $SkipTests) {
        Write-Host "==> Compiling tests" -ForegroundColor Cyan
        $testSources = @(Get-ChildItem -Recurse src\test\java -Filter *.java | ForEach-Object { $_.FullName })
        if ($testSources.Count -gt 0) {
            & javac -encoding UTF-8 --release 17 -cp build\classes -d build\test-classes $testSources
            if ($LASTEXITCODE -ne 0) { throw "javac (tests) thất bại" }

            Write-Host "==> Running tests" -ForegroundColor Cyan
            & java -Dfile.encoding=UTF-8 -cp "build\classes;build\test-classes" vn.edu.hcmus.slang.AllTests
            if ($LASTEXITCODE -ne 0) { throw "Kiểm thử thất bại" }
        }
    }

    Write-Host ""
    Write-Host "BUILD OK -> dist\SlangDictionary.jar" -ForegroundColor Green
    Write-Host "Chạy: run.bat (hoặc java -jar dist\SlangDictionary.jar)" -ForegroundColor Green
} finally {
    Pop-Location
}
