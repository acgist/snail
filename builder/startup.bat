@echo off

call config.bat

set path=.\%runtime%\bin


rem call start "snail" javaw -client -Xms128m -Xmx128m -jar %jar%
call start "snail" javaw -server -Xms128m -Xmx128m -jar %jar%

exit