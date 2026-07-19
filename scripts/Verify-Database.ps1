#Requires -Version 5.1
<#
.SYNOPSIS
    Database-level checks that complement Verify-BookTracker.ps1.

.DESCRIPTION
    Requires the `psql` client on PATH and network access to the Postgres
    container (e.g. via `docker compose exec` or a published port). Prints
    row counts for the key tables and validates specific invariants for the
    entities created during the last Verify-BookTracker.ps1 run.

    Can also be run standalone for a general health check:
        .\Verify-Database.ps1
#>

param(
    [string]$DbHostName = "localhost",
    [string]$DbPort = "5432",
    [string]$DbName = "booktracker",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "postgres",
    [string]$UserBookId = $null,
    [string]$InstanceId1 = $null,
    [string]$InstanceId2 = $null
)

$script:DbPassCount = 0
$script:DbFailCount = 0

function Write-DbPass([string]$Message) {
    $script:DbPassCount++
    Write-Host "  [PASS] $Message" -ForegroundColor Green
}

function Write-DbFail([string]$Message, $Expected, $Actual) {
    $script:DbFailCount++
    Write-Host "  [FAIL] $Message" -ForegroundColor Red
    Write-Host "         Expected: $Expected" -ForegroundColor Yellow
    Write-Host "         Actual:   $Actual" -ForegroundColor Yellow
}

$psqlAvailable = $null -ne (Get-Command psql -ErrorAction SilentlyContinue)
if (-not $psqlAvailable) {
    Write-Host "psql not found on PATH — skipping database checks entirely." -ForegroundColor Yellow
    Write-Host "Install the PostgreSQL client tools, or run this from inside the db container:" -ForegroundColor Yellow
    Write-Host "  docker compose exec db psql -U $DbUser -d $DbName" -ForegroundColor Yellow
    exit 0
}

$env:PGPASSWORD = $DbPassword

function Invoke-Sql {
    param([Parameter(Mandatory)][string]$Query)
    $result = & psql -h $DbHostName -p $DbPort -U $DbUser -d $DbName -t -A -c $Query 2>$null
    return ($result -join "`n").Trim()
}

function Invoke-SqlScalar {
    param([Parameter(Mandatory)][string]$Query)
    $value = Invoke-Sql -Query $Query
    if ([string]::IsNullOrWhiteSpace($value)) { return 0 }
    return [int]$value
}

Write-Host ""
Write-Host "==== Table Row Counts ====" -ForegroundColor Cyan
foreach ($table in @("users", "books", "user_books", "reading_instances", "status_change_log", "reading_sessions")) {
    $count = Invoke-SqlScalar -Query "SELECT COUNT(*) FROM $table;"
    Write-Host ("  {0,-20} {1}" -f $table, $count)
}

Write-Host ""
Write-Host "==== Structural Invariants ====" -ForegroundColor Cyan

# No user can have the same book twice in user_books.
$dupUserBooks = Invoke-SqlScalar -Query @"
SELECT COUNT(*) FROM (
    SELECT user_id, book_id, COUNT(*) c FROM user_books GROUP BY user_id, book_id HAVING COUNT(*) > 1
) dupes;
"@
if ($dupUserBooks -eq 0) {
    Write-DbPass "No duplicate (user_id, book_id) rows in user_books"
} else {
    Write-DbFail "No duplicate (user_id, book_id) rows in user_books" 0 $dupUserBooks
}

# read_number must be sequential (1, 2, 3, ...) with no gaps per user_book.
$badReadNumberSequences = Invoke-SqlScalar -Query @"
SELECT COUNT(*) FROM (
    SELECT user_book_id,
           read_number,
           ROW_NUMBER() OVER (PARTITION BY user_book_id ORDER BY read_number) AS expected_position
    FROM reading_instances
) seq
WHERE read_number <> expected_position;
"@
if ($badReadNumberSequences -eq 0) {
    Write-DbPass "read_number is a gap-free sequence (1, 2, 3, ...) within every user_book"
} else {
    Write-DbFail "read_number is a gap-free sequence within every user_book" 0 $badReadNumberSequences
}

# Every reading_instance must belong to an existing user_book (FK already enforces this,
# but a direct check documents the invariant for anyone reading the report).
$orphanInstances = Invoke-SqlScalar -Query @"
SELECT COUNT(*) FROM reading_instances ri
LEFT JOIN user_books ub ON ub.id = ri.user_book_id
WHERE ub.id IS NULL;
"@
if ($orphanInstances -eq 0) {
    Write-DbPass "No orphaned reading_instances (all reference a valid user_book)"
} else {
    Write-DbFail "No orphaned reading_instances" 0 $orphanInstances
}

