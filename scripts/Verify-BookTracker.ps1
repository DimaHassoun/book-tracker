#Requires -Version 5.1
<#
.SYNOPSIS
    End-to-end regression verification for the Book Tracker API.

.DESCRIPTION
    Exercises the full Step 5 workflow against a running instance of the app:
    auth -> library -> reading instances -> status transitions -> reading
    sessions -> library read model. Prints PASS/FAIL for every check and
    stops immediately (non-zero exit code) on the first unexpected response,
    printing expected vs. actual status.

    Written for Windows PowerShell 5.1 (Invoke-WebRequest + try/catch status
    extraction, not the PS7-only -SkipHttpErrorCheck flag).

.NOTES
    Run this after every batch, before considering it finished:
      1. Start the app (docker compose up -d ; .\mvnw.cmd spring-boot:run)
      2. .\Verify-BookTracker.ps1
      3. Optionally: .\Verify-BookTracker.ps1 -RunDbChecks
#>

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [switch]$RunDbChecks,
    [string]$DbHostName = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "booktracker",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "postgres"
)

$ErrorActionPreference = "Stop"
$script:PassCount = 0
$script:FailCount = 0

# ---------------------------------------------------------------------------
# Output helpers
# ---------------------------------------------------------------------------

function Write-SectionHeader([string]$Title) {
    Write-Host ""
    Write-Host "==== $Title ====" -ForegroundColor Cyan
}

function Write-Pass([string]$Message) {
    $script:PassCount++
    Write-Host "  [PASS] $Message" -ForegroundColor Green
}

function Write-FailAndExit([string]$Message, $Expected, $Actual) {
    $script:FailCount++
    Write-Host "  [FAIL] $Message" -ForegroundColor Red
    Write-Host "         Expected: $Expected" -ForegroundColor Yellow
    Write-Host "         Actual:   $Actual" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Stopping — a regression was detected. Fix this before continuing." -ForegroundColor Red
    Write-Summary
    exit 1
}

function Write-Summary {
    Write-Host ""
    Write-Host "==== Summary ====" -ForegroundColor Cyan
    Write-Host "  Passed: $script:PassCount" -ForegroundColor Green
    Write-Host "  Failed: $script:FailCount" -ForegroundColor $(if ($script:FailCount -gt 0) { "Red" } else { "Green" })
}

# ---------------------------------------------------------------------------
# HTTP helper (Windows PowerShell 5.1 compatible — no -SkipHttpErrorCheck)
# ---------------------------------------------------------------------------

function Invoke-Api {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Uri,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    $params = @{
        Method      = $Method
        Uri         = $Uri
        Headers     = $Headers
        ContentType = "application/json; charset=utf-8"
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }

    try {
        $response = Invoke-WebRequest @params
        $status = [int]$response.StatusCode
        $rawContent = $response.Content
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $stream = $_.Exception.Response.GetResponseStream()
            $stream.Position = 0
            $reader = New-Object System.IO.StreamReader($stream)
            $rawContent = $reader.ReadToEnd()
        } else {
            # Connection-level failure (app not running, wrong port, etc.)
            Write-Host ""
            Write-Host "Could not reach $Uri — is the app running? ($($_.Exception.Message))" -ForegroundColor Red
            Write-Summary
            exit 1
        }
    }

    $parsedBody = $null
    if ($rawContent) {
        try { $parsedBody = $rawContent | ConvertFrom-Json } catch { }
    }

    return [PSCustomObject]@{
        Status = $status
        Body   = $parsedBody
        Raw    = $rawContent
    }
}

function Assert-Status {
    param(
        [Parameter(Mandatory)]$Result,
        [Parameter(Mandatory)][int[]]$Expected,
        [Parameter(Mandatory)][string]$Context
    )
    if ($Expected -contains $Result.Status) {
        Write-Pass "$Context -> HTTP $($Result.Status)"
    } else {
        Write-FailAndExit $Context ($Expected -join " or ") $Result.Status
    }
}

function Assert-True {
    param([bool]$Condition, [string]$Context, $Expected = $true, $Actual = $false)
    if ($Condition) {
        Write-Pass $Context
    } else {
        Write-FailAndExit $Context $Expected $Actual
    }
}

