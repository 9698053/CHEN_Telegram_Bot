# KitsuBot – Telegram Anime & Manga Tracker

Bot Telegram per cercare anime e manga e gestire una watchlist personale usando le **API di Kitsu.io**.

---

## API (Kitsu.io)

Il bot utilizza le **Kitsu REST API** per:

 Ricercare **anime** e **manga**
* 📄 Ottenere informazioni base (titolo, sinossi, episodi/capitoli, rating, immagini)

---

## Comandi Principali

### Ricerca

* `/anime` → modalità ricerca anime
* `/manga` → modalità ricerca manga
  *(dopo il comando, scrivi il titolo)*
* /help → mostra il menu dei commandi

### Watchlist

* `/addwatch <anime|manga> <titolo>` → aggiunge alla watchlist
* `/remove <anime|manga> <titolo>` → rimuove
* `/done <anime|manga> <titolo>` → segna come completato

### Progresso & Valutazione

* `/progress <anime|manga> <titolo> <numero>` → episodi/capitoli visti
* `/rate <anime|manga> <titolo> <1-10>` → voto personale
* `/note <anime|manga> <titolo> <testo>` → nota personale

### Liste

* `/listwatch` → in corso
* `/listwatched` → completati

---

## 🗄️ Database (SQLite)

Database **SQLite** creato automaticamente al primo avvio.

### Tabelle principali

**Users**

```sql
user_id INTEGER PRIMARY KEY
username TEXT
first_name TEXT
join_date TEXT
```

**Watchlist**

```sql
id INTEGER PRIMARY KEY
user_id INTEGER
type TEXT        -- anime | manga
title TEXT
status TEXT      -- watching | completed
progress INTEGER
rating INTEGER
note TEXT
```

**Anime / Manga** // per la visualizzazione delle cronologie

```sql
title TEXT UNIQUE
synopsis TEXT
episodes | chapters INTEGER
image_url TEXT
rating REAL
start_date TEXT
```

Gestione DB tramite **DAO** dedicati (`UserDAO`, `WatchlistDAO`, `AnimeDAO`, `MangaDAO`).

---

**Difetti del API**
-non mostra il episodio attuale
-non ritorna la lingua del anime
-non torna il linke del episodio


