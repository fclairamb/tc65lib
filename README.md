TC65Lib (javacint)
=======
The goal of this library is to provide a base library for existing and new programs on all the Gemalto java enabled M2M devices:
* TC65
* TC65i / TC65i-X
* BSG5 / BSG5-X
* EHS5 / EHS6

This library is actually intended to be used as a framework. You don't have to use all the components but some of them are mandatory (like logging, settings, at handling).

It is highly recommended to obfuscate your programs to remove the unnecessary components of the library.

The library is now looking more and more to viable alternative to my (and your) in-house code.

Javadoc: http://docs.webingenia.com/tc65lib/javadoc/ (automatically updated)

Doxygen: http://docs.webingenia.com/tc65lib/doxygen/ (includes source-code, automatically updated)

Sample version: http://94.23.55.152:8080/demo/demo-0.1.9.jad

To install it:
''If you have an APN named ''websfr'', it will be something like that:''

    AT^SCFG="Userware/Autostart/Delay","","50"
    AT^SCFG="Userware/Autostart","","1"
    AT^SCFG="Userware/Stdout","ASC0"
    AT^SJOTAP=,"http://94.23.55.152:8080/demo/demo.jad","a:",,,"gprs","websfr",,,,,,
    AT^SJOTAP

Quick look at the console: 
* http://asciinema.org/a/6782 (settings management demo + few commands)
* http://asciinema.org/a/6783 (auto-update on new version detection)


The features that will be provided in this library are:
- Settings management (tested)
- Watchdog management (tested)
- Logging (tested)
- Console (tested)
- Time sync (tested)
- SMS receiving and sending (tested)
- Call receiving and making (not done)
- Automatic OTAP management (tested)
- Communication with server: MQTT / M2MP / HTTP ? (NOT done)
- Email sending (tested)

You are welcome to request features and forks and they might be accepted.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/fclairamb/tc65lib/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

