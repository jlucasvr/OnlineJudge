param(
    [string]$ApiBaseUrl = "http://localhost:8081",
    [string]$DataRoot = "",
    [string]$WorkerUsername = "",
    [string]$WorkerPassword = "",
    [switch]$SkipHealthWait,
    [int]$TimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"

function Read-DotEnv {
    param([string]$Path)
    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    Get-Content -LiteralPath $Path -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }
        $parts = $line.Split("=", 2)
        $values[$parts[0].Trim()] = $parts[1].Trim()
    }
    return $values
}

function Invoke-Json {
    param(
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string]$Method,
        [string]$Uri,
        [object]$Body = $null,
        [string]$Token = ""
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 20)
    }

    return Invoke-RestMethod @params
}

function Wait-ApiHealth {
    param([string]$BaseUrl, [int]$Timeout)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 5
            if ($health.status -eq "UP") {
                return
            }
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    throw "API nao ficou saudavel em $Timeout segundos: $BaseUrl/actuator/health"
}

function Login {
    param([string]$Username, [string]$Password)
    return Invoke-Json -Method POST -Uri "$ApiBaseUrl/auth/login" -Body @{
        username = $Username
        password = $Password
    }
}

function Register-Or-Login {
    param([string]$Username, [string]$Email, [string]$Password)
    try {
        return Invoke-Json -Method POST -Uri "$ApiBaseUrl/auth/register" -Body @{
            username = $Username
            email = $Email
            password = $Password
        }
    } catch {
        return Login -Username $Username -Password $Password
    }
}

function Wait-Submission {
    param([string]$SubmissionId, [string]$Token, [int]$Timeout)
    $terminal = @("ACCEPTED", "WRONG_ANSWER", "TIME_LIMIT", "MEMORY_LIMIT", "RUNTIME_ERROR", "COMPILATION_ERROR", "INTERNAL_ERROR")
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        $submission = Invoke-Json -Method GET -Uri "$ApiBaseUrl/submissions/$SubmissionId" -Token $Token
        Write-Host "  status=$($submission.status)"
        if ($terminal -contains $submission.status) {
            return $submission
        }
        Start-Sleep -Seconds 2
    }
    throw "Submissao $SubmissionId nao finalizou em $Timeout segundos"
}

function Assert-In {
    param([string]$Actual, [string[]]$Expected, [string]$Message)
    if ($Expected -notcontains $Actual) {
        throw "$Message Esperado: $($Expected -join ', ') | Obtido: $Actual"
    }
}

function Write-Utf8NoBom {
    param([string]$Path, [string]$Value)
    $encoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Value, $encoding)
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$envValues = Read-DotEnv -Path (Join-Path $PSScriptRoot ".env")

if (-not $WorkerUsername) {
    $WorkerUsername = $envValues["WORKER_API_USERNAME"]
}
if (-not $WorkerPassword) {
    $WorkerPassword = $envValues["WORKER_API_PASSWORD"]
}
if (-not $DataRoot) {
    $DataRoot = $envValues["OJ_HOST_DATA_ROOT"]
}
if (-not $DataRoot) {
    $DataRoot = Join-Path $repoRoot ".oj-data"
}

if (-not $WorkerUsername -or -not $WorkerPassword) {
    throw "Configure WORKER_API_USERNAME e WORKER_API_PASSWORD em infra/.env ou passe por parametro."
}

$DataRoot = $DataRoot -replace "/", [System.IO.Path]::DirectorySeparatorChar
New-Item -ItemType Directory -Force -Path $DataRoot | Out-Null

if (-not $SkipHealthWait) {
    Write-Host "Aguardando API em $ApiBaseUrl ..."
    Wait-ApiHealth -BaseUrl $ApiBaseUrl -Timeout $TimeoutSeconds
}

Write-Host "Login admin/worker ..."
$adminAuth = Login -Username $WorkerUsername -Password $WorkerPassword
$adminToken = $adminAuth.token

$stamp = Get-Date -Format "yyyyMMddHHmmss"
$contestantUsername = "e2e_user_$stamp"
$contestantPassword = "E2ePassword-$stamp"
Write-Host "Criando usuario de submissao $contestantUsername ..."
$contestantAuth = Register-Or-Login -Username $contestantUsername -Email "$contestantUsername@example.test" -Password $contestantPassword
$contestantToken = $contestantAuth.token

