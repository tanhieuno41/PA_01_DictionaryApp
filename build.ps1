param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot

function Resolve-JdkTool {
    param([Parameter(Mandatory = $true)][string]$Name)
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }

    $javaHome = $null
    try {
        $javaHome = (& java -XshowSettings:properties -version 2>&1 |
            Where-Object { $_ -match 'java\.home\s*=\s*(.+)$' } |
            ForEach-Object { $matches[1].Trim() } |
            Select-Object -First 1)
    } catch {
    }
    if (-not $javaHome -and $env:JAVA_HOME) { $javaHome = $env:JAVA_HOME }
    if ($javaHome) {
        $candidate = Join-Path $javaHome "bin\$Name.exe"
        if (Test-Path $candidate) { return $candidate }
    }

    $roots = @("$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Microsoft", "$env:ProgramFiles\Amazon Corretto")
    foreach ($root in $roots) {
        if (Test-Path $root) {
            $found = Get-ChildItem $root -Recurse -Filter "$Name.exe" -ErrorAction SilentlyContinue |
                Select-Object -First 1
            if ($found) { return $found.FullName }
        }
    }
    throw "Không tìm thấy công cụ '$Name' của JDK. Hãy cài JDK 17+ và thêm vào PATH."
}

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
    $jarTool = Resolve-JdkTool "jar"
    $manifestPath = Join-Path $PSScriptRoot "build\MANIFEST.MF"
    [System.IO.File]::WriteAllText($manifestPath,
        "Manifest-Version: 1.0`r`nMain-Class: vn.edu.hcmus.slang.App`r`n`r`n",
        [System.Text.Encoding]::ASCII)
    & $jarTool cfm $jarPath $manifestPath -C build\classes .
    if ($LASTEXITCODE -ne 0) { throw "Đóng gói jar thất bại" }

    if (-not $SkipTests) {
        Write-Host "==> Compiling tests" -ForegroundColor Cyan
        $testSources = @(Get-ChildItem -Recurse src\test\java -Filter *.java | ForEach-Object { $_.FullName })
        if ($testSources.Count -gt 0) {
            & javac -encoding UTF-8 --release 17 -cp build\classes -d build\test-classes $testSources
            if ($LASTEXITCODE -ne 0) { throw "javac (tests) thất bại" }

            Write-Host "==> Running tests" -ForegroundColor Cyan
            $testErrorLog = "build\test-stderr.txt"
            & java "-Dfile.encoding=UTF-8" -Xmx1g -cp "build\classes;build\test-classes" vn.edu.hcmus.slang.AllTests 2> $testErrorLog
            $testExitCode = $LASTEXITCODE
            if ($testExitCode -ne 0) {
                if (Test-Path $testErrorLog) { Get-Content $testErrorLog }
                throw "Kiểm thử thất bại"
            }
        }
    }

    Write-Host ""
    Write-Host "BUILD OK -> dist\SlangDictionary.jar" -ForegroundColor Green
    Write-Host "Chạy: run.bat (hoặc java -jar dist\SlangDictionary.jar)" -ForegroundColor Green
} finally {
    Pop-Location
}
