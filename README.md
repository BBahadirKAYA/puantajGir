# PuantajGir Android Uygulaması

Bu uygulama, personel puantaj bilgilerini Android cihazdan doğrudan Google Sheets'e göndermek için geliştirilmiştir.

## 📱 Özellikler

- Kullanıcı adı girişiyle puantaj kaydı
- Otomatik tarih ekleme
- İnternete bağlı olarak Google Apps Script üzerinden veri gönderimi
- Basit ve kullanıcı dostu arayüz
- OkHttp kütüphanesiyle arka planda veri iletimi

## 🛠 Kullanılan Teknolojiler

- **Android Studio**
- **Kotlin**
- **Google Sheets + Apps Script**
- **OkHttp 4.12.0**

## 📤 Veri Gönderimi

Uygulama aşağıdaki bilgileri gönderir:

| Alan Adı   | Açıklama                |
|------------|-------------------------|
| `AdSoyad`  | Kullanıcının adı        |
| `Tarih`    | Güncel tarih (yyyy-MM-dd) |
| `Durum`    | (Geliştirilebilir)      |
| `Aciklama` | (Geliştirilebilir)      |

## 🚀 Kurulum

1. Bu repoyu klonlayın:
   ```bash
   git clone https://github.com/BBahadirKAYA/puantajGir.git
