@echo off

rem coding utf-8
chcp 65001 > nul

dir /b /s /a-d "src\*.java" > sources.txt

javac -d bin/ @sources.txt

del sources.txt

cd debug
.\Robot.exe -f -m .\maps\1.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 1.out
.\Robot.exe -f -m .\maps\2.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 2.out
.\Robot.exe -f -m .\maps\3.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 3.out
.\Robot.exe -f -m .\maps\4.txt -c ..\bin\ "java com.huawei.codecraft.Main" > 4.out

echo ------------------ >> ..\out.txt
type *.out >> ..\out.txt
del *.out