param(
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$requestFile = [System.IO.Path]::GetTempFileName()
$responseFile = [System.IO.Path]::GetTempFileName()
$taskId = $null
$deleted = $false

function Invoke-ApiCurl {
    param([string]$Method, [string]$Path, [int]$ExpectedStatus, [object]$Body = $null)

    $curlArguments = @('--silent', '--show-error', '--max-time', '15',
        '--request', $Method, '--output', $responseFile, '--write-out', '%{http_code}',
        '--header', 'Accept: application/json')
    if ($null -ne $Body) {
        [System.IO.File]::WriteAllText($requestFile, ($Body | ConvertTo-Json -Depth 10),
            [System.Text.UTF8Encoding]::new($false))
        $curlArguments += @('--header', 'Content-Type: application/json', '--data-binary', "@$requestFile")
    }
    $curlArguments += "$BaseUrl$Path"
    $status = & curl.exe @curlArguments
    if ($LASTEXITCODE -ne 0) { throw "curl failed for $Method $Path" }
    $responseText = [System.IO.File]::ReadAllText($responseFile)
    if ([int]$status -ne $ExpectedStatus) {
        throw "$Method $Path expected $ExpectedStatus, got ${status}: $responseText"
    }
    Write-Host "PASS $Method $Path -> $status"
    if ($responseText.Length -gt 0) { return ConvertFrom-Json -InputObject $responseText }
}

try {
    $health = Invoke-ApiCurl 'GET' '/health' 200
    if ($health.status -ne 'UP') { throw 'Health response is not UP' }

    $created = Invoke-ApiCurl 'POST' '/api/tasks' 201 @{
        title = 'curl smoke task'
        description = 'Keep this description'
        projectId = [guid]::NewGuid().ToString()
        priority = 'HIGH'
        dueDate = '2026-09-30T18:00:00'
    }
    $taskId = $created.id
    if (-not $taskId) { throw 'Create response must contain an id' }
    if ($created.status -ne 'TODO') { throw 'New task status must be TODO' }

    $found = Invoke-ApiCurl 'GET' "/api/tasks/$taskId" 200
    if ($found.title -ne $created.title) { throw 'GET returned an unexpected title' }
    $listed = @(Invoke-ApiCurl 'GET' '/api/tasks' 200)
    if ($taskId -notin $listed.id) { throw 'Created task is missing from the list' }

    $updated = Invoke-ApiCurl 'PATCH' "/api/tasks/$taskId" 200 @{ title = 'updated by curl' }
    if ($updated.title -ne 'updated by curl') { throw 'PATCH did not update title' }
    if ($updated.description -ne $created.description -or $updated.priority -ne $created.priority) {
        throw 'PATCH changed an omitted field'
    }
    $persisted = Invoke-ApiCurl 'GET' "/api/tasks/$taskId" 200
    if ($persisted.title -ne $updated.title) { throw 'PATCH was not saved' }

    $null = Invoke-ApiCurl 'PATCH' "/api/tasks/$taskId" 400 @{ title = '   '; description = 'Must not persist' }
    $afterInvalid = Invoke-ApiCurl 'GET' "/api/tasks/$taskId" 200
    if ($afterInvalid.title -ne $updated.title -or $afterInvalid.description -ne $created.description) {
        throw 'Rejected PATCH changed the task'
    }
    $null = Invoke-ApiCurl 'POST' '/api/tasks' 400 @{ title = 'Missing required fields' }
    $null = Invoke-ApiCurl 'GET' '/api/tasks/not-a-uuid' 400

    $null = Invoke-ApiCurl 'DELETE' "/api/tasks/$taskId" 204
    $deleted = $true
    $null = Invoke-ApiCurl 'GET' "/api/tasks/$taskId" 404
    $null = Invoke-ApiCurl 'PATCH' "/api/tasks/$taskId" 404 @{ title = 'Missing task' }
    $null = Invoke-ApiCurl 'DELETE' "/api/tasks/$taskId" 404
    Write-Host 'All task API curl checks passed.'
} finally {
    if ($taskId -and -not $deleted) {
        try { $null = Invoke-ApiCurl 'DELETE' "/api/tasks/$taskId" 204 }
        catch { Write-Warning "Could not remove the smoke-test task: $taskId" }
    }
    Remove-Item -LiteralPath $requestFile, $responseFile -Force
}
