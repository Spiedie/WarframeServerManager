@echo off
title WFAllocManagerLocal
pushd %~dp0
java -Xmx192M -jar WFAllocManager.jar
pause
