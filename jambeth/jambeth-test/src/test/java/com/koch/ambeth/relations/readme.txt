Das relations package soll als Basis für eine vollständige Abdeckung der Relationstests dienen. Jedes Unterscheidungskriterum wird als Verzweigungsebene im Packagenamen umgesetzt.
Jeder Test soll eigene Entitäten mit nur den benötigten Properties und eigene sql-Dateien enthalten, um unnötige Komplexität zu vermeiden die den Test verfälschen könnte. Zudem sollten sich Tests nur an den "Blättern" der Package-Struktur befinden.

1. Level -> one, many				(Ist es eine to-one oder to-many Relation)
2. Level -> lazy, eager, version	(Ladeverhalten, version steht für eager_version)
3. Level -> link, fk				(Ist die Relation in einer Link-Tabelle oder einer Foreign-Key-Spalte gespeichert)
4. Level -> forward, reverse, both	(Objekt-Ebene: Vom linken Objekt (Link-Tabelle)/Objekt der FK-Tabelle aus, umgekehrt oder bi-direktional)
5. Level -> both, lr, rl, none		(Cascade-Delete: beide Richtungen, links nach rechts, rechts nach links und gar nicht)
