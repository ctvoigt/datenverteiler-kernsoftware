Kernsoftware [![Build Status](https://travis-ci.org/falkoschumann/datenverteiler-kernsoftware.png)](https://travis-ci.org/falkoschumann/datenverteiler-kernsoftware)
============

Die Kernsoftware des Datenverteilers in Form eines Maven-Projekts. Der Zweck
dieses Projekts ist es, die Softwareinheiten (SWE) der Kernsoftware als
Maven-Artefakte zur Verf�gung zu stellen und so als Maven-Abh�ngigkeit nutzbar
zu machen.

Das Projekt beinhaltet die SWEs, die vom NERZ e.V. als Paket *Kernsoftware* zum
Download bereit gestellt werden.

Die Projektdokumentation befindet sich unter
http://falkoschumann.github.io/datenverteiler-kernsoftware/

Das Maven Repository http://falkoschumann.github.io/maven-repository/releases/
enth�lt alle mit diesem Projekt erzeugten Artefakte der Kernsofware: Binary
JARs, Source JARs und JavaDoc JARs.


Der *master* Branch
-------------------

Der *master* Branch enth�lt die Kernsoftware in Version 3.5.0 vom 15.04.2012.


Der *develop* Branch
--------------------

Der *develop* Branch umfasst gegen�ber dem *master* Branch folgende �nderungen:

- *DaV Datenverteiler-Applikationsfunktionen* (de.bsvrz.dav.daf) wurde auf Version 3.5.5 vom 13.12.2012 aktualisiert.
- *DaV Datenverteiler* (de.bsvrz.dav.dav) wurde auf Version 3.5.5 vom 13.12.2012 aktualisiert.
- *PuK Konfiguration* (de.bsvrz.puk.config) wurde auf Version 3.5.4 vom 08.11.2012 aktualisiert.
- *Kappich Parametrierung* (de.kappich.puk.param) wurde auf Version 3.5.1 vom 30.04.2012 aktualisiert.


Hinweise zur Maven-Konfiguration
--------------------------------

Die SWEs der Kernsoftware sind als Unterprojekte angelegt. Im Root-Projekt sind
alle gemeinsamen Einstellungen konfiguriert, einschlie�lich der Sektionen f�r
*build* und *reporting*.

In den Unterprojekten der SWEs sind nur vom Root-Projekt abweichende
Einstellungen konfiguriert. Insbesondere bei Reports ist im Root-Projekt der
aggregierte Report aktiviert und im Unterprojekt wieder deaktiviert.


---

Dieses Projekt ist nicht Teil des NERZ e.V. Die offizielle Software sowie
weitere Informationen zur bundeseinheitlichen Software f�r
Verkehrsrechnerzentralen (BSVRZ) finden Sie unter http://www.nerz-ev.de.
