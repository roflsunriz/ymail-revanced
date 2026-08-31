param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$')]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$lines = Get-Content -LiteralPath '.\CHANGELOG.md'
$headingPattern = '^## \[' + [regex]::Escape($Version) + '\](?:\s+-\s+\d{4}-\d{2}-\d{2})?$'
$start = -1
for ($index = 0; $index -lt $lines.Count; $index++) {
    if ($lines[$index] -match $headingPattern) {
        $start = $index
        break
    }
}
if ($start -lt 0) {
    throw "CHANGELOG.mdにバージョン $Version の見出しがありません"
}

$end = $lines.Count
for ($index = $start + 1; $index -lt $lines.Count; $index++) {
    if ($lines[$index] -match '^## \[' -or $lines[$index] -match '^\[[^]]+\]:\s+') {
        $end = $index
        break
    }
}

$parent = Split-Path -Parent $OutputPath
if ($parent) {
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
}
$lines[$start..($end - 1)] | Set-Content -LiteralPath $OutputPath -Encoding utf8NoBOM