# Every status_change_log row must reference an existing reading_instance.
$orphanLogs = Invoke-SqlScalar -Query @"
SELECT COUNT(*) FROM status_change_log scl
LEFT JOIN reading_instances ri ON ri.id = scl.reading_instance_id
WHERE ri.id IS NULL;
"@
if ($orphanLogs -eq 0) {
    Write-DbPass "No orphaned status_change_log rows"
} else {
    Write-DbFail "No orphaned status_change_log rows" 0 $orphanLogs
}

# ---------------------------------------------------------------------------
# Targeted checks for the run just performed by Verify-BookTracker.ps1
# ---------------------------------------------------------------------------

if ($UserBookId) {
    Write-Host ""
    Write-Host "==== Checks For This Run (user_book_id = $UserBookId) ====" -ForegroundColor Cyan

    $userBookCount = Invoke-SqlScalar -Query "SELECT COUNT(*) FROM user_books WHERE id = '$UserBookId';"
    if ($userBookCount -eq 1) {
        Write-DbPass "Exactly one user_books row exists for this run's book"
    } else {
        Write-DbFail "Exactly one user_books row exists for this run's book" 1 $userBookCount
    }

    $instanceCount = Invoke-SqlScalar -Query "SELECT COUNT(*) FROM reading_instances WHERE user_book_id = '$UserBookId';"
    if ($instanceCount -eq 2) {
        Write-DbPass "Exactly two reading_instances exist (original read + one reread)"
    } else {
        Write-DbFail "Exactly two reading_instances exist (original read + one reread)" 2 $instanceCount
    }
}

if ($InstanceId1) {
    # Instance 1 went READING -> PAUSED -> READING -> READING(no-op) -> READ -> DNF -> READ.
    # That is 5 *real* transitions (the no-op must NOT have logged), so 5 status_change_log rows.
    $logCountInstance1 = Invoke-SqlScalar -Query "SELECT COUNT(*) FROM status_change_log WHERE reading_instance_id = '$InstanceId1';"
    if ($logCountInstance1 -eq 5) {
        Write-DbPass "status_change_log has exactly 5 rows for instance 1 (no-op transition did not log)"
    } else {
        Write-DbFail "status_change_log row count for instance 1 (no-op must not create a row)" 5 $logCountInstance1
    }

    $instance1Status = Invoke-Sql -Query "SELECT status FROM reading_instances WHERE id = '$InstanceId1';"
    if ($instance1Status -eq "READ") {
        Write-DbPass "Instance 1 final status is READ (after DNF correction back to READ)"
    } else {
        Write-DbFail "Instance 1 final status" "READ" $instance1Status
    }

    $instance1ReadNumber = Invoke-SqlScalar -Query "SELECT read_number FROM reading_instances WHERE id = '$InstanceId1';"
    if ($instance1ReadNumber -eq 1) {
        Write-DbPass "Instance 1 read_number is still 1 (corrections never change it)"
    } else {
        Write-DbFail "Instance 1 read_number after corrections" 1 $instance1ReadNumber
    }
}

if ($InstanceId2) {
    $instance2ReadNumber = Invoke-SqlScalar -Query "SELECT read_number FROM reading_instances WHERE id = '$InstanceId2';"
    if ($instance2ReadNumber -eq 2) {
        Write-DbPass "Instance 2 (reread) has read_number = 2"
    } else {
        Write-DbFail "Instance 2 read_number" 2 $instance2ReadNumber
    }

    $sessionCountInstance2 = Invoke-SqlScalar -Query "SELECT COUNT(*) FROM reading_sessions WHERE reading_instance_id = '$InstanceId2';"
    if ($sessionCountInstance2 -eq 3) {
        Write-DbPass "reading_sessions has exactly 3 rows for instance 2 (2 valid + 1 backwards, all logged)"
    } else {
        Write-DbFail "reading_sessions row count for instance 2" 3 $sessionCountInstance2
    }

    $instance2CurrentPage = Invoke-SqlScalar -Query "SELECT current_page FROM reading_instances WHERE id = '$InstanceId2';"
    if ($instance2CurrentPage -eq 90) {
        Write-DbPass "Instance 2 current_page is 90 (unaffected by the backwards session)"
    } else {
        Write-DbFail "Instance 2 current_page after backwards session" 90 $instance2CurrentPage
    }
}

Write-Host ""
Write-Host "==== Database Summary ====" -ForegroundColor Cyan
Write-Host "  Passed: $script:DbPassCount" -ForegroundColor Green
Write-Host "  Failed: $script:DbFailCount" -ForegroundColor $(if ($script:DbFailCount -gt 0) { "Red" } else { "Green" })

Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue

if ($script:DbFailCount -gt 0) { exit 1 }
