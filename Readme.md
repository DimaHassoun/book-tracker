# 📚 Book Tracker

A personal reading companion app built to answer one question honestly: *what have I actually read, and how did it go?*

Unlike Goodreads, StoryGraph, or Other free apps, Book Tracker is built around a single non-negotiable principle:

> **Reader Autonomy** — the app surfaces information. It never tells you what to read next through algorithmic recommendation pressure.

There is no "For You" feed, no recommendation carousel, and no engagement-optimized ranking anywhere in the product. Every feature either helps you track your own reading history or shows you what other readers *said*, structured and unfiltered — never what the system thinks you should do about it.

---

## Why this project exists

I read a book, checked my reading app, and it told me I'd already read it. I didn't remember a single thing about it. I rated it low at some point — so I reread it, and it turned out to be genuinely good.

That raised an obvious question: why did I rate it low the first time, if it wasn't the book's fault? The most likely explanation was timing — what I read right before or after it — but no app I used gave me an easy, visual way to actually see that. No calendar view of my reading history, no way to look back and connect "I gave this 2 stars" to "I read it back-to-back with something way better."

So I'm building the tool I wanted: one that shows my reading history clearly enough to catch that pattern, and one that can warn me *before* I fall into it again.

This is a portfolio project, but the motivation is real: demonstrating Clean Architecture in a real Spring Boot backend, deliberate documented product decisions, and a schema built to handle real complexity (rereads, status history, structured feedback) without hacks.

---

## Core principles

| Principle | What it means here |
|---|---|
| **Reader Autonomy** | Information over unprompted influence. No algorithmic feeds, carousels, or engagement-optimized ranking. The one exception — the AI Reading Advisor — only activates when you ask about a specific book, and always explains its reasoning. |
| **Privacy-First** | Reading data is never shared or monetized. |
| **Transparency** | Any research/insight feature links back to its sources. |
| **API-only book data** | Every book comes from Google Books or Open Library — never freeform user-typed metadata. Enforced at the database level (`external_id NOT NULL`). |

---

## Tech stack

**Backend**
- Java 21, Spring Boot 3.x, Clean Architecture (Presentation → Application → Domain → Infrastructure)
- Spring Security + JWT authentication
- PostgreSQL 16, Flyway migrations
- Redis (planned, caching layer)

**Frontend** *(planned)*
- React + TypeScript

**Testing**
- JUnit 5, Mockito, Testcontainers, Playwright

**External APIs**
- Google Books API (primary book search)
- Open Library API (fallback — not yet implemented)

---

## What makes the data model different

- **`user_books`** is a pure library record — "this book is on my shelf," one row per book, ever.
- **`reading_instances`** is one row *per read* of a book, so rereads are fully supported without duplicating library entries. Includes `PAUSED` as a status distinct from `DNF`.
- **`status_change_log`** records every status transition with the page it happened at.
- **Structured feedback instead of star ratings.** Reviews are a set of closed questions (Pacing, Emotional Intensity, Timing, Density, Best-read-after, and a context factor) rather than free-text prose or a 1–5 star score — this is what powers spoiler-free, comparable reader insight without needing an algorithm to interpret it.
- **Notes** are private and text-only, scoped to a specific reading instance (so a reread's notes don't blend with the first read's).

Full schema: [`V1__init_schema.sql`](./src/main/resources/db/migration/V1__init_schema.sql).

---

## AI Reading Advisor — "Should I read this next?"

This one does recommend a book — worth saying plainly, since the rest of the app deliberately avoids that. The difference is *what it's optimizing for*.

You pick a book you're already considering and ask: should I read this right now? The AI looks at your reading history — recent books, genres, pace, and your structured responses (Pacing, Emotional Intensity, Timing, etc.) — and answers one of:

* **Yes, this should be your next read.** It fits your current reading mood and builds on what you've been enjoying.
* **Maybe later.** Likely a good book, but you may get more out of it after something else.
* **Not right now.** Recent reading suggests a different book would serve you better first.

It also draws on how readers with similar tastes felt about the book *at a comparable point in their own reading journey* — not star ratings or popularity, but timing-aware pattern matching.

The idea behind it: a book isn't good or bad in isolation — timing matters. A low rating is sometimes really about reading something immediately after a masterpiece, genre burnout, or just the wrong mood at the time, not the book itself. This is the feature built directly from the origin story above — the thing that would have caught the problem before it happened, instead of after.

**Why this doesn't break Reader Autonomy the way a feed would:** it only activates on a book you already chose to ask about — it never surfaces books unprompted or ranks a shelf for you. Every answer comes with its reasoning, so you're deciding with more context, not outsourcing the decision. That's a narrower, more honest kind of recommendation than an engagement-optimized "For You" carousel, but it's still a recommendation, and this README isn't going to pretend otherwise.

**Status:** designed, not yet built — scoped for V2, after the core tracking MVP (auth, book search, reading instances, sessions, structured responses, lists) ships.

---

## Current status

🚧 **Actively in development.** Not yet feature-complete.

**Done:**
- Database schema (locked, migrated via Flyway)
- User registration + login (JWT auth, BCrypt password hashing)
- Book search via Google Books API

**In progress / up next:**
- Open Library fallback for book search
- Reading instance tracking (status transitions, reread logic)
- Reading session logging
- Structured response system, notes, reading lists
- Basic statistics

**Deferred to V2:** AI Reading Advisor, followers/feed, advanced analytics, countdown timer.

## Known limitations

- No frontend yet — backend/API only at this stage.
- Open Library fallback is stubbed but not implemented.
- No production deployment yet.
