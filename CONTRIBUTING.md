# Katkı Rehberi

## Gereksinimler

- JDK 17 veya üzeri
- Git

Maven kurulumu gerekmez; repository içindeki Maven Wrapper kullanılır.

## Geliştirme Akışı

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

`verify`; Enforcer, testler, JaCoCo kapsam eşikleri, Spotless format kontrolü,
paketleme ve SBOM üretimini birlikte çalıştırır.

## Rule Katkıları

- Rule ID mevcut adlandırma düzenini izlemeli ve benzersiz olmalı.
- Finding mesajı açıklanabilir olmalı; secret değerini çıktıya yazmamalı.
- Düşük false-positive yaklaşımı korunmalı.
- Pozitif, negatif ve sınır durumları test edilmeli.
- Kullanıcı davranışı değişiyorsa README ve CHANGELOG güncellenmeli.

Pull request açmadan önce `clean verify` başarılı olmalıdır. Güvenlik açıkları
için pull request veya public issue yerine [SECURITY.md](SECURITY.md) izlenmelidir.
