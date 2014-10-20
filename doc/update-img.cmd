@echo off
@C:\Windows\Microsoft.NET\Framework64\v4.0.30319\msbuild.exe "OfficeToImages\OfficeToImages.sln" "/p:ContinueOnError=false" "/p:StopOnFirstFailure=true"
@OfficeToImages\bin\Debug\OfficeToImages.exe "I:\Technologie\Ambeth\Dokumentation" "img\gen"
@pause