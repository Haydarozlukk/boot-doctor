# Boot Doctor

Boot Doctor, Spring Boot Maven projelerini production-readiness açısından
incelemek üzere geliştirilen Java tabanlı bir komut satırı aracıdır.

## Amaç

Proje; güvenlik, operasyon, test, dokümantasyon ve temel mimari pratikleri
basit ve güvenilir kontrollerle görünür hale getirmeyi amaçlar. İlk sürümlerde
kusursuz static analysis yerine düşük yanlış pozitif oranına odaklanır.

## Neden Var?

Bir Spring Boot projesinin çalışması, production ortamına hazır olduğu anlamına
gelmez. Eksik gözlemleme bağımlılıkları, açık secret değerleri, yetersiz testler
ve eksik deployment dosyaları çoğu zaman ancak teslim aşamasında fark edilir.
Boot Doctor bu temel eksikleri hızlı ve tekrarlanabilir bir CLI kontrolünde
toplamayı hedefler.

## Mevcut Durum

Bu görev yalnızca çalışan CLI iskeletini ve gelecekteki kuralların kullanacağı
temel veri yapılarını içerir. Henüz production-readiness rule implementasyonu
bulunmaz.

## Gereksinimler

- Java 17 veya üzeri
- Maven 3.6.3 veya üzeri

Proje Java 17 bytecode üretecek şekilde yapılandırılmıştır.

## Derleme ve Test

```bash
mvn test
mvn package
```

Derleme sonunda bağımlılıkları içeren çalıştırılabilir dosya
`target/boot-doctor.jar` altında oluşur.

## Çalıştırma

Geliştirme sırasında:

```bash
mvn exec:java -Dexec.args="."
```

Paketlenmiş JAR ile:

```bash
java -jar target/boot-doctor.jar .
```

### `boot-doctor` Komutu

Windows PowerShell oturumunda:

```powershell
$env:Path = "$PWD\bin;$env:Path"
boot-doctor .
```

Linux veya macOS üzerinde:

```bash
chmod +x bin/boot-doctor
export PATH="$PWD/bin:$PATH"
boot-doctor .
```

Launcher kullanılmadan Windows üzerinde doğrudan şu komut da çalıştırılabilir:

```powershell
.\bin\boot-doctor.cmd .
```

## Yardım

```bash
boot-doctor --help
```

## Örnek Terminal Çıktısı

```text
Boot Doctor
Target path: .
Status: CLI initialized successfully
```

Eksik veya geçersiz path kullanıldığında CLI açıklayıcı bir hata mesajı basar
ve `2` exit code ile sonlanır.

## v0.1 Roadmap

v0.1 sürümünde aşağıdaki kontrollerin eklenmesi planlanır:

```text
BUILD-001 Not a Spring Boot Maven Project
SEC-001 Possible plain secret in config file
SEC-002 CORS wildcard detected
OPS-001 Actuator dependency missing
OPS-002 Dockerfile missing
OPS-003 Docker Compose missing
TEST-001 No test files found
DOC-001 README.md missing
ARCH-001 Global exception handler missing
VAL-001 Validation annotation missing
```

Ayrıca score değerinin 100 üzerinden hesaplanması, finding'lerin severity
sırasıyla gösterilmesi, terminal raporu ve temiz/sorunlu iki fixture proje
ile doğrulama v0.1 kapsamındadır.

## v0.1 Dışında Bırakılanlar

- JavaParser ve symbol resolver
- Entity'nin controller response olarak kullanılması analizi
- Controller içindeki business logic analizi
- Gradle ve Kotlin desteği
- GitHub Action ve PR comment bot
- Markdown rapor çıktısı
- Otomatik düzeltme
- Web arayüzü

## Gelecek Sürüm Fikirleri

- Gradle ve Kotlin proje desteği
- JSON ve Markdown rapor formatları
- CI/CD entegrasyonları
- Yapılandırılabilir rule ve severity politikaları
- Baseline karşılaştırması ve geçmiş rapor takibi
- Güvenli otomatik düzeltme önerileri

