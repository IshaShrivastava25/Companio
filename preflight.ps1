<#
Companio pre-flight check for the demo.

Run it in a SECOND terminal while the backend is running:

    .\preflight.ps1           check only
    .\preflight.ps1 -Fix      also repair what it can: adds a demo contact if you have none,
                              sets your PINs to 1234 (real) and 9876 (duress), and clears leftover alerts

Optional: -Email, -Password, -Pin, -DuressPin, -Base
#>
param(
    [string]$Base = 'http://localhost:8080',
    [string]$Email = 'isha@bcrypt.com',
    [string]$Password = 'test1234',
    [string]$Pin = '1234',
    [string]$DuressPin = '9876',
    [switch]$Fix
)

$script:fails = 0
$script:h = $null

function Ok($m)    { Write-Host "  [ OK ]  $m" -ForegroundColor Green }
function Bad($m)   { Write-Host "  [FAIL]  $m" -ForegroundColor Red; $script:fails++ }
function Fixed($m) { Write-Host "  [FIXED] $m" -ForegroundColor Cyan }

function Api([string]$Method, [string]$Path, $Body = $null, [switch]$Anon) {
    $p = @{ Method = $Method; Uri = "$Base$Path"; SkipHttpErrorCheck = $true; StatusCodeVariable = 'sc' }
    if (-not $Anon -and $script:h) { $p.Headers = $script:h }
    if ($null -ne $Body) { $p.Body = ($Body | ConvertTo-Json -Compress); $p.ContentType = 'application/json' }
    try { $r = Invoke-RestMethod @p }
    catch { return [pscustomobject]@{ Status = 0; Body = $null; Error = $_.Exception.Message } }
    return [pscustomobject]@{ Status = $sc; Body = $r; Error = $null }
}
function AsList($x) { if ($null -eq $x) { return @() }; return @($x | ForEach-Object { $_ }) }
function Show($x) { if ($x -is [string]) { return $x }; return ($x | ConvertTo-Json -Compress) }

Write-Host ""
Write-Host "Companio pre-flight check  ($Base)" -ForegroundColor White
Write-Host ""

# 1. server and login
Write-Host "Server and login"
$r = Api POST '/api/auth/login' @{ email = $Email; password = $Password } -Anon
if ($r.Status -eq 0) {
    Bad "Cannot reach $Base. Is the backend running? ($($r.Error))"
    Write-Host "`nStart the backend first, then run this again." -ForegroundColor Red
    exit 1
}
if ($r.Status -ne 200 -or -not $r.Body.token) {
    Bad "Login failed (HTTP $($r.Status): $(Show $r.Body)). Check -Email and -Password."
    exit 1
}
$token = $r.Body.token
$script:h = @{ Authorization = "Bearer $token" }
Ok "Login works (token length $($token.Length))"

# 2. security
$r = Api GET '/api/sos' -Anon
if ($r.Status -eq 401 -or $r.Status -eq 403) { Ok "Requests without a token are rejected (HTTP $($r.Status))" }
else { Bad "A request with no token returned HTTP $($r.Status). Protected endpoints should return 401." }

$r = Api GET '/api/users/me'
if ($r.Status -eq 200 -and $r.Body.email -eq $Email) {
    if ($r.Body.PSObject.Properties.Name -contains 'password') { Bad '/api/users/me exposes a password field' }
    else { Ok "/api/users/me returns only your own details, with no password" }
} else { Bad "/api/users/me failed (HTTP $($r.Status))" }

# 3. contacts
Write-Host "`nData"
$r = Api GET '/api/contacts'
$contacts = @(AsList $r.Body)
if ($contacts.Count -ge 1) {
    Ok "$($contacts.Count) trusted contact(s): $(($contacts | ForEach-Object { $_.name }) -join ', ')"
} elseif ($Fix) {
    $a = Api POST '/api/contacts' @{ name = 'Mom'; phoneNumber = '9999999999'; relationship = 'Mother' }
    if ($a.Status -eq 200) { Fixed 'Added a demo contact: Mom (9999999999)' } else { Bad "Could not add a contact (HTTP $($a.Status): $(Show $a.Body))" }
} else {
    Bad 'You have no trusted contacts. Run with -Fix, or add one in the app.'
}

