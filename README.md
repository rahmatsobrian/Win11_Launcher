# Win11 Launcher

Android launcher terinspirasi Windows 11 Fluent Design. Kotlin + Jetpack Compose + Clean Architecture (Hilt, Room, DataStore).

## Status

Ini adalah **MVP (Minimum Viable Product) lengkap** dengan semua fitur utama terimplementasi. Semua modul di bawah berisi kode nyata (bukan placeholder/TODO) dengan fitur lengkap.

### Sudah diimplementasikan
- **Desktop**: grid icon absolut, multi-page dengan swipe, drag & drop long-press, folder overlay, widget placement otomatis ke cell kosong
- **Taskbar**: start button, pinned apps, clock/date, system tray (wifi/bluetooth/battery/notification), notification count badge real-time
- **Start Menu**: search bar live, pinned grid, recommended apps, overlay animasi Fluent-style
- **App Drawer**: alphabetical sectioning + sticky header, sidebar index, search
- **Universal Search**: gabungan apps + settings + contacts (dengan tap action untuk contact)
- **Widgets**: AppWidgetHost wrapper, widget picker, host container (Compose interop), placement otomatis
- **Settings**: semua toggle sesuai checklist (taskbar, start menu, desktop, security), About screen, Developer Options screen
- **File Explorer**: Windows Explorer-style UI, rename dialog, file open via viewer Intent, create folder, delete
- **Persistence**: Room (apps + desktop layout) + DataStore (preferences)
- **Notification Listener**: real-time notification tracking dari NotificationListenerService

### Fitur tambahan
- **Folder Overlay**: tap folder di desktop menampilkan isi folder dalam grid overlay
- **File Provider**: proper file sharing untuk membuka file dari File Explorer
- **Back Navigation**: navigasi back yang benar (Settings → About/Developer → back ke Settings)
- **Immersive Fullscreen**: system bar hiding dengan support cross-OEM

### Fitur yang belum diimplementasikan (opsional, untuk pengembangan lebih lanjut)
- Icon pack support
- Live wallpaper
- Gesture customization (swipe gestures khusus)
- Unit/UI/screenshot tests

## Cara membuka

1. Buka folder ini di Android Studio (Koala atau lebih baru direkomendasikan untuk AGP 8.6+)
2. Sync Gradle
3. Jalankan ke device/emulator, lalu set sebagai default launcher via Settings > Apps > Default apps > Home app

## Struktur modul

```
app/                    → entry point, manifest launcher, navigation host
core/designsystem/      → Theme, Color, Typography, token Fluent (acrylic, blur)
core/common/            → utilities lintas modul
core/domain/            → model + interface repository + use case (pure Kotlin)
core/database/          → Room entities, DAO
core/datastore/         → Preferences DataStore
core/data/              → implementasi repository, mapper
feature/desktop/        → layar Desktop (grid, drag-drop, folder overlay)
feature/taskbar/        → layar Taskbar (system tray, notification badge)
feature/startmenu/      → layar Start Menu
feature/appdrawer/      → layar App Drawer
feature/search/         → Universal Search
feature/widgets/        → AppWidgetHost wrapper
feature/settings/       → layar Settings (About, Developer Options)
feature/filemanager/    → File Explorer (rename, file open, folder management)
```

## Kontribusi lanjutan

Kirim log error compile dari Android Studio kalau ada masalah — perbaikan targeted jauh lebih cepat daripada regenerate ulang.
