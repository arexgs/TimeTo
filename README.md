# TIME TO - Aplikasi Manajemen Tugas

Aplikasi todo list modern berbasis Java dengan integrasi kalender dan fitur beragam.

## Fitur Utama

### Fungsi Utama

* **Manajemen Tugas**: Membuat, mengedit, dan menghapus tugas dengan judul, deskripsi, dan tenggat waktu.
* **Pelacakan Status Cerdas**: Indikator visual untuk tingkat urgensi tugas:

  * Tugas terlambat (merah)
  * Tugas jatuh tempo dalam 2 hari (kuning)
  * Tugas jatuh tempo dalam 7 hari (hijau)
  * Tugas selesai (abu-abu)
* **Tampilan Kalender**: Kalender bulanan interaktif yang menampilkan tugas berdasarkan tanggal.
* **Pengurutan Otomatis**: Tugas otomatis diurutkan berdasarkan tenggat waktu.
* **Pencarian & Filter**: Pencarian real-time dengan opsi filter:

  * Semua tugas
  * Tugas terlambat saja
  * Tugas dalam 7 hari ke depan
  * Tugas selesai

### Antarmuka Pengguna

* **Desain Modern**: Tampilan bersih, berwarna, dan mudah digunakan.
* **Umpan Balik Visual**: Snackbar untuk notifikasi tindakan pengguna.
* **Penanda Hari Ini**: Tanggal saat ini diberi highlight warna khusus.
* **Indikator Urgensi**: Titik warna dan latar belakang sesuai status tenggat.
* **Hover Effects**: Efek interaktif pada elemen kalender.
* **Status Bar**: Menampilkan total tugas, selesai, dan terlambat secara real-time.

### Fitur Lanjutan

* **Sistem Pengingat**: Mengecek tugas terlambat secara berkala dan menampilkan popup.
* **Aksi Cepat**: Menandai tugas selesai langsung dari tabel.
* **Operasi Massal**: Menghapus semua tugas selesai dalam satu klik.
* **Date Picker**: Pemilihan tanggal yang mudah.
* **Layout Responsif**: Navigasi sidebar dengan daftar tugas yang dapat dicari.
  
### Skema Warna

* **Overdue**: Merah (#FFCDD2)
* **Warning**: Kuning (#FFF5B3)
* **Upcoming**: Hijau (#C8F2C3)
* **Done**: Abu-abu (#E6E6F0)
* **Today**: Cream (#FFF8E1) dengan border oranye

### Komponen Utama

**Main.java**

* Jendela aplikasi
* Tabel tugas dengan filter dan sorting
* Sidebar navigasi dan pencarian
* Status bar

**CalendarPanel.java**

* Grid kalender bulanan
* Indikator tugas
* Navigasi bulan

**EditorDialog.java**

* Form pengisian tugas
* Pemilihan tanggal
* Checkbox tanda selesai

**TableRenderer.java**

* Pewarnaan baris
* Strikethrough untuk tugas selesai
* Dukungan HTML dalam sel

### Operasi Dasar

1. **Tambah Tugas**: Klik tombol "⊕ Add Task"
2. **Edit Tugas**: Pilih tugas lalu klik "✏ Edit Selected"
3. **Hapus Tugas**: Pilih tugas lalu klik "⊗ Delete Selected"
4. **Tandai Selesai**: Centang pada tabel
5. **Lihat Kalender**: Klik ikon kalender
6. **Cari Tugas**: Ketik di search bar
7. **Gunakan Filter**: Pilih status dari dropdown

## Fitur Kalender

* Navigasi antar bulan
* Indikator jumlah tugas
* Simbol "!" untuk tugas urgent
* Klik tanggal untuk detail tugas

## Sistem Pengingat

* Mengecek setiap menit
* Snackbar jika ada tugas baru yang terlambat

## Creator

Program ini dikembangkan oleh kelompok 3 sebagai tugas proyek mata kuliah Pemrograman Berorientasi Objek.


