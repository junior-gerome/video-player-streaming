# VideoPlayer Studio Design System

*Sleek, Minimalist & Professional Dark Studio Workspace (`Operate` Mode)*

---

## 1. Color Palette & Elevation

### Dark Neutrals (Zinc Studio Base)
- `bg-base` / `zinc-950`: `#09090b` (Deep obsidian background)
- `surface-1` / `zinc-900`: `#121215` (Cards, sidebars, main panels)
- `surface-2` / `zinc-800`: `#1c1c21` (Dropdowns, modals, elevated surfaces, search input)
- `surface-3` / `zinc-700`: `#27272e` (Hover states, active chips, inactive sliders)

### Borders & Dividers
- `border-subtle`: `rgba(255, 255, 255, 0.07)` (Standard structural divider)
- `border-medium`: `rgba(255, 255, 255, 0.12)` (Card borders, popover edges)
- `border-focus`: `rgba(225, 29, 72, 0.5)` (Input and control focus state)

### Brand & Semantic Accents
- `brand-accent`: `#e11d48` (Rose 600 / Studio Ruby - restrained, high-contrast, premium)
- `brand-accent-hover`: `#f43f5e` (Rose 500)
- `brand-accent-subtle`: `rgba(225, 29, 72, 0.12)` (Active chip background)
- `status-live`: `#f59e0b` (Amber 500 - Real-time streaming)
- `status-ready`: `#10b981` (Emerald 500 - Completed/Ready track)
- `status-error`: `#ef4444` (Red 500 - System warning)

---

## 2. Typography

- **Family**: Inter / Plus Jakarta Sans / system sans-serif (`font-sans`)
- **Weights**: 400 (regular), 500 (medium), 600 (semibold)
- **Hierarchy**:
  - Topbar Logo: `18px`, `font-semibold`, tracking `-0.02em`
  - Page Headings: `22px`, `font-semibold`, tracking `-0.02em`
  - Card Titles: `14px`, `font-medium`, text-zinc-100
  - Body / Labels: `13px`, text-zinc-300
  - Captions / Meta: `11px`, `12px`, text-zinc-400
  - Numerical Timecodes: `font-mono tabular-nums` (prevents jitter during playback)

---

## 3. Layout & Geometry

- **Card Radius**: `12px` (`rounded-xl` or `rounded-lg`)
- **Pills / Chips**: `9999px` (`rounded-full`) for badges and small action controls
- **Elevation**: Subtle border `1px solid rgba(255,255,255,0.08)` paired with soft ambient shadow `shadow-[0_4px_20px_rgba(0,0,0,0.4)]`
- **Focus Rings**: `focus-visible:ring-2 focus-visible:ring-rose-500/40 focus-visible:outline-none`

---

## 4. Components & Affordances

### Top Navigation (`app.component.html`)
- Glassmorphic floating topbar with backdrop-blur (`bg-zinc-950/80 backdrop-blur-md border-b border-white/5`).
- Refined geometric icon for logo, subtle pill navigation tabs with clean active state.

### Video Cards (`video-list.component.html`)
- 16:9 ratio with refined rounded corners and subtle border.
- Floating playback pill on hover with scale transition (`transition-transform duration-200 ease-out`).
- Clean metadata row: file size, relative date, compact status indicator.
- Minimalist actions: sleek play button, unobtrusive trash/delete on hover.

### Cinema Player (`video-player.component.html`)
- Seamless black frame with soft ambient vignette.
- Scrubber: sleek 4px timeline track that expands to 6px on hover, glowing Ruby playhead, buffered track with 30% opacity.
- Controls bar: frosted glass dock with unified 20px / 24px stroke SVG icons.
- Audio Menu: organized segmented panel with clear sections, live streaming status, and modern toggle switches for Demucs & Zero-Shot TTS.
- Playlist Drawer: minimalist list with active wave equalizer icon.

### Upload Modal (`upload-dialog.component.html`)
- Minimalist modal with backdrop-blur, elegant dashed dropzone, clear formats, and fluid progress bar.