# ---------------------------------------------------------------------------
# 1. Authentication
# ---------------------------------------------------------------------------

Write-SectionHeader "Authentication"

$suffix = Get-Random -Minimum 100000 -Maximum 999999
$username = "regression_$suffix"
$email = "regression_$suffix@example.com"
$password = "RegressionTest123!"

$registerResult = Invoke-Api -Method POST -Uri "$BaseUrl/auth/register" -Body @{
    username = $username
    email    = $email
    password = $password
}
Assert-Status $registerResult @(201) "Register new user ($username)"
$token = $registerResult.Body.token
Assert-True ([string]::IsNullOrWhiteSpace($token) -eq $false) "Register response includes JWT token"

$authHeaders = @{ Authorization = "Bearer $token" }

$loginResult = Invoke-Api -Method POST -Uri "$BaseUrl/auth/login" -Body @{
    usernameOrEmail = $username
    password        = $password
}
Assert-Status $loginResult @(200) "Login with correct credentials"

$badLoginResult = Invoke-Api -Method POST -Uri "$BaseUrl/auth/login" -Body @{
    usernameOrEmail = $username
    password        = "wrong-password"
}
Assert-Status $badLoginResult @(401) "Login with wrong password is rejected"

$noAuthResult = Invoke-Api -Method GET -Uri "$BaseUrl/library"
Assert-Status $noAuthResult @(401, 403) "Accessing /library without a token is rejected"

# ---------------------------------------------------------------------------
# 2. Library — add book, idempotency, retrieval
# ---------------------------------------------------------------------------

Write-SectionHeader "Library"

# A fixed, known book so re-runs are deterministic even without hitting Google Books.
$bookPayload = @{
    externalSource  = "GOOGLE_BOOKS"
    externalId      = "regression-book-$suffix"
    isbn13          = "9780000000000"
    isbn10          = "0000000000"
    title           = "The Regression Test"
    subtitle        = $null
    authors         = @("QA Author")
    publisher       = "Test Publisher"
    publishedDate   = $null
    description     = "A book that exists purely to be tested against."
    pageCount       = 300
    genres          = @("Testing")
    coverImageUrl   = $null
    language        = "en"
    ownedFormat     = "EBOOK"
}

$addResult = Invoke-Api -Method POST -Uri "$BaseUrl/library" -Headers $authHeaders -Body $bookPayload
Assert-Status $addResult @(201) "Add new book to library"
$userBookId = $addResult.Body.userBookId
$bookId = $addResult.Body.bookId
Assert-True ($null -ne $userBookId) "Add-to-library response includes userBookId"

$addAgainResult = Invoke-Api -Method POST -Uri "$BaseUrl/library" -Headers $authHeaders -Body $bookPayload
Assert-Status $addAgainResult @(200) "Adding the same book again is idempotent (200, not 201)"
Assert-True ($addAgainResult.Body.userBookId -eq $userBookId) "Idempotent add returns the same userBookId"

$libraryResult = Invoke-Api -Method GET -Uri "$BaseUrl/library" -Headers $authHeaders
Assert-Status $libraryResult @(200) "Get full library"
Assert-True (($libraryResult.Body | Where-Object { $_.userBookId -eq $userBookId }).Count -eq 1) `
    "New book appears exactly once in the library listing"

$wantToReadResult = Invoke-Api -Method GET -Uri "$BaseUrl/library?shelf=WANT_TO_READ" -Headers $authHeaders
Assert-Status $wantToReadResult @(200) "Get WANT_TO_READ shelf"
Assert-True (($wantToReadResult.Body | Where-Object { $_.userBookId -eq $userBookId }).Count -eq 1) `
    "Newly added book (no reading instance yet) appears on WANT_TO_READ shelf"

$detailResult = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
Assert-Status $detailResult @(200) "Get single book detail"
Assert-True ($detailResult.Body.shelf -eq "WANT_TO_READ") "Book detail shelf is WANT_TO_READ before any reading instance"
Assert-True ($detailResult.Body.readingInstances.Count -eq 0) "Book detail has no reading instances yet"

# ---------------------------------------------------------------------------
# 3. Reading instances — first read, read_number, shelf transitions
# ---------------------------------------------------------------------------

