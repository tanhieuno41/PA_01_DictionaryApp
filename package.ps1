param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9]+$')]
    [string]$Mssv,

    [string]$VideoUrl = "",

    [string]$FullName = "",

    [string]$ClassName = "",

    [ValidateRange(0, 100)]
    [int]$Score = 100
)

$ErrorActionPreference = "Stop"
Push-Location $PSScriptRoot
try {
    Write-Host "==> Build + test" -ForegroundColor Cyan
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "build.ps1")
    if ($LASTEXITCODE -ne 0) { throw "Build hoặc kiểm thử thất bại" }

    $stage = Join-Path $PSScriptRoot "build\submission"
    if (Test-Path $stage) { Remove-Item -Recurse -Force $stage }
    New-Item -ItemType Directory -Force $stage | Out-Null

    Write-Host "==> Chuẩn bị nội dung bài nộp" -ForegroundColor Cyan
    Copy-Item -Path "src" -Destination $stage -Recurse -Force
    Copy-Item -Path "dist\SlangDictionary.jar" -Destination $stage -Force
    Copy-Item -Path "slang.txt", "README.md", "build.ps1", "run.bat", "test.bat", "pom.xml" -Destination $stage -Force
    Copy-Item -Path "report" -Destination $stage -Recurse -Force
    if (Test-Path "data") {
        Copy-Item -Path "data" -Destination $stage -Recurse -Force
    }

    # Ghi lại lịch sử commit thực tế (ảnh chụp GitHub vẫn cần sinh viên bổ sung).
    if ((Test-Path ".git") -and (Get-Command git -ErrorAction SilentlyContinue)) {
        & git log --oneline --graph --decorate | Out-File -Encoding utf8 (Join-Path $stage "report\git-log.txt")
        & git rev-list --count HEAD | ForEach-Object { "Tổng số commit: $_" } |
            Out-File -Encoding utf8 -Append (Join-Path $stage "report\git-log.txt")
    }

    $assessmentPath = Join-Path $stage "report\TuDanhGia.txt"
    $assessment = Get-Content $assessmentPath -Raw -Encoding UTF8
    if ($FullName -ne "") {
        $assessment = $assessment.Replace("Họ và tên: ....................................................", "Họ và tên: $FullName")
    }
    $assessment = $assessment.Replace("MSSV: ........................................................", "MSSV: $Mssv")
    if ($ClassName -ne "") {
        $assessment = $assessment.Replace("Lớp: .........................................................", "Lớp: $ClassName")
    }
    if ($VideoUrl -ne "") {
        $assessment = $assessment.Replace("Link video YouTube: ..........................................", "Link video YouTube: $VideoUrl")
    }
    Set-Content -Path $assessmentPath -Value $assessment -Encoding UTF8
    Copy-Item -Path $assessmentPath -Destination (Join-Path $stage "$($Mssv)_TuDanhGia.txt") -Force

    $zipPath = Join-Path $PSScriptRoot ("{0}_{1}.zip" -f $Mssv, $Score)
    if (Test-Path $zipPath) { Remove-Item -Force $zipPath }
    Write-Host "==> Đóng gói $zipPath" -ForegroundColor Cyan
    Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $zipPath -CompressionLevel Optimal

    $sizeMb = [Math]::Round((Get-Item $zipPath).Length / 1MB, 2)
    Write-Host ""
    Write-Host "PACKAGE OK: $zipPath ($sizeMb MB)" -ForegroundColor Green
    Write-Host "Tên file đúng quy ước MSSV_SoDiemTuDanhGia.zip." -ForegroundColor Green
    Write-Host "Nhớ bổ sung ảnh chụp commit GitHub và link video vào report/TuDanhGia.txt." -ForegroundColor Yellow
} finally {
    Pop-Location
}