# 4. PINs (the server cannot report whether PINs exist, so -Fix sets them)
if ($Fix) {
    $a = Api PUT '/api/users/me/pins' @{ pin = $Pin; duressPin = $DuressPin }
    if ($a.Status -eq 204 -or $a.Status -eq 200) { Fixed "PINs set: $Pin (real) and $DuressPin (duress)" }
    else { Bad "Could not set PINs (HTTP $($a.Status): $(Show $a.Body))" }
}

# 5. leftover alerts
function LiveEvents { @(AsList (Api GET '/api/sos').Body | Where-Object { $_.status -eq 'PENDING' -or $_.status -eq 'ACTIVE' }) }
$live = LiveEvents
if ($live.Count -eq 0) {
    Ok 'No leftover SOS alerts'
} elseif ($Fix) {
    $resolved = 0
    $deadline = (Get-Date).AddSeconds(35)
    do {
        $live = LiveEvents
        foreach ($e in @($live | Where-Object { $_.status -eq 'ACTIVE' })) {
            $x = Api PUT "/api/sos/$($e.id)/resolve"
            if ($x.Status -eq 200) { $resolved++ }
        }
        $pending = @($live | Where-Object { $_.status -eq 'PENDING' })
        if ($pending.Count -gt 0) { Start-Sleep -Seconds 3 }
    } while ($pending.Count -gt 0 -and (Get-Date) -lt $deadline)
    if ((LiveEvents).Count -eq 0) { Fixed "Cleared $resolved leftover alert(s)" } else { Bad 'Some alerts are still live. Wait 20 seconds and run again with -Fix.' }
} else {
    Bad "$($live.Count) leftover alert(s) are still live and would appear on the demo screen. Run with -Fix."
}

# 6. web page
Write-Host "`nWeb page"
try {
    $page = Invoke-WebRequest -Uri "$Base/" -SkipHttpErrorCheck
    if ($page.StatusCode -eq 200 -and $page.Content -match 'Companio') { Ok 'The web page is served at /' }
    else { Bad "The web page is not served (HTTP $($page.StatusCode)). Copy index.html to src\main\resources\static\, allow / in SecurityConfig, and restart." }
} catch { Bad "Could not fetch the web page: $($_.Exception.Message)" }

# 7. end-to-end smoke test
Write-Host "`nEnd to end"
$t = Api POST '/api/sos/trigger' @{ latitude = 30.3165; longitude = 78.0322 }
if ($t.Status -ne 200) {
    Bad "SOS trigger failed (HTTP $($t.Status): $(Show $t.Body))"
} else {
    $c = Api PUT "/api/sos/$($t.Body.id)/cancel" @{ pin = $Pin }
    if ($c.Status -eq 200 -and $c.Body.status -eq 'CANCELLED') {
        Ok "SOS trigger and PIN cancel work (event $($t.Body.id) is CANCELLED)"
    } else {
        Bad "PIN cancel failed (HTTP $($c.Status): $(Show $c.Body)). If your PIN is not $Pin, pass -Pin <yourpin>, or run with -Fix to set it. A test alert was left behind: run with -Fix to clear it."
    }
}

Write-Host ""
Write-Host "Note: the server cannot list guardian timers. If you started one recently and did not check in, check in now or wait for it to expire before you demo." -ForegroundColor DarkGray
Write-Host ""
if ($script:fails -eq 0) {
    Write-Host "READY FOR THE DEMO" -ForegroundColor Green
    exit 0
} else {
    Write-Host "$($script:fails) problem(s) to fix before the demo" -ForegroundColor Red
    exit 1
}
