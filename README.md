# Win11 Launcher

Android launcher terinspirasi Windows 11 Fluent Design. Kotlin + Jetpack Compose + Clean Architecture (Hilt, Room, DataStore).

## Status

Ini adalah **skeleton production-grade tahap awal**, bukan build final siap Play Store. Semua modul di bawah berisi kode nyata (bukan placeholder/TODO), tapi belum pernah dikompilasi di Android Studio nyata — jadi anggap ini starting point solid yang perlu satu-dua putaran perbaikan compile error kecil (mismatch versi library, dsb) sebelum jalan mulus.

### Sudah diimplementasikan
- **Desktop**: grid icon absolut, multi-page dengan swipe, drag placeholder, folder model
- **Taskbar**: start button, pinned apps, clock/date, system tray (wifi/bluetooth/battery/notification)
- **Start Menu**: search bar live, pinned grid, recommended apps, overlay animasi Fluent-style
- **App Drawer**: alphabetical sectioning + sticky header, sidebar index, search
- **Universal Search**: gabungan apps + settings + contacts (provider settings/contacts perlu diimplementasi di modul `app`)
- **Widgets**: AppWidgetHost wrapper, widget picker, host container (Compose interop)
- **Settings**: semua toggle sesuai checklist (taskbar, start menu, desktop, security), backup/restore JSON
- **Persistence**: Room (apps + desktop layout) + DataStore (preferences)

### Belum diimplementasikan (langkah selanjutnya)
- Icon loading nyata dari PackageManager (saat ini pakai placeholder warna+huruf)
- Drag & drop nyata di Desktop (posisi saat ini statis dari DB, belum ada gesture reposition)
- Context menu (long-press: uninstall, app info, pin/unpin)
- Quick Settings panel & Notification Center (dirujuk di TaskbarScreen tapi belum ada layarnya)
- Implementasi konkret `SystemStatusProvider`, `SettingsSearchProvider`, `ContactsSearchProvider` di modul `app`
- Icon pack support, live wallpaper, gesture customization
- Unit/UI/screenshot tests
- CI/CD GitHub Actions

## Cara membuka

1. Buka folder ini di Android Studio (Koala atau lebih baru direkomendasikan untuk AGP 8.6+)
2. Sync Gradle
3. Jika ada compile error, kemungkinan besar karena versi library di `gradle/libs.versions.toml` perlu disesuaikan dengan versi stabil terbaru saat kamu building — cek versi di [Android Developers](https://developer.android.com/jetpack/androidx/versions)
4. Jalankan ke device/emulator, lalu set sebagai default launcher via Settings > Apps > Default apps > Home app

## Struktur modul

```
app/                    → entry point, manifest launcher, navigation host
core/designsystem/      → Theme, Color, Typography, token Fluent (acrylic, blur)
core/common/            → utilities lintas modul
core/domain/            → model + interface repository + use case (pure Kotlin)
core/database/          → Room entities, DAO
core/datastore/         → Preferences DataStore
core/data/              → implementasi repository, mapper
feature/desktop/        → layar Desktop
feature/taskbar/        → layar Taskbar
feature/startmenu/      → layar Start Menu
feature/appdrawer/      → layar App Drawer
feature/search/         → Universal Search
feature/widgets/        → AppWidgetHost wrapper
feature/settings/       → layar Settings
```

## Kontribusi lanjutan

Kirim log error compile dari Android Studio kalau ada masalah — perbaikan targeted jauh lebih cepat daripada regenerate ulang.
