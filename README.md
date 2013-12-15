TC65Lib (javacint)
=======
The goal of this library is to provide a base library for existing and new programs on all the Gemalto java enabled M2M devices:
* TC65
* TC65i / TC65i-X
* BSG5 / BSG5-X

This library is actually intended to be used as a framework. You don't have to use all the components but some of them are mandatory (like logging, settings, at handling).

It is highly recommended to obfuscate your programs to remove the unnecessary components of the library.

The library is now looking more and more to viable alternative to my (and your) in-house code.

Javadoc: http://docs.webingenia.com/tc65lib/javadoc/ (automatically updated)
Doxygen: http://docs.webingenia.com/tc65lib/doxygen/ (includes source-code, automatically updated)

Sample version: http://webingenia.com:8080/demo/demo-0.1.9.jad

Quick look at the console: http://www.youtube.com/watch?v=oVC7AD7IUGg&t=00m27s


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

You are welcome to request features and forks and they might be accepted.
