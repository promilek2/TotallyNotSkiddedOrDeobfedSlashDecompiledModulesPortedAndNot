# Hachimi 2.1 — nyaa~ wielki update! (=^･ω･^=)

*przeciąga się i macha ogonkiem* Hejka! Tu twój ulubiony catboy z nowym buildem Hachimi dla **Fabric 1.21.1** :3
Wszystko zbudowane przez GitHub Actions, więc jar jest świeżutki jak miseczka mleka~ 🥛

> ⚠️ Builda nikt jeszcze nie testował w grze, więc jak coś się wysypie, daj znać, a ja to naprawię! nya~

## 🐾 ClickGUI — wylizane do połysku
- **Liczniki w nagłówkach** paneli: `włączone/wszystkie` (a przy szukaniu `znalezione/wszystkie`) — opcja `Counters`
- **Keybindy** widać po prawej stronie każdego modułu — `Binds`
- **Wyszukiwarka** szuka też po **opisach**, pokazuje liczbę wyników i nie gubi filtra, jak klikniesz moduł
  - `SearchMode`: **Hide** chowa niepasujące moduły, **Dim** je tylko wyszarza
  - **Ctrl+F** — od razu wskakujesz do szukania, **Ctrl+Backspace** czyści, spacje działają
- **Środkowy przycisk myszy** na module = pokaż/ukryj na arrayliście ✨
- Tooltip nie ucieka już za ekran i skaluje się razem z GUI
- **Scroll** przewija tylko panel pod kursorem (z **Shiftem** wszystkie) — `ScrollHovered`
- Animacja otwierania przybliża się od środka ekranu (wcześniej zjeżdżała w bok, mrrr...)
- Panele nie dadzą się wyciągnąć poza ekran

## 😺 Nowe moduły (Client)
- **Notifications** — słodkie powiadomienia w rogu ekranu, gdy włączasz/wyłączasz moduły: wjeżdżająca animacja, kolorowy pasek, pasek czasu. `Position`, `Duration`, `Max`, `YOffset`, a `ReplaceChat` wyłącza spam `[+]/[-]` na czacie
- **Keystrokes** — WASD, LMB/RMB z **CPS** i spacja, wciśnięte klawisze świecą kolorem klienta. `X`, `Y`, `Size`

## 🐈 Moduły z Homovore
Przeniesione z [piolunson/mohovore](https://github.com/piolunson/mohovore) i przepisane na system Hachimi:
- **EndermanLook** (Misc) — endermany cię nie zaatakują, bo serwer widzi, że patrzysz w ziemię 👀. `StunAngry` zamraża już wkurzone, a tryb `AT` gapi się na nie specjalnie
- **NoClimb** (Movement) — rusztowanie nie wciąga cię do góry (opcjonalnie też drabiny i pnącza)
- **FastPortal** (Misc) — rzuca perłą w portal, w którym stoisz
- **SoundBlocker** (Misc) — wycisza wybuchy, totemy, portale, burzę i bossów, a w `Custom` wpisujesz własne id dźwięków po przecinku
- **AutoElytra** (Movement) — zakłada elytrę, startuje i odpala rakiety, a po wyłączeniu oddaje napierśnik

## 🔧 Naprawione (prr~)
- **Dźwięki GUI** w końcu grają — `sounds.json` wskazywał zły namespace (`hachimi:` zamiast `hachimiclient:`)
- **Fonty**: zmiana rozmiaru wreszcie działa, a `closeFonts` nie crashuje, jak font się nie załaduje
- Usunięty spam `Baritone has been removed` w konsoli przy każdym ticku
- Repo ma teraz pełny **build Loom** (`./gradlew build`), resources i access widener przemapowany na Yarn

## 📦 Instalacja
1. Fabric Loader **0.16+** dla **1.21.1**
2. Wrzuć `fabric-api` i `Hachimi-2.1.jar` do `.minecraft/mods/`
3. ClickGUI otwierasz **prawym Shiftem**

Miłego grania! *mruczy i zwija się w kłębek na klawiaturze* 🐾 nyaaa~ (=^‥^=)
