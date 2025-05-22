package com.example.easydrive.dades

data class AjudaDades(
    val pregunta: String,
    val resposta: String
)

val llistaAjudaClient = listOf<AjudaDades>(
    AjudaDades("Com puc registrar-me com a conductor?", "Pots registrar-te com a conductor descarregant l’app d’EasyDrive per a conductors i seguint els passos de registre. Necessitaràs documentació bàsica com el teu permís de conduir."),
    AjudaDades("Quins documents necessito per començar a conduir?", "Necessites:\n\nDNI o NIE\n\nPermís de conduir vigent\n\nFitxa tècnica del vehicle\n"),
    AjudaDades("El meu compte ha sigut suspès o bloquejat", "Això pot passar per motius de seguretat o incompliment de les normes d’ús. Contacta amb nosaltres a suport@easydrive.com per rebre més informació i recuperar l’accés."),
    AjudaDades("No puc demanar un vehicle", "Comprova que tinguis connexió a internet, que la teva ubicació estigui activada i que no hi hagi problemes amb la forma de pagament. Si tot està bé i encara no funciona, prova de reiniciar l’app."),
    AjudaDades("Com puc valorar un conductor?", "Després de finalitzar el viatge, l’app et mostrarà una pantalla per valorar el conductor de 1 a 5 estrelles, amb opció de deixar comentaris."),
    AjudaDades("Com demano un viatge programat per més tard?", "Selecciona l’opció de “Reserva”, selecciona el destí final, clica sobre “Reserva Taxi” i allà pots escollir el dia i l’hora de la reserva."),
    AjudaDades("Com contacto amb el servei d’atenció al client?", "Des de l’app, ves a “Ajuda” > “Contacte” o envia un correu a suport@easydrive.com. També pots trucar al 900 654 321."),
    AjudaDades("Com cancel·lo un viatge com a client?", "En l’apartat de reserves pendents hi ha totes les teves reserves que estan pendents de confirmar pel client o que estan confirmades però encara el conductor no ha arribat. Clica sobre la icona de la creu."),
)


val llistaAjudaTaxista = listOf<AjudaDades>(
    AjudaDades("Què faig si el passatger no es presenta?", "Espera els 5 minuts de cortesia. Si no arriba, pots cancel·lar el viatge seleccionant “El passatger no s’ha presentat”. Podries rebre una compensació pel temps d’espera."),
    AjudaDades("No puc iniciar sessió a l'app de conductor", "Comprova que la connexió a internet sigui estable i que les dades d’accés siguin correctes. Si el problema persisteix, intenta restablir la contrasenya o contacta amb suport."),
    AjudaDades("Com actualitzo la meva documentació?", "Ves a la secció “Perfil” dins de l’app EasyDrive com a conductor. Des d’allà podràs pujar qualsevol document nou o renovat."),
    AjudaDades("Com indico que no estic disponible?", "A la pantalla principal de l'aplicació hi ha un interruptor amb el qual pots indicar si estàs disponible o no."),
    AjudaDades("Hi ha un número d’atenció per a emergències?", "Sí, pots trucar al 900 123 456 per a emergències durant el servei. Està disponible 24/7."),
    AjudaDades("Com funcionen les puntuacions dels passatgers?", "Després de cada viatge pots valorar el teu passatger. També ells et poden valorar a tu. Aquestes puntuacions ajuden a mantenir la qualitat del servei."),
    AjudaDades("El meu compte ha sigut bloquejat, què puc fer?", "Contacta amb el nostre servei d’atenció a conductors mitjançant l’app o envia un correu a suport@easydrive.com. T’informarem del motiu i dels passos per reactivar el teu compte."),
)