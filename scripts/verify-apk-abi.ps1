[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Path,

    [string]$DeviceSerial,

    [ValidateNotNullOrEmpty()]
    [string[]]$RequiredAbi
)

if ($DeviceSerial -and $RequiredAbi) {
    throw '-DeviceSerialと-RequiredAbiは同時に指定できません。'
}

$resolved = Resolve-Path -LiteralPath $Path -ErrorAction Stop
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($resolved.Path)

try {
    $apkAbis = @(
        $archive.Entries |
            ForEach-Object FullName |
            Where-Object { $_ -match '^lib/([^/]+)/[^/]+\.so$' } |
            ForEach-Object { ([regex]::Match($_, '^lib/([^/]+)/')).Groups[1].Value } |
            Sort-Object -Unique
    )
} finally {
    $archive.Dispose()
}

if ($apkAbis.Count -eq 0) {
    Write-Output "APK ABI検証成功: native libraryなし（CPU ABI非依存）: $($resolved.Path)"
    return
}

$requiredAbis = @($RequiredAbi)
if ($DeviceSerial) {
    $adb = Get-Command adb -ErrorAction Stop
    $requiredAbis = @(
        (& $adb.Source -s $DeviceSerial shell getprop ro.product.cpu.abilist).Trim() -split ',' |
            Where-Object { $_ }
    )
    if ($LASTEXITCODE -ne 0 -or $requiredAbis.Count -eq 0) {
        throw "端末ABIを取得できません: $DeviceSerial"
    }
}

Write-Output "APK内ABI: $($apkAbis -join ', ')"

if ($requiredAbis.Count -eq 0) {
    Write-Output '照合対象ABIは未指定です。-DeviceSerialまたは-RequiredAbiを指定すると互換性を判定できます。'
    return
}

Write-Output "照合対象ABI: $($requiredAbis -join ', ')"
$matchingAbis = @($apkAbis | Where-Object { $_ -in $requiredAbis })
if ($matchingAbis.Count -eq 0) {
    throw "CPU ABIが一致しません。AndroidではINSTALL_FAILED_NO_MATCHING_ABISになります。元XAPKで対象ABIのsplit（例: config.arm64_v8a.apk）を選び直してください。"
}

Write-Output "APK ABI検証成功: $($matchingAbis -join ', ')"
