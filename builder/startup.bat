@echo off

call config.bat

set path=.\%runtime%\bin

rem javaw -server -Xms256m -Xmx256m -jar %jar%
call javaw -Xms256m -Xmx256m -jar %jar%

exit