# Boot Doctor

[![CI](https://github.com/Haydarozlukk/boot-doctor/actions/workflows/ci.yml/badge.svg)](https://github.com/Haydarozlukk/boot-doctor/actions/workflows/ci.yml)
[![CodeQL](https://github.com/Haydarozlukk/boot-doctor/actions/workflows/codeql.yml/badge.svg)](https://github.com/Haydarozlukk/boot-doctor/actions/workflows/codeql.yml)
[![Java 17](https://img.shields.io/badge/Java-17%2B-007396)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Boot Doctor, Spring Boot Maven projelerini temel production-readiness pratikleri
açısından kontrol eden, bağımsız çalışan bir Java CLI aracıdır. Basit ve
açıklanabilir kontroller kullanır; gereksiz yanlış pozitif üretmemeye odaklanır.

## Neler Kontrol Edilir?

| Rule | Severity | Ceza | Kontrol |
|---|---:|---:|---|
| `BUILD-001` | Critical | 100 | Spring Boot Maven projesi değil |
| `SEC-001` | Critical | 20 | Config dosyasında olası açık secret |
| `SEC-002` | High | 15 | CORS wildcard kullanımı |
| `OPS-001` | Medium | 10 | Actuator dependency eksik |
| `OPS-002` | Medium | 10 | Dockerfile eksik |
| `OPS-003` | Low | 5 | Docker Compose eksik |
| `TEST-001` | High | 15 | Test dosyası bulunamadı |
| `DOC-001` | Low | 5 | README.md eksik |
| `ARCH-001` | Medium | 10 | Global exception handler eksik |
| `VAL-001` | Medium | 10 | Validation annotation eksik |

Bir rule aynı dosyada birden fazla finding üretse bile skor cezası yalnızca bir
kez uygulanır. Spring Boot Maven projesi algılanamazsa diğer kontroller
çalıştırılmaz ve skor `0` olur.

## Hızlı Kurulum

### GitHub Release Üzerinden

1. [Releases](https://github.com/Haydarozlukk/boot-doctor/releases) sayfasından
   işletim sistemine uygun paketi indir.
2. Arşivi aç.
3. Java 17 veya üzerinin kurulu olduğunu `java -version` ile doğrula.

Windows PowerShell:

```powershell
.\bin\boot-doctor.cmd C:\path\to\spring-project
```

Linux veya macOS:

```bash
./bin/boot-doctor /path/to/spring-project
```

İstersen paketin `bin` dizinini `PATH` değişkenine ekleyerek her konumdan şu
komutu kullanabilirsin:

```bash
boot-doctor .
```

### Kaynak Koddan

Gereksinim: Java 17+. Maven, proje içindeki doğrulanmış Wrapper tarafından
sağlanır.

```bash
git clone https://github.com/Haydarozlukk/boot-doctor.git
cd boot-doctor
.\mvnw.cmd clean verify
java -jar target/boot-doctor.jar .
```

Linux veya macOS üzerinde build komutu `./mvnw clean verify` şeklindedir.

Geliştirme sırasında:

```bash
.\mvnw.cmd exec:java -Dexec.args="."
```

## Kullanım

```text
boot-doctor [--fail-on-findings] <path>
```

```bash
boot-doctor --help
boot-doctor --version
boot-doctor .
boot-doctor --fail-on-findings .
```

Varsayılan olarak geçerli bir dizin analiz edildiğinde finding bulunsa bile exit
code `0` döner. CI/CD içinde `--fail-on-findings` kullanılırsa en az bir finding
için exit code `1` döner. Eksik veya geçersiz path exit code `2` üretir.

## Örnek Çıktı

```text
Boot Doctor
Target path: .
Score: 75/100
Status: NEEDS_ATTENTION
Findings: 2

[CRITICAL] SEC-001 - Possible plain secret for key 'spring.datasource.password' at line 8
  Location: src/main/resources/application.yml
[LOW] DOC-001 - README.md missing

Summary: CRITICAL=1, HIGH=0, MEDIUM=0, LOW=1, INFO=0
```

Status eşikleri:

| Skor | Status |
|---:|---|
| 100 | `READY` |
| 80-99 | `GOOD` |
| 60-79 | `NEEDS_ATTENTION` |
| 0-59 | `NOT_READY` |
| Spring Boot Maven değil | `INVALID_PROJECT` |

## Secret Kontrolü

`SEC-001`; `application.yml`, `application.yaml`, `application.properties` ve
profil varyantlarında password, token, secret ve API key benzeri alanları
kontrol eder. Secret değerleri terminal çıktısına yazılmaz.

Aşağıdaki değerler açık secret sayılmaz:

```yaml
password: ${DB_PASSWORD}
client-secret: ${CLIENT_SECRET:}
api-key: ENC(encrypted-value)
token: vault://service/token
```

Güvensiz varsayılan içeren `${DB_PASSWORD:local-password}` ise finding üretir.

## Sınırlar

Boot Doctor kaynak kodu AST veya symbol resolver ile yorumlamaz. Kontroller dosya
envanteri, güvenli XML ayrıştırma ve düşük riskli metin kalıpları kullanır. Bu
nedenle rapor, güvenlik denetiminin veya code review sürecinin yerine geçmez.

Bu sürüm yalnızca Java 17+ Spring Boot Maven projelerini destekler. Gradle,
Kotlin, otomatik fix, web arayüzü ve uzak sunucuya kaynak kod gönderimi yoktur.
Tüm analiz yerel makinede yapılır.

## Geliştirme

Windows:

```powershell
.\mvnw.cmd spotless:apply
.\mvnw.cmd clean verify
```

Linux veya macOS:

```bash
./mvnw spotless:apply
./mvnw clean verify
```

`verify`; Java/Maven sürüm kontrollerini, dependency convergence, 28 testi,
Spotless format kontrolünü ve JaCoCo kapsam eşiklerini çalıştırır. Minimum kapsam
eşikleri satır için `%90`, branch için `%70` değerindedir.

`mvn package` şu dosyaları üretir:

```text
target/boot-doctor.jar
target/boot-doctor-1.0.0-bin.zip
target/boot-doctor-1.0.0-bin.tar.gz
target/boot-doctor-1.0.0-sbom.json
```

Fixture projeler `src/test/resources/fixtures` altındadır. Release workflow,
`v1.0.0` gibi proje sürümüyle eşleşen tag push edildiğinde doğrulanmış paketleri
GitHub Release olarak yayınlar.

## Yayın Doğrulama

Her GitHub Release; SHA-256 checksum dosyası, CycloneDX JSON SBOM ve GitHub build
provenance attestation içerir. İndirilen dosyanın checksum değerini doğrulamak
için:

```powershell
Get-FileHash .\boot-doctor.jar -Algorithm SHA256
```

GitHub CLI kuruluysa provenance doğrulaması da yapılabilir:

```bash
gh attestation verify boot-doctor.jar -R Haydarozlukk/boot-doctor
```

Katkı kuralları için [CONTRIBUTING.md](CONTRIBUTING.md), güvenlik bildirimi için
[SECURITY.md](SECURITY.md) kullanılmalıdır.

## Lisans

[MIT](LICENSE)
