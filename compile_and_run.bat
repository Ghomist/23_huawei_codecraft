@echo off

rem coding utf-8
chcp 65001 > nul

dir /b /s /a-d "src\*.java" > sources.txt

javac -encoding UTF-8 -d bin/ @sources.txt

del sources.txt

cd debug
.\Robot.exe -f -m .\maps\1.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 1.out
.\Robot.exe -f -m .\maps\2.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 2.out
.\Robot.exe -f -m .\maps\3.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 3.out
.\Robot.exe -f -m .\maps\4.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 4.out

type *.out > data.txt

setlocal EnableDelayedExpansion
set /a sum=0

for /f "tokens=4 delims=,:} " %%a in (data.txt) do (
    set score=%%a
    rem set score=!score:~1!
    set /a sum+=score
)

echo ------------------ >> ..\out.txt
echo The sum is %sum% >> ..\out.txt
type *.out >> ..\out.txt
del *.out
del data.txt

cls
echo The sum is %sum%