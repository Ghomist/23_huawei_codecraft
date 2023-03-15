@echo off

rem coding utf-8
chcp 65001 > nul

dir /b /s /a-d "src\*.java" > sources.txt

javac -encoding UTF-8 -d bin/ @sources.txt

del sources.txt