Write-SectionHeader "Reading Instances"

$startReadingResult = Invoke-Api -Method POST -Uri "$BaseUrl/library/$userBookId/reading-instances" -Headers $authHeaders -Body @{
    status = "READING"
}
Assert-Status $startReadingResult @(201) "Start first reading instance (READING)"
$instanceId1 = $startReadingResult.Body.id
Assert-True ($startReadingResult.Body.readNumber -eq 1) "First reading instance has read_number = 1"
Assert-True ($null -ne $startReadingResult.Body.startDate) "First READING instance auto-sets startDate"

$detailAfterStart = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
Assert-True ($detailAfterStart.Body.shelf -eq "READING") "Shelf now derives to READING"

# ---------------------------------------------------------------------------
# 4. Status transitions + status_change_log
# ---------------------------------------------------------------------------

Write-SectionHeader "Status Transitions"

$toPaused = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status = "PAUSED"
}
Assert-Status $toPaused @(200) "READING -> PAUSED"
Assert-True ($toPaused.Body.readNumber -eq 1) "Pausing does not change read_number"

$toResume = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status = "READING"
}
Assert-Status $toResume @(200) "PAUSED -> READING (resume)"
Assert-True ($toResume.Body.readNumber -eq 1) "Resuming does not change read_number (not a reread)"

$noOp = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status = "READING"
}
Assert-Status $noOp @(200) "READING -> READING is accepted as a no-op"

$toRead = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status      = "READ"
    currentPage = 300
}
Assert-Status $toRead @(200) "READING -> READ, with currentPage supplied"
Assert-True ($null -ne $toRead.Body.endDate) "Finishing sets endDate"
Assert-True ($toRead.Body.currentPage -eq 300) "current_page updates from page_at_change on status transition"

$correction = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status = "DNF"
}
Assert-Status $correction @(200) "READ -> DNF correction (terminal <-> terminal, no new instance)"
Assert-True ($correction.Body.id -eq $instanceId1) "Correction updates the same instance, not a new one"
Assert-True ($correction.Body.readNumber -eq 1) "Correction does not change read_number"

$correctionBack = Invoke-Api -Method PUT -Uri "$BaseUrl/reading-instances/$instanceId1/status" -Headers $authHeaders -Body @{
    status = "READ"
}
Assert-Status $correctionBack @(200) "DNF -> READ correction back"

$detailAfterCorrection = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
Assert-True ($detailAfterCorrection.Body.readingInstances.Count -eq 1) "Still only one reading instance after corrections"

# ---------------------------------------------------------------------------
# 5. Reread confirmation
# ---------------------------------------------------------------------------

Write-SectionHeader "Reread Confirmation"

$rereadWithoutConfirm = Invoke-Api -Method POST -Uri "$BaseUrl/library/$userBookId/reading-instances" -Headers $authHeaders -Body @{
    status = "READING"
}
Assert-Status $rereadWithoutConfirm @(409) "Starting a reread without confirmReread is rejected (409)"

$rereadWithConfirm = Invoke-Api -Method POST -Uri "$BaseUrl/library/$userBookId/reading-instances" -Headers $authHeaders -Body @{
    status        = "READING"
    confirmReread = $true
}
Assert-Status $rereadWithConfirm @(201) "Starting a reread with confirmReread=true succeeds"
$instanceId2 = $rereadWithConfirm.Body.id
Assert-True ($rereadWithConfirm.Body.readNumber -eq 2) "Reread instance has read_number = 2"

$detailAfterReread = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
Assert-True ($detailAfterReread.Body.readingInstances.Count -eq 2) "Book detail now shows two reading instances"
Assert-True ($detailAfterReread.Body.shelf -eq "READING") "Shelf reflects the latest (reread) instance"

# ---------------------------------------------------------------------------
# 6. Reading sessions
# ---------------------------------------------------------------------------

Write-SectionHeader "Reading Sessions"

$validSession = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate      = (Get-Date -Format "yyyy-MM-dd")
    startPage        = 0
    endPage          = 40
    durationMinutes  = 35
}
Assert-Status $validSession @(201) "Log a valid reading session"
Assert-True ($validSession.Body.pagesRead -eq 40) "pagesRead computed correctly (endPage - startPage)"

