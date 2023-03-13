@echo off

rem coding utf-8
chcp 65001 > nul

dir /b /s /a-d "src\*.java" > sources.txt

javac -d bin/ @sources.txt

del sources.txt