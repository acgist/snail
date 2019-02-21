@echo off

call config.bat

set path=.\%runtime%\bin


rem call javaw -client -Xms128m -Xmx128m -jar %jar%
call javaw -server -Xms128m -Xmx128m -jar %jar%

exit