@echo off
cd /d "%~dp0frontend"
echo Starting Frontend (Vite) on port 5173...
call npm run dev
pause