Write-Host "Criando problema E2E ..."
$problem = Invoke-Json -Method POST -Uri "$ApiBaseUrl/problems" -Token $adminToken -Body @{
    title = "E2E Sandbox Soma $stamp"
    statement = "Leia dois inteiros e imprima a soma."
    difficulty = "EASY"
    timeLimitMs = 1000
    memoryLimitKb = 65536
    isPublic = $true
    tags = @("e2e", "sandbox")
}

$problemId = $problem.id
$inputDir = Join-Path $DataRoot "testcases\$problemId\inputs"
$outputDir = Join-Path $DataRoot "testcases\$problemId\outputs"
New-Item -ItemType Directory -Force -Path $inputDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
Write-Utf8NoBom -Path (Join-Path $inputDir "1") -Value "2 3"
Write-Utf8NoBom -Path (Join-Path $outputDir "1") -Value "5"

Write-Host "Cadastrando test case ..."
$testCase = Invoke-Json -Method POST -Uri "$ApiBaseUrl/problems/$problemId/test-cases" -Token $adminToken -Body @{
    orderIndex = 1
    inputPath = "/data/testcases/$problemId/inputs/1"
    outputPath = "/data/testcases/$problemId/outputs/1"
    points = 100
    isSample = $true
}

$cases = @(
    @{
        Name = "accepted_python"
        Language = "python"
        Source = "a,b=map(int,input().split())`nprint(a+b)"
        ExpectedStatuses = @("ACCEPTED")
        ExpectedVerdicts = @("ACCEPTED")
    },
    @{
        Name = "wrong_answer_python"
        Language = "python"
        Source = "print(0)"
        ExpectedStatuses = @("WRONG_ANSWER")
        ExpectedVerdicts = @("WRONG_ANSWER")
    },
    @{
        Name = "time_limit_python"
        Language = "python"
        Source = "while True:`n    pass"
        ExpectedStatuses = @("TIME_LIMIT")
        ExpectedVerdicts = @("TLE")
    },
    @{
        Name = "network_blocked_python"
        Language = "python"
        Source = @'
import socket

s = socket.socket()
s.settimeout(1)
try:
    s.connect(("1.1.1.1", 80))
    print("999")
except OSError:
    print(5)
'@
        ExpectedStatuses = @("ACCEPTED")
        ExpectedVerdicts = @("ACCEPTED")
    },
    @{
        Name = "host_data_not_mounted_python"
        Language = "python"
        Source = @"
from pathlib import Path

try:
    leaked = Path("/data/testcases/$problemId/outputs/1").read_text()
    print("999")
except Exception:
    print(5)
"@
        ExpectedStatuses = @("ACCEPTED")
        ExpectedVerdicts = @("ACCEPTED")
    },
    @{
        Name = "memory_pressure_python"
        Language = "python"
        Source = "chunks=[]`nwhile True:`n    chunks.append(bytearray(8*1024*1024))"
        ExpectedStatuses = @("MEMORY_LIMIT", "RUNTIME_ERROR")
        ExpectedVerdicts = @("MLE", "RE")
    }
)

$results = @()
foreach ($case in $cases) {
    Write-Host ""
    Write-Host "Submetendo caso: $($case.Name)"
    $submission = Invoke-Json -Method POST -Uri "$ApiBaseUrl/submissions" -Token $contestantToken -Body @{
        problemId = $problemId
        language = $case.Language
        sourceCode = $case.Source
    }

    $finalSubmission = Wait-Submission -SubmissionId $submission.id -Token $contestantToken -Timeout $TimeoutSeconds
    Assert-In -Actual $finalSubmission.status -Expected $case.ExpectedStatuses -Message "Status final invalido para $($case.Name)."

    $verdicts = Invoke-Json -Method GET -Uri "$ApiBaseUrl/submissions/$($submission.id)/verdicts" -Token $contestantToken
    if ($verdicts.Count -lt 1) {
        throw "Nenhum veredicto criado para $($case.Name)."
    }
    $firstVerdict = $verdicts[0]
    Assert-In -Actual $firstVerdict.result -Expected $case.ExpectedVerdicts -Message "Veredicto invalido para $($case.Name)."

    $results += [PSCustomObject]@{
        Case = $case.Name
        SubmissionId = $submission.id
        Status = $finalSubmission.status
        Verdict = $firstVerdict.result
        TimeMs = $firstVerdict.executionTimeMs
        ActualOutput = $firstVerdict.actualOutput
    }
}

Write-Host ""
Write-Host "E2E sandbox concluido com sucesso."
$results | Format-Table -AutoSize
