@echo off
echo Starting Salesforce CRUD Application...
start "Salesforce Backend" cmd /k "%~dp0start-backend.bat"
start "Salesforce Frontend" cmd /k "%~dp0start-frontend.bat"
echo Applications launched in separate windows!
echo Frontend will be available at: http://localhost:5173
echo Backend will be available at:  http://localhost:8080
