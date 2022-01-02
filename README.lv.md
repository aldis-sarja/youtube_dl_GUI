youtube-dl-GUI ir GUI frontends komandrindas programmai [youtube-dl](https://yt-dl.org/),
ar kuru, savukār, ir iespējams lejuplādēt video no YouTube un citiem avotiem.
Lai darbinātu šo programmu, ir nepieciešams uzstādīt youtube-dl kādā no direktorijām,
kura ietilpst sistēmas PATH mainīgajā. Windows sistēmā, vienkāršības pēc, youtube-dl.exe
failu var iemest tajā pašā direktorijā kur atrodas youtube-dl-GUI.jar.
youtube-dl lejuplādēšana un instalācijas pamācība [šeit](https://github.com/ytdl-org/youtube-dl/blob/master/README.md#installation).

Ja uzklikšķinot uz youtube-dl-GUI.jar faila nekas nenotiek, tad, acīmredzot, ir jāuzinstalē [Java programmatūra](https://www.oracle.com/java/technologies/downloads/).
youtube-dl-GUI.jar var darbināt sākot ar Java 1.8 versiju. Programmas nokompilētā versija atrodas bin direktorijā.

Pašas GUI programmas darbība, ceru, būs pašsaprotama.
URL lauciņā iekopējam YouTube video adresi. Izmantojam taustiņu kombināciju Ctrl-V (labā peles poga nedarbosies).
Nospiežam "Get info" pogu un pagaidam līdz youtube-dl savāks informāciju par pieejamiem formātiem.
Opciju laukā vienkāršības pēc izvēlamies formātu "Default" un lejuplādējam.
Šajā gadījumā youtube-dl programma pati piemeklē labāko formātu. Bet var gadīties, ka lejuplādētais video bremzē -
pārāk liela izšķirtspēja un vp9 kodeks prasa jaudīgāku datoru. Tad der paeksperimentēt ar citām izvēlēm.
Paturam prātā, ka starp video formātiem labāk izvairīties no av01 (pretstatā avc1, kam nav ne vainas).
