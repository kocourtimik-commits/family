# Hledání 111

Android appka: když v telefonu vytočíš **111**, hovor se nezavolá a nahoře přes všechno
(jako notch / dynamic island) vyjede černé okno s vyhledávacím polem. Po potvrzení se otevře
vyhledávání na Googlu.

Funguje na Androidu 10 a novějším.

## Jak získat APK

### A) Android Studio
1. Otevři složku projektu v Android Studiu (File → Open).
2. Počkej na dokončení Gradle sync.
3. Build → Build Bundle(s) / APK(s) → Build APK(s).
4. APK je v `app/build/outputs/apk/debug/app-debug.apk`.

### B) Zdarma na GitHubu, bez instalace čehokoli
1. Vytvoř nový repozitář na github.com a nahraj do něj celý obsah této složky
   (včetně skryté složky `.github`).
2. Otevři záložku **Actions** → workflow **Build APK** → doběhne sám (nebo Run workflow).
3. Ve výsledku běhu dole v sekci **Artifacts** stáhni `hledani111-apk` (ZIP s `app-debug.apk`).

## Nastavení v telefonu (jednorázově)

1. Nainstaluj APK (povol instalaci z neznámých zdrojů, když se zeptá).
2. Otevři appku **Hledání 111**.
3. Klepni na **1) Povolit zobrazení přes ostatní aplikace** a zapni přepínač.
4. Klepni na **2) Nastavit jako přesměrování hovorů** a potvrď.
5. Vytoč 111 v aplikaci Telefon.

Tlačítko **Vyzkoušet overlay** ukáže ostrov i bez vytáčení.

## Úpravy

- Číslo, které spouští overlay: `TriggerCallRedirectionService.kt` (`TRIGGER_NUMBER`).
- Vzhled, pozice a animace: `IslandOverlay.kt`.
- Vyhledávač: v `IslandOverlay.kt` funkce `search()` (URL Googlu, jde nahradit třeba Seznamem
  `https://search.seznam.cz/?q=`).

## Poznámky

- Role „přesměrování hovorů" může být vždy nastavená jen pro jednu appku.
- Na některých telefonech (Xiaomi, Huawei…) bývá role nebo overlay omezená systémem.
  Pak je potřeba v nastavení povolit appce „zobrazení nad jinými aplikacemi" a vypnout
  optimalizaci baterie.
- Když číslo není 111, hovor proběhne úplně normálně.