$invalidRange = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate = (Get-Date -Format "yyyy-MM-dd")
    startPage   = 100
    endPage     = 50
}
Assert-Status $invalidRange @(400) "Session with endPage <= startPage is rejected"

$negativeStart = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate = (Get-Date -Format "yyyy-MM-dd")
    startPage   = -5
    endPage     = 10
}
Assert-Status $negativeStart @(400) "Session with negative startPage is rejected"

$futureDate = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate = (Get-Date).AddDays(3).ToString("yyyy-MM-dd")
    startPage   = 0
    endPage     = 10
}
Assert-Status $futureDate @(400) "Session with a future date is rejected"

$badDuration = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate     = (Get-Date -Format "yyyy-MM-dd")
    startPage       = 0
    endPage         = 10
    durationMinutes = 0
}
Assert-Status $badDuration @(400) "Session with non-positive duration is rejected"

# A second, forward-progressing session should advance current_page.
$advancingSession = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate = (Get-Date -Format "yyyy-MM-dd")
    startPage   = 40
    endPage     = 90
}
Assert-Status $advancingSession @(201) "Log a second, forward-progressing session"

$detailAfterAdvance = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
$reReadInstanceAfterAdvance = $detailAfterAdvance.Body.readingInstances | Where-Object { $_.id -eq $instanceId2 }
Assert-True ($reReadInstanceAfterAdvance.currentPage -eq 90) "current_page advanced to 90 after forward session"

# An out-of-order session (re-reading an earlier chapter) must NOT move current_page backwards.
$backwardsSession = Invoke-Api -Method POST -Uri "$BaseUrl/reading-instances/$instanceId2/sessions" -Headers $authHeaders -Body @{
    sessionDate = (Get-Date -Format "yyyy-MM-dd")
    startPage   = 10
    endPage     = 30
}
Assert-Status $backwardsSession @(201) "Log an out-of-order (backwards) session"

$detailAfterBackwards = Invoke-Api -Method GET -Uri "$BaseUrl/library/$userBookId" -Headers $authHeaders
$reReadInstanceAfterBackwards = $detailAfterBackwards.Body.readingInstances | Where-Object { $_.id -eq $instanceId2 }
Assert-True ($reReadInstanceAfterBackwards.currentPage -eq 90) "current_page did NOT decrease after a backwards session (still 90)"

# ---------------------------------------------------------------------------
# 7. Library read model — shelf filters
# ---------------------------------------------------------------------------

Write-SectionHeader "Library Read Model — Shelf Filters"

foreach ($shelf in @("WANT_TO_READ", "READING", "PAUSED", "READ", "DNF")) {
    $shelfResult = Invoke-Api -Method GET -Uri "$BaseUrl/library?shelf=$shelf" -Headers $authHeaders
    Assert-Status $shelfResult @(200) "Get library filtered by shelf=$shelf"
}

$fullLibraryFinal = Invoke-Api -Method GET -Uri "$BaseUrl/library" -Headers $authHeaders
Assert-True (($fullLibraryFinal.Body | Where-Object { $_.userBookId -eq $userBookId }).Count -eq 1) `
    "Book still appears exactly once in the unfiltered library (no duplicate user_books rows)"

# ---------------------------------------------------------------------------
# Done
# ---------------------------------------------------------------------------

Write-Summary

if ($RunDbChecks) {
    $dbScript = Join-Path $PSScriptRoot "Verify-Database.ps1"
    if (Test-Path $dbScript) {
        Write-Host ""
        Write-Host "Running database-level checks..." -ForegroundColor Cyan
        & $dbScript -DbHostName $DbHostName -DbPort $DbPort -DbName $DbName -DbUser $DbUser -DbPassword $DbPassword `
            -UserBookId $userBookId -InstanceId1 $instanceId1 -InstanceId2 $instanceId2
    } else {
        Write-Host "Verify-Database.ps1 not found next to this script — skipping DB checks." -ForegroundColor Yellow
    }
}

if ($script:FailCount -eq 0) {
    Write-Host ""
    Write-Host "All checks passed." -ForegroundColor Green
    exit 0
}
